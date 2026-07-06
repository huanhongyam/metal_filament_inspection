package com.kunpeng.metal_filament_inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.kunpeng.metal_filament_inspection.domain.dto.*;
import com.kunpeng.metal_filament_inspection.domain.entity.WireMaterial;
import com.kunpeng.metal_filament_inspection.domain.vo.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IWireMaterialService extends IService<WireMaterial> {
    List<WireMaterialDTO> listQueryPage( Integer limit, WireMaterialQueryDTO wireMaterialDTO);

    Result<Boolean> updateByBatchNumber(@Valid WireMaterialDTO wireMaterialDTO, @NotBlank(message = "批次号不能为空") Long batchNumber);

    Result<Boolean> deleteById(@NotBlank(message = "批次号不能为空") Long batchNumber);

    PageDTO listPage(Integer current);

    PageDTO listPageWithFilter(Integer current, String deviceId, String scenarioCode, Long batchNo,
                                String productionMachine, String responsiblePerson, String modelEvaluationResult,
                                LocalDate startDate, LocalDate endDate);

    Result<Boolean> saveWireMaterial(WireMaterialSaveDTO wireMaterialSaveDTO);

    Result<Boolean> checkByBatchNoWithRollNo(Long batchNo, Long rollNo);

    List<WireMaterialDTO> listUnevaluated(Integer hours, Integer limit);

    Result<Boolean> updateEvaluation(Long batchNumber, @Valid WireMaterialUpdateDTO dto);

    Result<Integer> updateEvaluationBatch(@Valid List<WireMaterialUpdateDTO> dtoList);
    
    List<WireMaterialPassRateVO> getPassRateByYearMonth(String yearMonth);

    Long queryByBatchNoWithRollNo(Long batchNo, Long rollNo);

    Result<WireInfoWithDetectionInfo> queryWireMaterialByBatchNoWithRollNo(WireDTO wireDTO);

    Result<WireMaterialUpdateDTO> triggerEvaluation(Long batchNumber);

    EarlyWarningStatsDTO getEarlyWarningStats(Integer hours);

    Result<EarlyWarningVO> triggerEarlyWarning(Integer hours);

    WireMaterialPhysicalVO QueryWithBatchNoAvg(Long batchNo);

    List<WireMaterialPhysicalVO> QueryWithBatchNo(Long batchNo);

    Result<Page<WireMaterialPhysicalVO>> QueryListWithBatchNoAvg(Integer current) throws JsonProcessingException;
}
