package com.matchaworld.backend.service.ai;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matchaworld.backend.dto.response.ai.AiResult;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AIService {

    private final OpenAiChatModel openAiChatModel;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ✅ Jackson 객체

    @Autowired
    public AIService(OpenAiChatModel openAiChatModel) {
        this.openAiChatModel = openAiChatModel;
    }

    /**
     * ✅ 프론트엔드용 안정 버전 (NDJSON → 단일 JSON 문자열)
     * - 스트리밍 중 null/Map chunk 모두 안전 필터링
     * - 최종 결과를 하나의 문자열로 병합하여 반환
     */
    public Mono<String> imageAnalysis(String question, String contentType, byte[] bytes) {

        SystemMessage systemMessage = SystemMessage.builder()
            .text("""
                당신은 친환경 행동을 판별하는 이미지 분석 전문가입니다.
                사용자가 업로드한 이미지를 분석하여,
                '텀블러', '머그컵', '일회용컵' 중 하나로 판단하세요.
                다른 설명 없이 정확히 그 세 단어 중 하나만 결과로 출력하세요.
            """)
            .build();

        Media imageMedia = Media.builder()
            .mimeType(MimeType.valueOf(contentType))
            .data(new ByteArrayResource(bytes))
            .build();

        UserMessage userMessage = UserMessage.builder()
            .text(question)
            .media(imageMedia)
            .build();

        Prompt prompt = Prompt.builder()
            .messages(systemMessage, userMessage)
            .build();

        log.info("📤 AI 요청 전송 (Model: gpt-4o, ContentType: {})", contentType);

        return openAiChatModel.stream(prompt)
            .flatMap(resp -> {
                try {
                    if (resp == null || resp.getResult() == null || resp.getResult().getOutput() == null)
                        return Flux.empty();

                    var output = resp.getResult().getOutput();
                    String text = null;

                    try {
                        text = output.getText();
                    } catch (Exception e) {
                        text = String.valueOf(output);
                    }

                    if (text == null || text.isBlank() || "null".equalsIgnoreCase(text))
                        return Flux.empty();

                    return Flux.just(text.trim());
                } catch (Exception e) {
                    log.error("⚠️ 스트리밍 변환 오류: {}", e.getMessage());
                    return Flux.empty();
                }
            })
            .filter(chunk -> chunk != null && !chunk.isBlank())
            .collectList() // ✅ 모든 chunk 모아서
            .map(chunks -> {
                String result = String.join("", chunks).trim();
                log.info("✅ 최종 분석 결과: {}", result);
                return result.isEmpty() ? "결과를 해석할 수 없습니다." : result;
            })
            .onErrorResume(e -> {
                log.error("❌ 전체 스트림 오류: {}", e.getMessage());
                return Mono.just("AI 분석 중 오류가 발생했습니다.");
            });
    }

    // 아래는 기존 비동기 메서드를 그대로 두고, 동기식 버전 추가 코드
    // 수정 이유: 기존 imageAnalysis는 Mono를 반환하므로 Controller에서 비동기 흐름과 맞지 않음.
    //            Controller에서 동기적으로 결과를 받아 퀘스트 완료 처리를 하기 위해 동기 버전을 추가함.
    public String imageAnalysisBlocking(String question, String contentType, byte[] bytes) {

        SystemMessage systemMessage = SystemMessage.builder()
            .text("""
                당신은 친환경 행동을 판별하는 이미지 분석 전문가입니다.
                사용자가 업로드한 이미지를 분석하여,
                '텀블러', '머그컵', '일회용컵' 중 하나로 판단하세요.
                다른 설명 없이 정확히 그 세 단어 중 하나만 결과로 출력하세요.
                셋 중 하나로 판별할 수 없을 경우, 다른 설명 없이 '모두 아님'으로 결과를 출력하세요. 띄어쓰기를 준수하세요.
            """)
            .build();

        Media imageMedia = Media.builder()
            .mimeType(MimeType.valueOf(contentType))
            .data(new ByteArrayResource(bytes))
            .build();

        UserMessage userMessage = UserMessage.builder()
            .text(question)
            .media(imageMedia)
            .build();

        Prompt prompt = Prompt.builder()
            .messages(systemMessage, userMessage)
            .build();

        log.info("📤 [AI 요청 전송 - 동기 처리 모드]");

        try {
            // 수정 이유: 기존 스트리밍 처리 코드를 그대로 사용하되, block()을 통해 동기적으로 결과를 반환
            return openAiChatModel.stream(prompt)
                .flatMap(resp -> {
                    try {
                        if (resp == null || resp.getResult() == null || resp.getResult().getOutput() == null)
                            return Flux.empty();

                        var output = resp.getResult().getOutput();
                        String text = null;

                        try {
                            text = output.getText();
                        } catch (Exception e) {
                            text = String.valueOf(output);
                        }

                        if (text == null || text.isBlank() || "null".equalsIgnoreCase(text))
                            return Flux.empty();

                        return Flux.just(text.trim());
                    } catch (Exception e) {
                        log.error("스트리밍 변환 오류: {}", e.getMessage());
                        return Flux.empty();
                    }
                })
                .filter(chunk -> chunk != null && !chunk.isBlank())
                .collectList()
                .map(chunks -> {
                    String result = String.join("", chunks).trim();
                    log.info("최종 분석 결과(동기): {}", result);
                    return result.isEmpty() ? "결과를 해석할 수 없습니다." : result;
                })
                .onErrorResume(e -> {
                    log.error("전체 스트림 오류(동기): {}", e.getMessage());
                    return Mono.just("AI 분석 중 오류가 발생했습니다.");
                })
                .block(); // 수정 이유: 비동기 Mono를 동기적으로 변환하기 위해 block() 호출
        } catch (Exception e) {
            log.error("동기 분석 중 예외 발생: {}", e.getMessage());
            return "AI 분석 중 오류가 발생했습니다.";
        }
    }

   /**
     * ✅ 텍스트 ESG 기록 분석 (Jackson 기반)
     * - Markdown(````json ... ````) 형태 응답도 안전하게 처리
     * - 동기식 (기록 저장, 퀘스트 완료 등 트랜잭션 내에서 안전)
     * - 항상 rawText(사용자 원문)를 포함하도록 보장
     */
    public AiResult analyzeTextJackson(String content) {
        SystemMessage systemMessage = SystemMessage.builder()
            .text("""
                당신은 ESG 활동 분류 전문가입니다.
                사용자가 작성한 문장을 분석하여 ESG 카테고리를 판별하고
                관련 키워드를 추출하세요.
                출력은 반드시 JSON 형식으로:
                {"category": "E", "keywords": ["텀블러","도시락"], "confidence": 0.93}
            """)
            .build();

        UserMessage userMessage = UserMessage.builder()
            .text(content)
            .build();

        log.info("📤 [AI 요청 전송 - 텍스트 분석(Jackson)], content={}", content);
        Prompt prompt = Prompt.builder()
            .messages(systemMessage, userMessage)
            .build();

        log.info("📤 [AI 요청 전송 - 텍스트 분석(Jackson)]");

        try {
            // 1️⃣ AI 응답 수신
            String response = openAiChatModel.call(prompt).getResult().getOutput().getText();
            log.info("✅ 텍스트 분석 결과(Jackson): {}", response);

            // 2️⃣ Markdown 백틱(```json ... ````) 제거
            String cleanJson = response
                    .replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();

            // 3️⃣ ObjectMapper로 JSON 파싱
            AiResult result = objectMapper.readValue(cleanJson, AiResult.class);

            // ✅ 4️⃣ 원문(content)을 rawText에 반드시 세팅
            result.setRawText(content);

            // 5️⃣ 결과 로그
            log.info("🧠 [AI 분석 파싱 성공] category={}, keywords={}, confidence={}, rawText={}",
                    result.getCategory(), result.getKeywords(), result.getConfidence(), result.getRawText());

            return result;

        } catch (Exception e) {
            log.error("❌ 텍스트 분석(Jackson) 오류: {}", e.getMessage());
            // ✅ 6️⃣ 예외 시 기본값 반환 (E 카테고리, 키워드 없음, 원문 포함)
            return new AiResult("E", java.util.List.of(), 0.5, content);
        }
    }
}
