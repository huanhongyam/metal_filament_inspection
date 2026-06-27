package com.kunpeng.metal_filament_inspection.mq.consumer;

import com.kunpeng.metal_filament_inspection.domain.dto.DetectTaskDTO;
import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import com.kunpeng.metal_filament_inspection.service.IDetectionBatchService;
import com.kunpeng.metal_filament_inspection.utils.QiniuUploadUtil;
import com.kunpeng.metal_filament_inspection.utils.YoloUtil;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Component
public class IoTMessageConsumer {

    @Autowired
    private YoloUtil yoloUtil;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private IDetectionBatchService detectionBatchService;

    @Autowired
    private QiniuUploadUtil qiniuUploadUtil;

    @Value("${yolo.image-dir:/data/images/}")
    private String imageDir;

    private static final DateTimeFormatter SECOND_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "detect.queue", durable = "true"),
            exchange = @Exchange(name = "detect.exchange", type = ExchangeTypes.DIRECT),
            key = "detect.task"
    ))
    @Transactional(rollbackFor = Exception.class)
    public void handleDetectTask(DetectTaskDTO task, Message message) throws Exception {
        Long batchNumber = task.getBatchNumber();
        log.info("🔬 开始处理检测任务: batchNumber={}, start={}, end={}",
                batchNumber, task.getStartTime(), task.getEndTime());

        // ===== 1. 扫描图片 =====
        List<File> images = scanImages(task.getStartTime(), task.getEndTime());
        log.info("📷 找到 {} 张图片", images.size());

        if (images.isEmpty()) {
            log.warn("⚠️ 未找到任何图片");
            return;
        }

        // ===== 2. 一次遍历：推理 + 收集数据 + 累加置信度 =====
        double totalSumConf = 0.0;
        int totalDetections = 0;
        List<DetectionBatch> detectionList = new ArrayList<>();

        for (File img : images) {
            try {
                // 2.1 YOLO 推理
                var result = yoloUtil.detect(img);

                int imgScratch = 0, imgBlock = 0, imgCluster = 0, imgMetal = 0, imgScuff = 0;

                // ====== 1. 读取原图准备画框 ======
                Mat annotatedImage = Imgcodecs.imread(img.getAbsolutePath());
                // 为不同类别定义不同颜色 (B, G, R)
                Scalar[] colors = new Scalar[]{
                        new Scalar(0, 0, 255),   // scratch: 红色
                        new Scalar(0, 255, 255), // block_defect: 黄色
                        new Scalar(255, 0, 0),   // cluster_defect: 蓝色
                        new Scalar(0, 165, 255), // metal_burr: 橙色
                        new Scalar(255, 0, 255)  // scuff: 紫色
                };

                for (var obj : result.items()) {
                    String className = obj.getClassName();
                    double confidence = obj.getProbability();

                    // 累加置信度
                    totalSumConf += confidence;
                    totalDetections++;

                    // 累加各类缺陷数量
                    switch (className) {
                        case "scratch": imgScratch++; break;
                        case "block_defect": imgBlock++; break;
                        case "cluster_defect": imgCluster++; break;
                        case "metal_burr": imgMetal++; break;
                        case "scuff": imgScuff++; break;
                        default: log.warn("未知类别: {}", className);
                    }

                    // ====== 2. 在图上画框和文字 ======
                    Scalar color = colors[obj.classId % colors.length];
                    Point topLeft = new Point(obj.x1, obj.y1);
                    Point bottomRight = new Point(obj.x2, obj.y2);

                    // 画矩形框 (厚度设为2)
                    Imgproc.rectangle(annotatedImage, topLeft, bottomRight, color, 2);

                    // 准备文字标签
                    String label = String.format("%s %.2f", className, confidence);
                    // 文字背景
                    int[] baseline = new int[1];
                    Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseline);
                    Point labelOrigin = new Point(obj.x1, obj.y1 - labelSize.height);
                    // 画文字背景矩形（更好看）
                    Imgproc.rectangle(annotatedImage,
                            new Point(obj.x1, obj.y1 - labelSize.height - baseline[0]),
                            new Point(obj.x1 + labelSize.width, obj.y1),
                            color, Imgproc.FILLED);
                    // 写文字
                    Imgproc.putText(annotatedImage, label, labelOrigin,
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(255, 255, 255), 1);
                }

                // ====== 3. 将画好框的图片保存为临时文件 ======
                File annotatedFile = new File(System.getProperty("java.io.tmpdir"), "annotated_" + System.currentTimeMillis() + ".jpg");
                Imgcodecs.imwrite(annotatedFile.getAbsolutePath(), annotatedImage);
                annotatedImage.release(); // 释放内存

                // 2.2 上传到七牛云
                String cloudUrl = qiniuUploadUtil.uploadImage(annotatedFile);

                // ====== 4. 上传完后删除临时文件 ======
                if (annotatedFile.exists()) {
                    annotatedFile.delete();
                }

                // 2.3 暂存当前图片的检测数据
                DetectionBatch detectionBatch = new DetectionBatch();
                detectionBatch.setScratchCount(imgScratch);
                detectionBatch.setBlockDefectCount(imgBlock);
                detectionBatch.setClusterDefectCount(imgCluster);
                detectionBatch.setMetalBurrCount(imgMetal);
                detectionBatch.setScuffCount(imgScuff);
                detectionBatch.setImgUrl(cloudUrl);
                detectionList.add(detectionBatch);

                log.debug("📊 图片 {}: 划痕={}, 块状={}, 簇状={}, 毛刺={}, 擦伤={}",
                        cloudUrl, imgScratch, imgBlock, imgCluster, imgMetal, imgScuff);

            } catch (Exception e) {
                log.error("❌ 处理图片失败 ", e);
            }
        }

        // ===== 3. 计算批次平均置信度 =====
        double batchAvgConf = (totalDetections == 0) ? 0.0 : (totalSumConf / totalDetections);
        log.info("📊 批次平均置信度: {}", batchAvgConf);

        // ===== 4. 批量插入数据库（每张图片一条记录） =====
        for (DetectionBatch detection : detectionList) {
            // 4.1 生成主键 ID
            Long id = idWorker.generateId(SystemConstants.DETECTION_BATCH_PREFIX);

            // 4.2 创建记录
            detection.setId(id);
            detection.setBatchNumber(batchNumber);
            detection.setStartTime(task.getStartTime());
            detection.setEndTime(task.getEndTime());
            detection.setTotalImages(images.size());  // 所有记录相同：本次批次总图片数
            detection.setAvgConfidence(BigDecimal.valueOf(batchAvgConf));  // 批次平均置信度
            detection.setStatus("SUCCESS");
        }
        detectionBatchService.saveBatch(detectionList);
        log.info("💾 图片记录已保存: 业务批次号={}, 本次批次处理的图片总数={}, 总体平均置信度={}", batchNumber,totalDetections,batchAvgConf);
        log.info("✅ 批次检测完成: batchNumber={}, 共处理 {} 张图片, 总缺陷数={}",
                batchNumber, images.size(), totalDetections);
    }
    /**
     * 扫描图片目录，按时间范围过滤（精度：秒级）
     * 文件名格式：Wire 20260627103811246 → 提取前14位数字 → 2026-06-27 10:38:11
     */
    private List<File> scanImages(LocalDateTime start, LocalDateTime end) throws Exception {
        Path dirPath = Paths.get(imageDir);
        if (!Files.exists(dirPath) || !Files.isDirectory(dirPath)) {
            log.warn("图片目录不存在: {}", imageDir);
            return new ArrayList<>();
        }
        try (Stream<Path> paths = Files.list(dirPath)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase();
                        return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".jpeg");
                    })
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        String numbers = name.replaceAll("\\D", "");
                        if (numbers.length() < 14) return false;
                        try {
                            String timeStr = numbers.substring(0, 14);
                            LocalDateTime fileTime = LocalDateTime.parse(timeStr, SECOND_FORMATTER);
                            return (fileTime.isAfter(start) || fileTime.equals(start)) &&
                                    (fileTime.isBefore(end) || fileTime.equals(end));
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        }
    }
}