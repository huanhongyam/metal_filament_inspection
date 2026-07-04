package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.stream.CollectorUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kunpeng.metal_filament_inspection.domain.dto.*;import com.kunpeng.metal_filament_inspection.domain.entity.DetectionBatch;
import com.kunpeng.metal_filament_inspection.domain.entity.User;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.domain.vo.*;
import com.kunpeng.metal_filament_inspection.mapper.DetectionBatchMapper;
import com.kunpeng.metal_filament_inspection.mapper.WireMaterialMapper;
import com.kunpeng.metal_filament_inspection.service.IUserService;
import com.kunpeng.metal_filament_inspection.service.IWireMaterialService;
import com.kunpeng.metal_filament_inspection.utils.DetectionSummary;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import com.kunpeng.metal_filament_inspection.utils.UserHolder;
import com.kunpeng.metal_filament_inspection.utils.WireMaterialStats;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class WireMaterialServiceImpl extends ServiceImpl<WireMaterialMapper, WireMaterial> implements IWireMaterialService {
    @Autowired
    private IUserService userService;
    @Autowired
    private WireMaterialMapper wireMaterialMapper;
    @Autowired
    private DetectionBatchMapper detectionBatchMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Resource
    private WireMaterialStats wireMaterialStats;
    @Override
    public List<WireMaterialDTO> listQueryPage(Integer limit, WireMaterialQueryDTO queryDTO) {
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        // 精确匹配
        if (StringUtils.hasText(queryDTO.getDeviceId())) {
            wrapper.eq(WireMaterial::getDeviceId, queryDTO.getDeviceId());
        }
        if (StringUtils.hasText(queryDTO.getManufacturer())) {
            wrapper.eq(WireMaterial::getManufacturer, queryDTO.getManufacturer());
        }
        if (StringUtils.hasText(queryDTO.getResponsiblePerson())) {
            wrapper.eq(WireMaterial::getResponsiblePerson, queryDTO.getResponsiblePerson());
        }
        if (StringUtils.hasText(queryDTO.getProcessType())) {
            wrapper.eq(WireMaterial::getProcessType, queryDTO.getProcessType());
        }
        if (StringUtils.hasText(queryDTO.getScenarioCode())) {
            wrapper.eq(WireMaterial::getScenarioCode, queryDTO.getScenarioCode());
        }
        // 数值等值条件
        if (queryDTO.getBatchNumber() != null) {
            wrapper.eq(WireMaterial::getBatchNumber, queryDTO.getBatchNumber());
        }
        if (queryDTO.getBatchNo() != null) {
            wrapper.eq(WireMaterial::getBatchNo, queryDTO.getBatchNo());
        }
        // 查询创建时间前7天到指定时间
        if (queryDTO.getCreateTime() != null) {
            LocalDateTime end = queryDTO.getCreateTime();
            LocalDateTime start = end.minusDays(7);
            wrapper.between(WireMaterial::getCreateTime, start, end);
        }
        // 限制条数（使用 last 拼接 LIMIT）
        if (limit > 1000) limit = 1000;
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }
        List<WireMaterial> entities = baseMapper.selectList(wrapper);
        // 转换为 DTO
        return entities.stream()
                .map(entity -> {
                    WireMaterialDTO dto = new WireMaterialDTO();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public Result<Boolean> updateByBatchNumber(WireMaterialDTO wireMaterialDTO, Long batchNumber) {
        User user = userService.getById(UserHolder.getUserId());
        String userName = user.getUserName();
        log.info("管理员{}更新线材信息，批次号：{}", userName, batchNumber);
        UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
        updateWrapper.eq("batch_number", batchNumber);
        WireMaterial wireMaterial = BeanUtil.copyProperties(wireMaterialDTO, WireMaterial.class);
        boolean isUpdate = update(wireMaterial, updateWrapper);
        return Result.success(isUpdate);
    }
    @Override
    public Result<Boolean> deleteById(Long batchNumber) {
        Long userId = UserHolder.getUserId();
        User user = userService.getById(userId);
        String userName = user.getUserName();
        log.info("管理员{}删除线材记录，批次号：{}", userName, batchNumber);
        return Result.success(removeById(batchNumber));
    }
    @Override
    public PageDTO<WireMaterialVO> listPage(Integer current) {
        Page<WireMaterial> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(WireMaterial::getCreateTime);
        IPage<WireMaterial> pageResult = page(page, wrapper);
        // 转换当前页数据为 DTO
        List<WireMaterialVO> dtoList = pageResult.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, WireMaterialVO.class))
                .collect(Collectors.toList());
        PageDTO<WireMaterialVO> pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage((int) pageResult.getCurrent());
        pageDTO.setPageSize((int) pageResult.getSize());
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setRecords(dtoList);
        return pageDTO;
    }

    @Override
    public PageDTO<WireMaterialVO> listPageWithFilter(Integer current, String deviceId, String scenarioCode,
                                                       Long batchNo, String manufacturer, String responsiblePerson,
                                                       String modelEvaluationResult, LocalDate startDate, LocalDate endDate) {
        Page<WireMaterial> page = new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE);
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(deviceId)) {
            wrapper.eq(WireMaterial::getDeviceId, deviceId);
        }
        if (StringUtils.hasText(scenarioCode)) {
            wrapper.eq(WireMaterial::getScenarioCode, scenarioCode);
        }
        if (batchNo != null) {
            wrapper.eq(WireMaterial::getBatchNo, batchNo);
        }
        if (StringUtils.hasText(manufacturer)) {
            wrapper.eq(WireMaterial::getManufacturer, manufacturer);
        }
        if (StringUtils.hasText(responsiblePerson)) {
            wrapper.eq(WireMaterial::getResponsiblePerson, responsiblePerson);
        }
        if (StringUtils.hasText(modelEvaluationResult)) {
            try {
                wrapper.eq(WireMaterial::getModelEvaluationResult,
                        WireMaterial.EvaluationResult.valueOf(modelEvaluationResult));
            } catch (IllegalArgumentException e) {
                log.warn("无效的评估结果值：{}，忽略此条件", modelEvaluationResult);
            }
        }
        if (startDate != null) {
            wrapper.ge(WireMaterial::getCreateTime, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(WireMaterial::getCreateTime, endDate.atTime(23, 59, 59));
        }
        wrapper.orderByDesc(WireMaterial::getCreateTime);

        IPage<WireMaterial> pageResult = page(page, wrapper);
        List<WireMaterialVO> dtoList = pageResult.getRecords().stream()
                .map(item -> BeanUtil.copyProperties(item, WireMaterialVO.class))
                .collect(Collectors.toList());
        PageDTO<WireMaterialVO> pageDTO = new PageDTO<>();
        pageDTO.setCurrentPage((int) pageResult.getCurrent());
        pageDTO.setPageSize((int) pageResult.getSize());
        pageDTO.setTotal(pageResult.getTotal());
        pageDTO.setRecords(dtoList);
        return pageDTO;
    }

    @Override
    public Result<Boolean> saveWireMaterial(WireMaterialSaveDTO wireMaterialSaveDTO) {
        // 保存线材检测记录并返回结果
        WireMaterial wireMaterial = BeanUtil.copyProperties(wireMaterialSaveDTO, WireMaterial.class);
        boolean isSuccess = save(wireMaterial);
        if (isSuccess) {
            log.info("创建线材检测记录成功，设备ID：{}",
                    wireMaterial.getDeviceId());
            return Result.success();
        } else {
            log.error("创建线材检测记录成功，设备ID：{}",wireMaterial.getDeviceId());
            return Result.error("创建设备失败，请稍后重试");
        }
    }

    @Override
    public Result<Boolean> checkByBatchNoWithRollNo(Long batchNo, Long rollNo) {
        boolean exists = query().eq("batch_no", batchNo)
                .eq("roll_no", rollNo)
                .exists();
        return Result.success(exists);
    }
    @Override
    public List<WireMaterialDTO> listUnevaluated(Integer hours, Integer limit) {
        if (hours == null || hours <= 0) {
            hours = 24;
        }
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WireMaterial::getModelEvaluationResult, WireMaterial.EvaluationResult.UNKNOWN)
                .ge(WireMaterial::getCreateTime, LocalDateTime.now().minusHours(hours))
                .orderByDesc(WireMaterial::getCreateTime);
        if (limit != null && limit > 0) {
            if (limit > 1000) limit = 1000;
            wrapper.last("LIMIT " + limit);
        }
        List<WireMaterial> entities = list(wrapper);
        return entities.stream()
                .map(entity -> {
                    WireMaterialDTO dto = new WireMaterialDTO();
                    BeanUtils.copyProperties(entity, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Override
    public Result<Boolean> updateEvaluation(Long batchNumber, WireMaterialUpdateDTO dto) {
        log.info("Agent 更新评估结果 — 批次号：{}，结论：{}，置信度：{}",
                batchNumber, dto.getModelEvaluationResult(), dto.getModelConfidence());

        UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
        updateWrapper.eq("batch_number", batchNumber);

        WireMaterial entity = new WireMaterial();
        entity.setEvaluationMessage(dto.getEvaluationMessage());
        entity.setModelConfidence(dto.getModelConfidence());

        // String → Enum 转换，容错处理
        if (StringUtils.hasText(dto.getModelEvaluationResult())) {
            try {
                entity.setModelEvaluationResult(
                        WireMaterial.EvaluationResult.valueOf(dto.getModelEvaluationResult()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的评估结果值：{}，跳过更新", dto.getModelEvaluationResult());
                return Result.error("modelEvaluationResult 取值必须为 PASS / FAIL / UNKNOWN");
            }
        }

        boolean updated = update(entity, updateWrapper);
        if (!updated) {
            log.warn("更新评估结果失败，批次号 {} 不存在", batchNumber);
            return Result.error("批次号不存在");
        }
        return Result.success(true);
    }
    @Override
    public Result<Integer> updateEvaluationBatch(List<WireMaterialUpdateDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return Result.error("评估列表不能为空");
        }
        int successCount = 0;
        for (WireMaterialUpdateDTO dto : dtoList) {
            UpdateWrapper<WireMaterial> updateWrapper = Wrappers.update();
            updateWrapper.eq("batch_number", dto.getBatchNumber());

            WireMaterial entity = new WireMaterial();
            entity.setEvaluationMessage(dto.getEvaluationMessage());
            entity.setModelConfidence(dto.getModelConfidence());

            if (StringUtils.hasText(dto.getModelEvaluationResult())) {
                try {
                    entity.setModelEvaluationResult(
                            WireMaterial.EvaluationResult.valueOf(dto.getModelEvaluationResult()));
                } catch (IllegalArgumentException e) {
                    log.warn("批次 {} 评估结果值无效：{}，跳过", dto.getBatchNumber(), dto.getModelEvaluationResult());
                    continue;
                }
            }

            boolean updated = update(entity, updateWrapper);
            if (updated) {
                successCount++;
            } else {
                log.warn("批次 {} 不存在，跳过", dto.getBatchNumber());
            }
        }
        log.info("Agent 批量评估完成：{}/{} 条成功", successCount, dtoList.size());
        return Result.success(successCount);
    }

    public List<WireMaterialPassRateVO> getPassRateByYearMonth(String yearMonth) {
        // 1. 解析年月
        YearMonth ym = YearMonth.parse(yearMonth);
        LocalDateTime start = ym.atDay(1).atStartOfDay();
        LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59);
        // 2. 查询该月所有记录
        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(WireMaterial::getCreateTime, WireMaterial::getModelEvaluationResult)
                .between(WireMaterial::getCreateTime, start, end);
        List<WireMaterial> records = wireMaterialMapper.selectList(wrapper);

        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        // 3. 用两个 Map 分别统计总数量和通过数量
        Map<LocalDate, Long> totalMap = new LinkedHashMap<>();
        Map<LocalDate, Long> passMap = new LinkedHashMap<>();
        for (WireMaterial record : records) {
            LocalDate day = record.getCreateTime().toLocalDate();
            // 累加总数
            totalMap.put(day, totalMap.getOrDefault(day, 0L) + 1);
            // 累加通过数（直接比较枚举常量）
            if (record.getModelEvaluationResult() == WireMaterial.EvaluationResult.PASS) {
                passMap.put(day, passMap.getOrDefault(day, 0L) + 1);
            }
        }
        // 4. 组装成 VO 列表
        List<WireMaterialPassRateVO> result = new ArrayList<>();
        for (Map.Entry<LocalDate, Long> entry : totalMap.entrySet()) {
            LocalDate day = entry.getKey();
            Long total = entry.getValue();
            Long pass = passMap.getOrDefault(day, 0L);
            Long fail = total - pass;

            BigDecimal rate = BigDecimal.ZERO;
            if (total > 0) {
                rate = BigDecimal.valueOf(pass)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
            }
            WireMaterialPassRateVO vo = new WireMaterialPassRateVO();
            vo.setDay(day);
            vo.setPassRate(rate);
            vo.setPassCount(pass);
            vo.setFailCount(fail);
            result.add(vo);
        }
        result.sort(Comparator.comparing(WireMaterialPassRateVO::getDay));
        return result;
    }

    @Override
    public Long queryByBatchNoWithRollNo(Long batchNo, Long rollNo) {
        Long batchNumber = query().eq("batch_no", batchNo).eq("roll_no", rollNo).one().getBatchNumber();
        return batchNumber;
    }

    @Override
    public Result<WireInfoWithDetectionInfo> queryWireMaterialByBatchNoWithRollNo(WireDTO wireDTO) {
        // 1. 查询线材基础信息
        WireMaterial wireMaterial = query()
                .eq("batch_no", wireDTO.getBatchNo())
                .eq("roll_no", wireDTO.getRollNo())
                .one();
        if (wireMaterial == null) {
            return Result.error("未找到对应的线材记录");
        }

        // 2. 查询该批次的所有表面缺陷数据
        LambdaQueryWrapper<DetectionBatch> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DetectionBatch::getBatchNumber, wireMaterial.getBatchNumber());
        List<DetectionBatch> detectionBatches = detectionBatchMapper.selectList(wrapper);

        // 3. 转换 DTO 并聚合
        List<DetectionBatchDTO> dtoList = detectionBatches.stream()
                .map(b -> BeanUtil.copyProperties(b, DetectionBatchDTO.class))
                .collect(Collectors.toList());
        DetectionBatchSummaryDTO summary = DetectionSummary.summarizeByBatchLoop(dtoList);

        // 4. 拼接返回
        WireInfoWithDetectionInfo.WireInfoWithDetectionInfoBuilder builder = WireInfoWithDetectionInfo.builder()
                .batchNumber(wireMaterial.getBatchNumber())
                .deviceId(wireMaterial.getDeviceId())
                .diameter(wireMaterial.getDiameter())
                .resistance(wireMaterial.getResistance())
                .extensibility(wireMaterial.getExtensibility())
                .weight(wireMaterial.getWeight())
                .manufacturer(wireMaterial.getManufacturer())
                .responsiblePerson(wireMaterial.getResponsiblePerson())
                .processType(wireMaterial.getProcessType())
                .scenarioCode(wireMaterial.getScenarioCode())
                .batchNo(wireMaterial.getBatchNo())
                .rollNo(wireMaterial.getRollNo())
                .modelConfidence(wireMaterial.getModelConfidence())
                .modelEvaluationResult(wireMaterial.getModelEvaluationResult() != null
                        ? wireMaterial.getModelEvaluationResult().name()
                        : null)
                .createTime(wireMaterial.getCreateTime());

        // 5. 填充检测缺陷聚合数据
        if (summary != null) {
            builder.avgConfidence(summary.getAvgConfidence())
                    .scratchCount(summary.getScratchCount())
                    .blockDefectCount(summary.getBlockDefectCount())
                    .clusterDefectCount(summary.getClusterDefectCount())
                    .metalBurrCount(summary.getMetalBurrCount())
                    .scuffCount(summary.getScuffCount());
        }

        return Result.success(builder.build());
    }

    @Override
    public Result<WireMaterialUpdateDTO> triggerEvaluation(Long batchNumber) {
        // 1. 查询线材
        WireMaterial wm = query().eq("batch_number", batchNumber).one();
        if (wm == null) {
            return Result.error("批次号 " + batchNumber + " 不存在");
        }

        // 2. 调用 Python Agent4j 评估
        String url = SystemConstants.AGENT4J_URL + "/api/v1/evaluate/single";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<WireMaterial> request = new HttpEntity<>(wm, headers);

        log.info("触发单条评估 — 批次号：{}", batchNumber);
        String aiResponse;
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(response.getBody())) {
                log.error("Agent4j 评估异常 — status: {}", response.getStatusCode());
                return Result.error("AI 评估服务异常");
            }
            aiResponse = response.getBody();
        } catch (Exception e) {
            log.error("调用 Agent4j 评估失败", e);
            return Result.error("AI 评估服务不可用: " + e.getMessage());
        }

        // 3. 解析评估结果
        WireMaterialUpdateDTO dto;
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            int code = root.path("code").asInt(-1);
            if (code != 200) {
                return Result.error("AI 评估返回异常");
            }
            JsonNode data = root.path("data");
            dto = new WireMaterialUpdateDTO();
            dto.setBatchNumber(data.path("batchNumber").asLong());
            dto.setModelEvaluationResult(data.path("modelEvaluationResult").asText());
            dto.setModelConfidence(BigDecimal.valueOf(data.path("modelConfidence").asDouble()));
            dto.setEvaluationMessage(data.path("evaluationMessage").asText());
        } catch (Exception e) {
            log.error("解析评估结果失败", e);
            return Result.error("解析评估结果失败");
        }

        // 4. 回写数据库
        Result<Boolean> updateResult = updateEvaluation(batchNumber, dto);
        if (updateResult.getData() == null || !updateResult.getData()) {
            return Result.error("评估结果回写失败");
        }

        log.info("单条评估完成 — 批次号：{}，结论：{}", batchNumber, dto.getModelEvaluationResult());
        return Result.success(dto);
    }

    // 预警分析
    @Override
    public EarlyWarningStatsDTO getEarlyWarningStats(Integer hours) {
        if (hours == null || hours <= 0) hours = 24;
        LocalDateTime since = LocalDateTime.now().minusHours(hours);

        LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(WireMaterial::getManufacturer, WireMaterial::getResponsiblePerson,
                        WireMaterial::getDeviceId, WireMaterial::getModelEvaluationResult)
                .ge(WireMaterial::getCreateTime, since);
        List<WireMaterial> records = list(wrapper);

        int total = records.size();
        long failTotal = records.stream()
                .filter(r -> r.getModelEvaluationResult() == WireMaterial.EvaluationResult.FAIL)
                .count();

        BigDecimal overallRate = total > 0
                ? BigDecimal.valueOf(failTotal).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        EarlyWarningStatsDTO dto = new EarlyWarningStatsDTO();
        dto.setHoursBack(hours);
        dto.setTotalCount(total);
        dto.setFailCount((int) failTotal);
        dto.setOverallFailRate(overallRate);
        dto.setByManufacturer(aggregateByField(records, "manufacturer"));
        dto.setByResponsiblePerson(aggregateByField(records, "responsiblePerson"));
        dto.setByDevice(aggregateByField(records, "deviceId"));
        return dto;
    }

    private List<EarlyWarningStatsDTO.GroupStats> aggregateByField(List<WireMaterial> records, String field) {
        Map<String, long[]> map = new LinkedHashMap<>();
        for (WireMaterial r : records) {
            String key = switch (field) {
                case "manufacturer" -> r.getManufacturer();
                case "responsiblePerson" -> r.getResponsiblePerson();
                case "deviceId" -> r.getDeviceId();
                default -> null;
            };
            if (key == null || key.isEmpty()) key = "未填写";
            map.computeIfAbsent(key, k -> new long[2]);
            map.get(key)[0]++; // total
            if (r.getModelEvaluationResult() == WireMaterial.EvaluationResult.FAIL) {
                map.get(key)[1]++; // fail
            }
        }
        return map.entrySet().stream()
                .map(e -> {
                    EarlyWarningStatsDTO.GroupStats gs = new EarlyWarningStatsDTO.GroupStats();
                    gs.setName(e.getKey());
                    long tot = e.getValue()[0];
                    long fail = e.getValue()[1];
                    gs.setTotalCount(tot);
                    gs.setFailCount(fail);
                    gs.setFailRate(tot > 0
                            ? BigDecimal.valueOf(fail).multiply(BigDecimal.valueOf(100))
                                .divide(BigDecimal.valueOf(tot), 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO);
                    return gs;
                })
                .sorted((a, b) -> b.getFailRate().compareTo(a.getFailRate()))
                .collect(Collectors.toList());
    }

    @Override
    public Result<EarlyWarningVO> triggerEarlyWarning(Integer hours) {
        if (hours == null || hours <= 0) hours = 24;

        // 1. 本地聚合 stats → 构建结构化 top 3
        EarlyWarningStatsDTO stats = getEarlyWarningStats(hours);
        List<EarlyWarningVO.WarningItem> topManufacturers = buildTop3(stats.getByManufacturer());
        List<EarlyWarningVO.WarningItem> topResponsiblePersons = buildTop3(stats.getByResponsiblePerson());
        List<EarlyWarningVO.WarningItem> topDevices = buildTop3(stats.getByDevice());

        // 2. 调 Agent4j — 仅传入 hours，由 Python 侧系统提示词驱动分析
        String aiAnalysis = callEarlyWarningAgent(hours);

        // 3. 组装 VO
        EarlyWarningVO vo = new EarlyWarningVO();
        vo.setTopManufacturers(topManufacturers);
        vo.setTopResponsiblePersons(topResponsiblePersons);
        vo.setTopDevices(topDevices);
        vo.setAiAnalysis(aiAnalysis);
        vo.setAnalysisTime(LocalDateTime.now());

        return Result.success(vo);
    }

    @Override
    public WireMaterialPhysicalVO QueryWithBatchNoAvg(Long batchNo) {
        if (batchNo == null){
            LambdaQueryWrapper<WireMaterial> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByDesc(WireMaterial::getCreateTime)
                    .last("LIMIT 1");
            WireMaterial wireMaterial = baseMapper.selectOne(wrapper);
            Long batchNo1 = wireMaterial.getBatchNo();
            List<WireMaterial> rollList = query().eq("batch_no", batchNo1).list();
            rollList.forEach(item -> {
                wireMaterialStats.accept(item);
            });
            BigDecimal avgDiameter = wireMaterialStats.getAvgDiameter();
            BigDecimal avgResistance = wireMaterialStats.getAvgResistance();
            BigDecimal avgExtensibility = wireMaterialStats.getAvgExtensibility();
            BigDecimal avgWeight = wireMaterialStats.getAvgWeight();
            return WireMaterialPhysicalVO.builder()
                    .batchNo(batchNo1)
                    .diameter(avgDiameter)
                    .weight(avgWeight)
                    .resistance(avgResistance)
                    .extensibility(avgExtensibility)
                    .build();
        }else {
            List<WireMaterial> rollList = query().eq("batch_no", batchNo).list();
            rollList.forEach(item -> {
                wireMaterialStats.accept(item);
            });
            BigDecimal avgDiameter = wireMaterialStats.getAvgDiameter();
            BigDecimal avgResistance = wireMaterialStats.getAvgResistance();
            BigDecimal avgExtensibility = wireMaterialStats.getAvgExtensibility();
            BigDecimal avgWeight = wireMaterialStats.getAvgWeight();
            return WireMaterialPhysicalVO.builder()
                    .batchNo(batchNo)
                    .diameter(avgDiameter)
                    .weight(avgWeight)
                    .resistance(avgResistance)
                    .extensibility(avgExtensibility)
                    .build();
        }
    }

    @Override
    public List<WireMaterialPhysicalVO> QueryWithBatchNo(Long batchNo) {
        List<WireMaterial> rollList = query().eq("batch_no", batchNo).list().stream().limit(10).toList();
        List<WireMaterialPhysicalVO> wireMaterialPhysicalVOS = rollList.stream().map(item -> {
            return BeanUtil.copyProperties(item, WireMaterialPhysicalVO.class);
        }).collect(Collectors.toList());
        return wireMaterialPhysicalVOS;
    }

    private List<EarlyWarningVO.WarningItem> buildTop3(List<EarlyWarningStatsDTO.GroupStats> list) {
        if (list == null || list.isEmpty()) return List.of();
        return list.stream().limit(3).map(gs -> {
            EarlyWarningVO.WarningItem item = new EarlyWarningVO.WarningItem();
            item.setName(gs.getName());
            item.setTotalCount(gs.getTotalCount());
            item.setFailCount(gs.getFailCount());
            item.setFailRate(gs.getFailRate());
            return item;
        }).collect(Collectors.toList());
    }

    private String callEarlyWarningAgent(int hours) {
        String url = SystemConstants.AGENT4J_URL + "/api/v1/early-warning";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = Map.of("hours", hours);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("触发预警分析 — 回溯 {} 小时", hours);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            if (response.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(response.getBody())) {
                return "[AI 服务异常]";
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.path("code").asInt(-1) != 200) {
                return "[AI 服务错误: " + root.path("message").asText("未知") + "]";
            }
            String content = root.path("data").path("content").asText("");
            return StringUtils.hasText(content) ? content : "[AI 未返回有效分析]";
        } catch (Exception e) {
            log.error("预警分析 Agent4j 调用失败", e);
            return "[AI 服务不可用: " + e.getMessage() + "]";
        }
    }
}
