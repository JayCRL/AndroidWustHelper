package com.example.wustchat.service;

import com.example.wustchat.dto.ChatResponse;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
public class RagQueryService {

    @Value("${rag.config-file:./rag-config.properties}")
    private String configFile;

    @Value("${rag.retrieval.max-results:4}")
    private Integer maxResults;

    @Value("${rag.retrieval.min-score:0.6}")
    private Double minScore;

    @Value("${rag.deepseek.base-url}")
    private String deepSeekBaseUrl;

    @Value("${rag.deepseek.api-key:}")
    private String deepSeekApiKey;

    @Value("${rag.deepseek.model:deepseek-chat}")
    private String deepSeekModel;

    private final CorpusService corpusService;
    private final ChatCacheService cacheService;
    private volatile OpenAiChatModel chatModel;

    public RagQueryService(CorpusService corpusService, ChatCacheService cacheService) {
        this.corpusService = corpusService;
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        this.deepSeekApiKey = loadApiKeyFromFile(this.deepSeekApiKey);
    }

    public ChatResponse ask(String question) {
        String guessedTag = guessTag(question);
        String cacheKey = cacheService.buildQaKey(question, guessedTag, maxResults);
        ChatResponse cached = (ChatResponse) cacheService.get(cacheKey);
        if (cached != null) {
            cached.setCacheHit(true);
            return cached;
        }

        List<EmbeddingMatch<TextSegment>> matches = searchMatches(question, guessedTag);
        if (matches.isEmpty()) {
            return buildNoKnowledgeResponse(question, guessedTag, cacheKey);
        }

        if (deepSeekApiKey == null || deepSeekApiKey.isBlank()) {
            throw new IllegalStateException("DeepSeek API Key 未配置");
        }
        if (chatModel == null) {
            synchronized (this) {
                if (chatModel == null) chatModel = buildChatModel(deepSeekApiKey);
            }
        }

        boolean hasStudentSource = matches.stream().anyMatch(m -> "student".equalsIgnoreCase(m.embedded().metadata().getString("uploaderType")));
        String context = buildCompactContext(matches);
        String prompt = buildAnswerPrompt(question, guessedTag, context, hasStudentSource);

        ChatRequest chatRequest = ChatRequest.builder().messages(UserMessage.from(prompt)).build();
        String answer = chatModel.chat(chatRequest).aiMessage().text();
        if (hasStudentSource) {
            answer = "以下内容参考了学生上传并经审核通过的语料，信息可能存在偏差，建议你再结合官方通知或老师要求确认。\n\n" + answer;
        }

        ChatResponse response = new ChatResponse();
        response.setQuestion(question);
        response.setMatchedTag(resolveMatchedTag(matches, guessedTag));
        response.setAnswer(answer);
        response.setHasStudentSource(hasStudentSource);
        response.setCacheHit(false);

        cacheService.setQa(cacheKey, response);
        return response;
    }

