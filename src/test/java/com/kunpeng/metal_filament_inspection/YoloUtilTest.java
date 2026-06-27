package com.kunpeng.metal_filament_inspection;

import ai.onnxruntime.OrtException;
import com.kunpeng.metal_filament_inspection.config.YoloConfig;
import com.kunpeng.metal_filament_inspection.utils.YoloUtil;
import com.kunpeng.metal_filament_inspection.utils.YoloUtil.DetectionBox;
import com.kunpeng.metal_filament_inspection.utils.YoloUtil.DetectionResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opencv.core.Core;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "opencv.library-path=D:/opencv/build/java/x64/opencv_java4120.dll",
        "yolo.model-path=src/main/resources/models/best.onnx",
        "yolo.confidence-threshold=0.5",
        "yolo.nms-threshold=0.4",
        "yolo.input-width=640",
        "yolo.input-height=640"
})
public class YoloUtilTest {

    @Autowired
    private YoloUtil yoloUtil;

    @Autowired
    private YoloConfig yoloConfig;

    @BeforeAll
    static void loadOpenCV() {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("✅ OpenCV 库加载成功");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("⚠️ OpenCV 加载失败，尝试手动指定路径...");
            // 根据操作系统调整路径，或忽略（后续测试将跳过）
            // 例如 Windows: System.load("C:/opencv/build/java/x64/opencv_java455.dll");
        }
    }

    // ==================== 集成测试（使用真实模型和图片） ====================

    @Test
    void testDetect_WithRealModelAndImage() throws OrtException {
        // 模型路径
        String modelPath = yoloConfig.getModelPath();
        File modelFile = new File(modelPath);
        org.junit.jupiter.api.Assumptions.assumeTrue(modelFile.exists(),
                "模型文件不存在，跳过集成测试: " + modelFile.getAbsolutePath());

        // 图片目录：src/main/resources/textPicture/ （可自行放入 test.jpg）
        String imageDir = "src/main/resources/textPicture/";
        File imageFile = new File(imageDir);
        org.junit.jupiter.api.Assumptions.assumeTrue(imageFile.exists(),
                "测试图片不存在，请将图片放入 " + imageDir );

        // 执行检测
        DetectionResult result = yoloUtil.detect(imageFile);

        // 验证结果
        assertNotNull(result);
        List<DetectionBox> boxes = result.getBoxes();
        assertNotNull(boxes);

        System.out.println("🔍 检测到 " + boxes.size() + " 个目标：");
        for (DetectionBox box : boxes) {
            System.out.printf("  classId=%d, conf=%.2f, [%.1f, %.1f, %.1f, %.1f]%n",
                    box.classId, box.confidence, box.x1, box.y1, box.x2, box.y2);
        }

        // 可选断言：至少有一个高置信度检测框
        if (!boxes.isEmpty()) {
            assertTrue(boxes.stream().anyMatch(b -> b.confidence > 0.5),
                    "应存在置信度 > 0.5 的检测框");
        }
    }

    // ==================== 多图片测试（遍历 textPicture 目录） ====================

    @Test
    void testDetect_AllImagesInTextPicture() {
        String imageDir = "src/main/resources/textPicture/";
        File dir = new File(imageDir);
        org.junit.jupiter.api.Assumptions.assumeTrue(dir.exists() && dir.isDirectory(),
                "textPicture 目录不存在，跳过批量测试");

        File[] imageFiles = dir.listFiles((d, name) ->
                name.toLowerCase().matches(".*\\.(jpg|jpeg|png|bmp|tiff)$"));
        org.junit.jupiter.api.Assumptions.assumeTrue(imageFiles != null && imageFiles.length > 0,
                "textPicture 目录中没有图片文件");

        System.out.println("📷 发现 " + imageFiles.length + " 张图片，开始逐个检测...");
        int successCount = 0;
        int failCount = 0;
        for (File img : imageFiles) {
            try {
                DetectionResult result = yoloUtil.detect(img);
                System.out.printf("  ✅ %s -> %d 个目标%n", img.getName(), result.getBoxes().size());
                successCount++;
            } catch (Exception e) {
                System.err.printf("  ❌ %s 检测失败: %s%n", img.getName(), e.getMessage());
                failCount++;
            }
        }
        System.out.printf("批量检测完成：成功 %d，失败 %d%n", successCount, failCount);
        assertTrue(successCount > 0, "至少应有一张图片检测成功");
    }

    // ==================== 单元测试：边界条件 ====================

    @Test
    void testDetect_NullFile_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> yoloUtil.detect(null));
    }

    @Test
    void testDetect_NonExistentFile_ThrowsException() {
        File fake = new File("src/main/resources/textPicture/not_exist.jpg");
        assertThrows(IllegalArgumentException.class, () -> yoloUtil.detect(fake));
    }

    // ==================== NMS 算法基础测试（示例） ====================

    @Test
    void testNmsLogic_Manual() {
        // 注意：NMS 在 YoloUtil 中为 private 方法，这里仅作为逻辑验证示意
        // 实际可将其提取为独立工具类或改为 protected 以测试
        System.out.println("✅ NMS 单元测试（示例）");
        // 可在这里调用一个独立的 NmsUtils 方法（若有）
    }
}