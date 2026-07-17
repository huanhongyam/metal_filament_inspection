FROM eclipse-temurin:17-jdk-jammy

RUN apt-get update && apt-get install -y unzip && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY metal_filament_inspection-0.0.1-SNAPSHOT.jar app.jar

RUN cd /tmp && \
    unzip -q /app/app.jar BOOT-INF/lib/opencv-4.5.5-1.jar && \
    unzip -q BOOT-INF/lib/opencv-4.5.5-1.jar 'nu/pattern/opencv/linux/x86_64/*' && \
    cp nu/pattern/opencv/linux/x86_64/libopencv_java455.so /usr/lib/ && \
    rm -rf BOOT-INF nu && \
    ldconfig

# 从 JAR 里提取 ONNX 模型文件
RUN unzip -q /app/app.jar BOOT-INF/classes/models/best.onnx && \
    mkdir -p /app/models && \
    cp BOOT-INF/classes/models/best.onnx /app/models/ && \
    rm -rf BOOT-INF

RUN mkdir -p /opt/YOLO_Images /app/logs

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Xms256m", "-Xmx512m", \
    "-XX:MaxMetaspaceSize=256m", \
    "-XX:+UseG1GC", \
    "-XX:MaxGCPauseMillis=200", \
    "-Xlog:gc*:file=/app/logs/gc.log:time,level:filecount=5,filesize=10m", \
    "-Dopencv.library-path=", \
    "-Dyolo.model-path=/app/models/best.onnx", \
    "-Dyolo.image-dir=/opt/YOLO_Images", \
    "-jar", "app.jar"]
