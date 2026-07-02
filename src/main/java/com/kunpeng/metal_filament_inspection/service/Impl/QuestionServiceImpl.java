package com.kunpeng.metal_filament_inspection.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kunpeng.metal_filament_inspection.domain.dto.QuestionAskDTO;
import com.kunpeng.metal_filament_inspection.domain.dto.Result;
import com.kunpeng.metal_filament_inspection.domain.entity.Question;
import com.kunpeng.metal_filament_inspection.domain.vo.QuestionVO;
import com.kunpeng.metal_filament_inspection.mapper.QuestionMapper;
import com.kunpeng.metal_filament_inspection.service.IQuestionService;
import com.kunpeng.metal_filament_inspection.utils.IdWorker;
import com.kunpeng.metal_filament_inspection.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements IQuestionService {

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Executor streamExecutor;

    // 用户发起提问
    @Override
    public Result<QuestionVO> askFromUser(Long userId, QuestionAskDTO dto) {
        String deviceId = dto.getDeviceId();
        String questionContent = dto.getQuestionContent();

        // 1. 生成 ID + 创建数据库记录（状态：未处理）
        Question question = createQuestionRecord(deviceId, questionContent, userId);

        // 2. 同步调用 Agent4j
        String aiResponse = callAgent4j(questionContent);

        // 3. 回写 AI 响应
        updateQuestionResponse(question.getId(), aiResponse);

        // 4. 组装返回
        question.setAiResponseContent(aiResponse);
        question.setResponseStatus(1);
        question.setResponseTime(LocalDateTime.now());

        QuestionVO vo = BeanUtil.copyProperties(question, QuestionVO.class);
        return Result.success(vo);
    }

    // 硬件 JMS 发起提问
    @Override
    public Result<QuestionVO> askFromDevice(String deviceId, String questionContent) {
        // 1. 生成 ID + 创建数据库记录（无 userId）
        Question question = createQuestionRecord(deviceId, questionContent, null);

        // 2. 同步调用 Agent4j
        String aiResponse = callAgent4j(questionContent);

        // 3. 回写 AI 响应
        updateQuestionResponse(question.getId(), aiResponse);

        // 4. 组装返回
        question.setAiResponseContent(aiResponse);
        question.setResponseStatus(1);
        question.setResponseTime(LocalDateTime.now());

        QuestionVO vo = BeanUtil.copyProperties(question, QuestionVO.class);
        return Result.success(vo);
    }

    // 分页查询
    @Override
    public List<QuestionVO> listPage(Integer current, String deviceId, Integer responseStatus) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(deviceId)) {
            wrapper.eq(Question::getDeviceId, deviceId);
        }
        if (responseStatus != null) {
            wrapper.eq(Question::getResponseStatus, responseStatus);
        }
        wrapper.orderByDesc(Question::getCreateTime);

        // 分页
        long total = count(wrapper);
        int offset = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        wrapper.last("LIMIT " + offset + ", " + SystemConstants.DEFAULT_PAGE_SIZE);

        return list(wrapper).stream()
                .map(q -> BeanUtil.copyProperties(q, QuestionVO.class))
                .collect(Collectors.toList());
    }

    /**
     * 创建 Question 数据库记录，返回带 ID 的实体
     */
    private Question createQuestionRecord(String deviceId, String questionContent, Long userId) {
        Long id = idWorker.generateId(SystemConstants.QUESTION_PREFIX);
        Question question = Question.builder()
                .id(id)
                .deviceId(deviceId)
                .questionContent(questionContent)
                .userId(userId)
                .responseStatus(0)
                .eventTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();
        save(question);
        log.info("问题记录已创建 — id: {}, deviceId: {}, userId: {}", id, deviceId, userId);
        return question;
    }

    /** 更新 Question 的 AI 响应 */
    private void updateQuestionResponse(Long questionId, String aiResponse) {
        Question update = new Question();
        update.setId(questionId);
        update.setAiResponseContent(aiResponse);
        update.setResponseStatus(1);
        update.setResponseTime(LocalDateTime.now());
        updateById(update);
        log.info("问题 {} 的 AI 响应已回写", questionId);
    }

    private String callAgent4j(String questionContent) {
        String url = SystemConstants.AGENT4J_URL + "/api/v1/chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new java.util.HashMap<>();
        body.put("message", questionContent);
        body.put("stream", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        log.info("Agent4j 请求 — url: {}, message: {}",
                url, questionContent.length() > 100
                        ? questionContent.substring(0, 97) + "..."
                        : questionContent);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK || !StringUtils.hasText(response.getBody())) {
                log.error("Agent4j HTTP 异常 — status: {}, body: {}", response.getStatusCode(), response.getBody());
                return "[AI 服务异常，HTTP " + response.getStatusCodeValue() + "]";
            }

            JsonNode root = objectMapper.readTree(response.getBody());

            int code = root.path("code").asInt(-1);
            if (code != 200) {
                String msg = root.path("message").asText("未知错误");
                log.error("Agent4j 业务错误 — code: {}, message: {}", code, msg);
                return "[AI 服务错误: " + msg + "]";
            }

            String content = root.path("data").path("content").asText("");
            if (!StringUtils.hasText(content)) {
                log.warn("Agent4j 返回 content 为空");
                return "[AI 未返回有效内容]";
            }

            log.info("Agent4j 响应成功 — 内容长度: {}", content.length());
            return content;

        } catch (Exception e) {
            log.error("调用 Agent4j 失败", e);
            return "[AI 服务不可用: " + e.getMessage() + "]";
        }
    }

    @Override
    public SseEmitter askStream(Long userId, QuestionAskDTO dto) {
        // 1. 创建数据库记录
        Question question = createQuestionRecord(dto.getDeviceId(), dto.getQuestionContent(), userId);

        // 2. 创建 SseEmitter（5 分钟超时）
        SseEmitter emitter = new SseEmitter(300_000L);

        // 3. 异步执行流式调用
        CompletableFuture.runAsync(() -> {
            StringBuilder fullResponse = new StringBuilder();
            try {
                String url = SystemConstants.AGENT4J_URL + "/api/v1/chat/stream";

                Map<String, Object> body = Map.of(
                        "message", dto.getQuestionContent(),
                        "stream", true
                );

                restTemplate.execute(url, HttpMethod.POST,
                        request -> {
                            request.getHeaders().setContentType(MediaType.APPLICATION_JSON);
                            request.getBody().write(objectMapper.writeValueAsBytes(body));
                        },
                        (org.springframework.web.client.ResponseExtractor<Void>) response -> {
                            try (BufferedReader reader = new BufferedReader(
                                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (line.startsWith("data: ")) {
                                        String jsonStr = line.substring(6);
                                        JsonNode node = objectMapper.readTree(jsonStr);

                                        if (node.path("done").asBoolean(false)) {
                                            break;
                                        }

                                        String content = node.path("content").asText("");
                                        if (!content.isEmpty()) {
                                            fullResponse.append(content);
                                            emitter.send(SseEmitter.event()
                                                    .name("message")
                                                    .data(Map.of("content", content)));
                                        }
                                    }
                                }
                            }
                            return null;
                        }
                );

                // 4. 发送完成事件
                emitter.send(SseEmitter.event()
                        .name("done")
                        .data(Map.of("content", "", "done", true)));
                emitter.complete();

                // 5. 回写数据库
                updateQuestionResponse(question.getId(), fullResponse.toString());

            } catch (Exception e) {
                log.error("流式问答失败", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(Map.of("error", e.getMessage())));
                } catch (Exception ex) {
                    log.error("发送错误事件失败", ex);
                }
                emitter.completeWithError(e);
                updateQuestionResponse(question.getId(), "[AI 服务不可用: " + e.getMessage() + "]");
            }
        }, streamExecutor);

        return emitter;
    }
}
