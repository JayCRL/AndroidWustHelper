package com.example.wustchat.service;

import com.example.wustchat.model.CorpusItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByCharacterSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzhv15q.BgeSmallZhV15QuantizedEmbeddingModel;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class CorpusService {

    @Value("${rag.corpus-file:./rag-corpus.json}")
    private String corpusFile;

    @Value("${rag.pending-corpus-file:./rag-pending-corpus.json}")
    private String pendingCorpusFile;

    @Value("${rag.tag-file:./rag-tags.json}")
    private String tagFile;

    @Value("${rag.store-file:./rag-store.json}")
    private String storeFile;

    @Value("${rag.expire-pattern:yyyy-MM-dd HH:mm}")
    private String expirePattern;

    private final EmbeddingModel embeddingModel = new BgeSmallZhV15QuantizedEmbeddingModel();
    private final DocumentByCharacterSplitter splitter = new DocumentByCharacterSplitter(300, 30);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ChatCacheService cacheService;

    private InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
    private final List<CorpusItem> corpusItems = Collections.synchronizedList(new ArrayList<>());
    private final List<CorpusItem> pendingCorpusItems = Collections.synchronizedList(new ArrayList<>());
    private final Set<String> tags = Collections.synchronizedSet(new LinkedHashSet<>());
    private final AtomicLong idGenerator = new AtomicLong(0);

    public CorpusService(ChatCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @PostConstruct
    public void init() {
        loadData();
        cleanupExpiredData();
        rebuildStore();
    }

    private void loadData() {
        corpusItems.clear();
        corpusItems.addAll(loadFromFile(corpusFile));
        corpusItems.forEach(this::normalizeCorpusItemTag);
        long maxId = corpusItems.stream().mapToLong(item -> item.getId() == null ? 0 : item.getId()).max().orElse(0);
        idGenerator.set(maxId + 1);

        pendingCorpusItems.clear();
        pendingCorpusItems.addAll(loadFromFile(pendingCorpusFile));
        pendingCorpusItems.forEach(this::normalizeCorpusItemTag);

        tags.clear();
        tags.addAll(loadTagsFromFile());
    }

    private List<CorpusItem> loadFromFile(String filePath) {
        Path path = Path.of(filePath);
        try {
            if (!Files.exists(path)) return new ArrayList<>();
            String json = Files.readString(path);
            if (json == null || json.isBlank()) return new ArrayList<>();
            return objectMapper.readValue(json, new TypeReference<List<CorpusItem>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<String> loadTagsFromFile() {
        Path path = Path.of(tagFile);
        try {
            if (!Files.exists(path)) return new ArrayList<>(List.of("通用"));
            return Files.readAllLines(path).stream()
                    .flatMap(line -> splitTagKeywords(line).stream())
                    .filter(s -> !s.isBlank())
                    .distinct()
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return new ArrayList<>(List.of("通用"));
        }
    }

    public synchronized void rebuildStore() {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        Set<String> activeTags = new LinkedHashSet<>();
        activeTags.add("通用");

        for (CorpusItem item : corpusItems) {
            if (isExpired(item.getExpireAt())) continue;
            normalizeCorpusItemTag(item);

            Metadata metadata = new Metadata();
            metadata.put("id", item.getId());
            metadata.put("source", item.getSource());
            metadata.put("tag", item.getTag());
            metadata.put("uploaderType", item.getUploaderType());

            Document document = Document.from(item.getText(), metadata);
            List<TextSegment> segments = splitter.split(document);
            for (TextSegment segment : segments) {
                store.add(embeddingModel.embed(segment).content(), segment);
            }
            activeTags.addAll(splitTagKeywords(item.getTag()));
        }
        this.embeddingStore = store;
        this.tags.clear();
        this.tags.addAll(activeTags);
        saveData();
        cacheService.bumpKbVersion();
    }

    private void saveData() {
        saveToFile(corpusFile, corpusItems);
        saveToFile(pendingCorpusFile, pendingCorpusItems);
        saveTagsToFile();
        saveStoreToFile();
    }

    private void saveToFile(String filePath, List<CorpusItem> items) {
        try {
            Path path = Path.of(filePath);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(items));
        } catch (IOException ignored) {}
    }

    private void saveTagsToFile() {
        try {
            Path path = Path.of(tagFile);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, String.join("\n", tags));
        } catch (IOException ignored) {}
    }

    private void saveStoreToFile() {
        try {
            Path path = Path.of(storeFile);
            if (path.getParent() != null) Files.createDirectories(path.getParent());
            Files.writeString(path, embeddingStore.serializeToJson());
        } catch (IOException ignored) {}
    }

    public List<CorpusItem> getCorpusItems() {
        cleanupExpiredData();
        return new ArrayList<>(corpusItems);
    }

    public List<CorpusItem> getPendingCorpusItems() {
        cleanupExpiredData();
        return new ArrayList<>(pendingCorpusItems);
    }

    public List<String> getTags() {
        return new ArrayList<>(tags);
    }

    public void ingest(String text, String source, String tag, String expireAt, String uploaderType, boolean studentSubmitted) {
        CorpusItem item = new CorpusItem();
        item.setId(idGenerator.getAndIncrement());
        item.setText(text);
        item.setSource(source == null || source.isBlank() ? "manual" : source);
        item.setTag(normalizeTag(tag));
        item.setExpireAt(normalizeExpireAt(expireAt));
        item.setUploaderType(uploaderType);
        item.setStudentSubmitted(studentSubmitted);

        if (studentSubmitted && "student".equals(uploaderType)) {
            pendingCorpusItems.add(item);
            saveToFile(pendingCorpusFile, pendingCorpusItems);
        } else {
            corpusItems.add(item);
            rebuildStore();
        }
    }

    public void approve(Long id) {
        CorpusItem target = null;
        synchronized (pendingCorpusItems) {
            for (int i = 0; i < pendingCorpusItems.size(); i++) {
                if (id.equals(pendingCorpusItems.get(i).getId())) {
                    target = pendingCorpusItems.remove(i);
                    break;
                }
            }
        }
        if (target != null) {
            normalizeCorpusItemTag(target);
            corpusItems.add(target);
            rebuildStore();
        }
    }

    public void reject(Long id) {
        synchronized (pendingCorpusItems) {
            pendingCorpusItems.removeIf(item -> id.equals(item.getId()));
        }
        saveToFile(pendingCorpusFile, pendingCorpusItems);
    }

    public void delete(Long id) {
        boolean removed = corpusItems.removeIf(item -> id.equals(item.getId()));
        if (removed) {
            rebuildStore();
        }
    }

    private void normalizeCorpusItemTag(CorpusItem item) {
        item.setTag(normalizeTag(item.getTag()));
    }

    public String normalizeTag(String rawTag) {
        List<String> keywords = splitTagKeywords(rawTag);
        if (keywords.isEmpty()) {
            return "通用";
        }
        return keywords.stream()
                .map(keyword -> "通用".equals(keyword) ? keyword : "#" + keyword)
                .collect(Collectors.joining(" "));
    }

    public List<String> splitTagKeywords(String rawTag) {
        if (rawTag == null || rawTag.isBlank()) {
            return List.of("通用");
        }
        String normalized = rawTag
                .replace('，', ' ')
                .replace(',', ' ')
                .replace('、', ' ')
                .replace('/', ' ')
                .replace('|', ' ')
                .replace(';', ' ')
                .replace('；', ' ')
                .replace("#", " ")
                .trim();
        if (normalized.isBlank()) {
            return List.of("通用");
        }
        return List.of(normalized.split("\\s+"))
                .stream()
                .map(String::trim)
                .filter(keyword -> !keyword.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }

    private String normalizeExpireAt(String expireAt) {
        if (expireAt == null || expireAt.isBlank()) return "";
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(expirePattern);
            return LocalDateTime.parse(expireAt.trim(), formatter).format(formatter);
        } catch (DateTimeParseException e) {
            return "";
        }
    }

    private boolean isExpired(String expireAt) {
        if (expireAt == null || expireAt.isBlank()) return false;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(expirePattern);
            return LocalDateTime.now().isAfter(LocalDateTime.parse(expireAt.trim(), formatter));
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public synchronized void cleanupExpiredData() {
        boolean changed = corpusItems.removeIf(item -> isExpired(item.getExpireAt()));
        boolean pendingChanged = pendingCorpusItems.removeIf(item -> isExpired(item.getExpireAt()));
        if (changed) rebuildStore();
        else if (pendingChanged) saveToFile(pendingCorpusFile, pendingCorpusItems);
    }

    public InMemoryEmbeddingStore<TextSegment> getEmbeddingStore() {
        return embeddingStore;
    }

    public EmbeddingModel getEmbeddingModel() {
        return embeddingModel;
    }
}
