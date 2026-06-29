package com.kunpeng.metal_filament_inspection.utils;

public class SystemConstants {
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final String WIRE_MATERIAL_PREFIX = "wireMaterial";
    public static final String DETECTION_BATCH_PREFIX = "detectionBatch";
    public static final String AGENT_TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJhZ2VudCIsInVzZXJJZCI6MTAwLCJleHAiOjE3ODIyOTYwMTN9.Mpmsy1EUne2BpBZ02YIKoYgf3w6GP_N-uVRsIZky6wGEH8g9I1EVK-jYSHWka8qx";
    public static final String HUAWEI_IOT_MESSAGE_PREFIX1 = "/notify_data/body/services/0/properties/1";
    public static final String HUAWEI_IOT_MESSAGE_PREFIX2 = "/notify_data/body/services/0/properties/1/2";
    public static final String HUAWEI_IOT_MESSAGE_SURFACE_PREFIX = "/notify_data/body/services/0/properties/surface_data";
    public static final String RABBITMQ_EXCHANGE_DETECT_TASK = "detect.exchange";
    public static final String RABBITMQ_EXCHANGE_SENDDOWN_QUEUE = "senddown.queue";
    public static final String RABBITMQ_EXCHANGE_SENDDOWN_EXCHANGE = "senddown.exchange";
    public static final String RABBITMQ_EXCHANGE_SENDDOWN_TASK = "senddown.task";
    public static final String DEFAULT_QINIU_URL = "https://portal.qiniu.com/kodo/bucket/resource-v2?bucketName=metalfilamentinspection";
    public static final String HUAWEI_SENDDOWN_SURFACE_DATA_TOPIC = "Surface_data.task";
    public static final String HUAWEI_DEVICE_ID = "6a32861318855b39c5258e08_test";


}
