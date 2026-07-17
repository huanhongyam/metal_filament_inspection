package com.kunpeng.metal_filament_inspection.utils;

import ai.onnxruntime.*;
import com.kunpeng.metal_filament_inspection.config.YoloConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class YoloUtil {

    @Autowired
    private YoloConfig yoloConfig;

    private OrtSession session;
    private OrtEnvironment env;

    // 类别名称映射（按 model 输出的 class_id 顺序）
    private static final List<String> CLASS_NAMES = List.of(
            "scratch",          // 0
            "block_defect",     // 1
            "cluster_defect",   // 2
            "metal_burr",       // 3
            "scuff"             // 4
    );

    @PostConstruct
    public void init() throws OrtException {
        env = OrtEnvironment.getEnvironment();
        OrtSession.SessionOptions options = new OrtSession.SessionOptions();
        options.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        session = env.createSession(yoloConfig.getModelPath(), options);
        log.info("✅ YOLO 模型加载成功: {}", yoloConfig.getModelPath());
    }

    /**
     * 对图片文件进行 YOLO 检测
     */
    public DetectionResult detect(File imgFile) throws OrtException {
        if (imgFile == null || !imgFile.exists()) {
            throw new IllegalArgumentException("图片文件不存在");
        }

        // 1. 强制以 3 通道彩色模式读取图片
        Mat image = Imgcodecs.imread(imgFile.getAbsolutePath(), Imgcodecs.IMREAD_COLOR);
        if (image.empty()) {
            throw new RuntimeException("无法读取图片: " + imgFile.getAbsolutePath());
        }

        int inputW = yoloConfig.getInputWidth();
        int inputH = yoloConfig.getInputHeight();

        // ===== letterbox 预处理（保持长宽比） =====
        double scale = Math.min((double) inputW / image.cols(), (double) inputH / image.rows());
        int newW = (int) (image.cols() * scale);
        int newH = (int) (image.rows() * scale);
        Mat resized = new Mat();
        Imgproc.resize(image, resized, new Size(newW, newH));

        int top    = (inputH - newH) / 2;
        int bottom = inputH - newH - top;
        int left   = (inputW - newW) / 2;
        int right  = inputW - newW - left;

        Mat padded = new Mat();
        Core.copyMakeBorder(resized, padded, top, bottom, left, right,
                Core.BORDER_CONSTANT, new Scalar(114, 114, 114));

        // ===== CHW + 归一化 (直接在 BGR 提取时转为 RGB) =====
        float[] inputData = new float[3 * inputH * inputW];
        int channelSize = inputH * inputW;
        for (int h = 0; h < inputH; h++) {
            for (int w = 0; w < inputW; w++) {
                double[] pixel = padded.get(h, w); // BGR 格式: [B, G, R]
                if (pixel == null || pixel.length < 3) continue;

                int spatialIdx = h * inputW + w;
                inputData[0 * channelSize + spatialIdx] = (float) (pixel[2] / 255.0); // R
                inputData[1 * channelSize + spatialIdx] = (float) (pixel[1] / 255.0); // G
                inputData[2 * channelSize + spatialIdx] = (float) (pixel[0] / 255.0); // B
            }
        }

        OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(inputData),
                new long[]{1, 3, inputH, inputW});

        OrtSession.Result result;
        try {
            result = session.run(Collections.singletonMap("images", tensor));
        } catch (OrtException e) {
            throw new RuntimeException("YOLO 推理失败", e);
        } finally {
            tensor.close();   // 别忘了释放，否则内存泄漏
        }

        OnnxValue outputValue = result.get(0);
        float[][][] outputData = (float[][][]) outputValue.getValue();

        // 把 letterbox 坐标还原回原图坐标所需的参数
        return postProcess(outputData, image.size(), scale, left, top);
    }

    private DetectionResult postProcess(float[][][] output, Size originalSize,
                                        double scale, int padLeft, int padTop) {
        int numAnchors = output[0][0].length;   // 8400
        int numChannels = output[0].length;     // 应为 9
        int numClasses = numChannels - 4;       // YOLOv11: 9 - 4 = 5 ✅

        if (numClasses != CLASS_NAMES.size()) {
            log.warn("模型类别数({})与配置类别数({})不一致，按模型实际输出处理",
                    numClasses, CLASS_NAMES.size());
        }

        List<DetectionBox> boxes = new ArrayList<>();

        for (int i = 0; i < numAnchors; i++) {
            // YOLOv11: index 0~3 是 xywh（已经是 640x640 坐标系下）
            float xCenter = output[0][0][i];
            float yCenter = output[0][1][i];
            float w       = output[0][2][i];
            float h       = output[0][3][i];

            // 找最大类别概率（类别从 index 4 开始）
            float maxProb = 0;
            int maxClassId = -1;
            for (int c = 0; c < numClasses; c++) {
                float prob = output[0][4 + c][i];
                if (prob > maxProb) {
                    maxProb = prob;
                    maxClassId = c;
                }
            }

            // YOLOv11 没有 objectness，类别概率直接作为置信度
            float confidence = maxProb;

            if (confidence >= yoloConfig.getConfidenceThreshold() && maxClassId >= 0) {
                // 1) letterbox 还原
                float x1 = (xCenter - w / 2f - padLeft) / (float) scale;
                float y1 = (yCenter - h / 2f - padTop)  / (float) scale;
                float x2 = (xCenter + w / 2f - padLeft) / (float) scale;
                float y2 = (yCenter + h / 2f - padTop)  / (float) scale;

                // 2) 边界裁剪
                x1 = Math.max(0, Math.min(x1, (float) originalSize.width));
                y1 = Math.max(0, Math.min(y1, (float) originalSize.height));
                x2 = Math.max(0, Math.min(x2, (float) originalSize.width));
                y2 = Math.max(0, Math.min(y2, (float) originalSize.height));

                String className = (maxClassId < CLASS_NAMES.size())
                        ? CLASS_NAMES.get(maxClassId) : "unknown";
                boxes.add(new DetectionBox(x1, y1, x2, y2, confidence, maxClassId, className));
            }
        }

        List<DetectionBox> finalBoxes = nms(boxes, yoloConfig.getNmsThreshold());
        return new DetectionResult(finalBoxes);
    }

    private List<DetectionBox> nms(List<DetectionBox> boxes, float iouThreshold) {
        boxes.sort((a, b) -> Float.compare(b.confidence, a.confidence));
        List<DetectionBox> selected = new ArrayList<>();

        for (DetectionBox candidate : boxes) {
            boolean keep = true;
            for (DetectionBox existing : selected) {
                if (calculateIoU(candidate, existing) > iouThreshold) {
                    keep = false;
                    break;
                }
            }
            if (keep) {
                selected.add(candidate);
            }
        }
        return selected;
    }

    private float calculateIoU(DetectionBox a, DetectionBox b) {
        float x1 = Math.max(a.x1, b.x1);
        float y1 = Math.max(a.y1, b.y1);
        float x2 = Math.min(a.x2, b.x2);
        float y2 = Math.min(a.y2, b.y2);
        float interArea = Math.max(0, x2 - x1) * Math.max(0, y2 - y1);
        float areaA = (a.x2 - a.x1) * (a.y2 - a.y1);
        float areaB = (b.x2 - b.x1) * (b.y2 - b.y1);
        return interArea / (areaA + areaB - interArea);
    }

    // ---------- 内部类 ----------
    public static class DetectionBox {
        public float x1, y1, x2, y2;
        public float confidence;
        public int classId;
        public String className;

        public DetectionBox(float x1, float y1, float x2, float y2, float confidence, int classId, String className) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.confidence = confidence;
            this.classId = classId;
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public float getProbability() {
            return confidence;
        }
    }

    public static class DetectionResult {
        private final List<DetectionBox> boxes;

        public DetectionResult(List<DetectionBox> boxes) {
            this.boxes = boxes;
        }

        public List<DetectionBox> getBoxes() {
            return boxes;
        }

        // 业务代码中使用的 items() 方法
        public List<DetectionBox> items() {
            return boxes;
        }
    }
}