    private List<EmbeddingMatch<TextSegment>> searchMatches(String question, String tag) {
        var questionEmbedding = corpusService.getEmbeddingModel().embed(question).content();
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(questionEmbedding)
                .maxResults(Math.max(maxResults * 3, 10))
                .minScore(minScore)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = corpusService.getEmbeddingStore().search(searchRequest);
        List<EmbeddingMatch<TextSegment>> all = searchResult.matches();
        if (all == null || all.isEmpty()) return new ArrayList<>();

        List<EmbeddingMatch<TextSegment>> tagFiltered = all.stream()
                .filter(match -> matchesTag(match, tag))
                .sorted(Comparator.comparingDouble(EmbeddingMatch<TextSegment>::score).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());

        if (!tagFiltered.isEmpty()) {
            return tagFiltered;
        }

        return all.stream()
                .sorted(Comparator.comparingDouble(EmbeddingMatch<TextSegment>::score).reversed())
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    private boolean matchesTag(EmbeddingMatch<TextSegment> match, String tag) {
        if (tag == null || tag.isBlank() || "通用".equals(tag)) {
            return false;
        }
        String segmentTag = match.embedded().metadata().getString("tag");
        if (segmentTag == null || segmentTag.isBlank()) {
            return false;
        }
        List<String> questionKeywords = corpusService.splitTagKeywords(tag);
        List<String> segmentKeywords = corpusService.splitTagKeywords(segmentTag);
        for (String questionKeyword : questionKeywords) {
            String normalizedQuestionKeyword = normalizeKeyword(questionKeyword);
            if (normalizedQuestionKeyword.length() < 2) {
                continue;
            }
            for (String segmentKeyword : segmentKeywords) {
                if (normalizedQuestionKeyword.equals(normalizeKeyword(segmentKeyword))) {
                    return true;
                }
            }
        }
        return false;
    }

    private String normalizeKeyword(String keyword) {
        return keyword == null ? "" : keyword.trim().toLowerCase();
    }

    private String resolveMatchedTag(List<EmbeddingMatch<TextSegment>> matches, String guessedTag) {
        if (matches == null || matches.isEmpty()) {
            return guessedTag;
        }
        String matchedTag = matches.get(0).embedded().metadata().getString("tag");
        return matchedTag == null || matchedTag.isBlank() ? guessedTag : matchedTag;
    }

    private ChatResponse buildNoKnowledgeResponse(String question, String guessedTag, String cacheKey) {
        ChatResponse response = new ChatResponse();
        response.setQuestion(question);
        response.setMatchedTag(guessedTag);
        response.setAnswer("当前知识库中没有检索到与你问题直接相关的内容。你可以换个更具体的问法，或通过学生投稿补充这类信息。");
        response.setHasStudentSource(false);
        response.setCacheHit(false);
        cacheService.setQa(cacheKey, response);
        return response;
    }

    private String buildCompactContext(List<EmbeddingMatch<TextSegment>> matches) {
        if (matches.isEmpty()) return "无";
        return matches.stream()
                .map(match -> {
                    TextSegment segment = match.embedded();
                    String source = segment.metadata().getString("source");
                    String tag = segment.metadata().getString("tag");
                    String uploaderType = segment.metadata().getString("uploaderType");
                    String prefix = "student".equalsIgnoreCase(uploaderType) ? "[学生投稿] " : "";
                    return prefix + "[" + tag + "/" + source + "] " + shorten(segment.text(), 220);
                })
                .collect(Collectors.joining("\n"));
    }

    private String buildAnswerPrompt(String question, String tag, String context, boolean hasStudentSource) {
        String studentHint = hasStudentSource ? "参考知识里含有学生上传并审核通过的内容。回答时自然提醒用户这部分信息仅供参考，语气温和一点。" : "";
        return String.format("""
                你是一个中文校园问答助手。请严格根据提供的知识作答，不要编造知识库中没有的信息。
                回答要求：
                1. 直接回答，不要输出“分类”“标签”“命中缓存”等提示。
                2. 语气自然、有人情味，像认真帮忙的学长学姐或老师助理。
                3. 优先给出明确结论；如果知识里本身带有“以通知为准”“建议咨询老师/学院”，就自然保留这种提醒。
                4. 不要机械复述参考知识原文，尽量整理后再回答。
                %s
                参考知识：
                %s
                用户问题：%s
                """, studentHint, context, question);
    }

    private String guessTag(String question) {
        List<String> tags = corpusService.getTags();
        if (tags.isEmpty()) return "通用";
        String lowerQuestion = question == null ? "" : question.toLowerCase();
        String bestTag = "通用";
        int bestScore = -1;
        for (String tag : tags) {
            int score = 0;
            for (String keyword : corpusService.splitTagKeywords(tag)) {
                String normalizedKeyword = normalizeKeyword(keyword);
                if (normalizedKeyword.length() >= 2 && lowerQuestion.contains(normalizedKeyword)) {
                    score += 5;
                }
            }
            if (score > bestScore) {
                bestScore = score;
                bestTag = tag;
            }
        }
        return bestScore <= 0 ? "通用" : bestTag;
    }

    private String shorten(String text, int maxLength) {
        if (text == null) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }

    public synchronized void updateApiKey(String apiKey) {
        this.deepSeekApiKey = apiKey == null ? "" : apiKey.trim();
        saveApiKeyToFile(this.deepSeekApiKey);
        this.chatModel = this.deepSeekApiKey.isBlank() ? null : buildChatModel(this.deepSeekApiKey);
    }

    private OpenAiChatModel buildChatModel(String apiKey) {
        return OpenAiChatModel.builder()
                .baseUrl(deepSeekBaseUrl)
                .apiKey(apiKey)
                .modelName(deepSeekModel)
                .build();
    }

    private String loadApiKeyFromFile(String fallback) {
        Path path = Path.of(resolveConfigFile());
        if (!Files.exists(path)) return fallback;
        try {
            Properties props = new Properties();
            try (var in = Files.newInputStream(path)) { props.load(in); }
            String key = props.getProperty("deepseek.api-key", "").trim();
            return key.isBlank() ? fallback : key;
        } catch (IOException e) { return fallback; }
    }

    private void saveApiKeyToFile(String apiKey) {
        try {
            Path path = Path.of(resolveConfigFile());
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Properties props = new Properties();
            props.setProperty("deepseek.api-key", apiKey == null ? "" : apiKey);
            try (var out = Files.newOutputStream(path)) { props.store(out, "WustChat config"); }
        } catch (IOException ignored) {}
    }

    private String resolveConfigFile() {
        return configFile == null || configFile.isBlank() ? "./rag-config.properties" : configFile;
    }

    public String getApiKey() {
        return deepSeekApiKey;
    }
}
