package com.example.wustchat;

import cn.wustlinghang.mywust.common.core.Result;
import com.example.wustchat.dto.ApiKeyRequest;
import com.example.wustchat.dto.ChatResponse;
import com.example.wustchat.dto.IngestRequest;
import com.example.wustchat.model.CorpusItem;
import com.example.wustchat.service.AdminAuthService;
import com.example.wustchat.service.ChatCacheService;
import com.example.wustchat.service.CorpusService;
import com.example.wustchat.service.RagQueryService;
import com.example.wustchat.util.FileExtractionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final CorpusService corpusService;
    private final RagQueryService ragQueryService;
    private final ChatCacheService cacheService;
    private final AdminAuthService adminAuthService;

    public RagController(CorpusService corpusService, RagQueryService ragQueryService, ChatCacheService cacheService, AdminAuthService adminAuthService) {
        this.corpusService = corpusService;
        this.ragQueryService = ragQueryService;
        this.cacheService = cacheService;
        this.adminAuthService = adminAuthService;
    }

    @PostMapping("/config/api-key")
    public Result<String> updateApiKey(@RequestBody ApiKeyRequest request) {
        adminAuthService.requireAdminRole();
        ragQueryService.updateApiKey(request.getApiKey());
        return Result.ok(null, "API Key 已更新");
    }

    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("apiKeyConfigured", ragQueryService.getApiKey() != null && !ragQueryService.getApiKey().isBlank());
        result.put("corpusCount", corpusService.getCorpusItems().size());
        result.put("pendingCorpusCount", corpusService.getPendingCorpusItems().size());
        result.put("tags", corpusService.getTags());
        return Result.ok(result);
    }

    @GetMapping("/corpus")
    public Result<List<Map<String, Object>>> getCorpus() {
        adminAuthService.requireAdminRole();
        String cacheKey = cacheService.buildCorpusListKey();
        List<Map<String, Object>> cached = (List<Map<String, Object>>) cacheService.get(cacheKey);
        if (cached != null) return Result.ok(cached);

        List<Map<String, Object>> result = corpusService.getCorpusItems().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("tag", item.getTag());
            row.put("source", item.getSource());
            row.put("uploaderType", item.getUploaderType());
            row.put("expireAt", item.getExpireAt() == null || item.getExpireAt().isBlank() ? "永不过期" : item.getExpireAt());
            row.put("textPreview", shorten(item.getText(), 120));
            return row;
        }).collect(Collectors.toList());

        cacheService.setCorpusList(cacheKey, result);
        return Result.ok(result);
    }

    @GetMapping("/pending-corpus")
    public Result<List<Map<String, Object>>> getPendingCorpus() {
        adminAuthService.requireAdminRole();
        List<Map<String, Object>> result = corpusService.getPendingCorpusItems().stream().map(item -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", item.getId());
            row.put("tag", item.getTag());
            row.put("source", item.getSource());
            row.put("uploaderType", item.getUploaderType());
            row.put("expireAt", item.getExpireAt() == null || item.getExpireAt().isBlank() ? "永不过期" : item.getExpireAt());
            row.put("textPreview", shorten(item.getText(), 120));
            return row;
        }).collect(Collectors.toList());
        return Result.ok(result);
    }

    @PostMapping("/student/submit")
    public Result<String> studentSubmit(@RequestBody IngestRequest request) {
        corpusService.ingest(request.getText(), request.getSource(), request.getTag(), request.getExpireAt(), "student", true);
        return Result.ok(null, "已提交审核");
    }

    @PostMapping("/pending-corpus/approve")
    public Result<String> approve(@RequestParam("id") Long id) {
        adminAuthService.requireAdminRole();
        corpusService.approve(id);
        return Result.ok(null, "审核通过");
    }

    @PostMapping("/pending-corpus/reject")
    public Result<String> reject(@RequestParam("id") Long id) {
        adminAuthService.requireAdminRole();
        corpusService.reject(id);
        return Result.ok(null, "已拒绝");
    }

    @PostMapping("/corpus/delete")
    public Result<String> delete(@RequestParam("id") Long id) {
        adminAuthService.requireAdminRole();
        corpusService.delete(id);
        return Result.ok(null, "已删除");
    }

    @PostMapping("/ingest")
    public Result<String> ingest(@RequestBody IngestRequest request) {
        adminAuthService.requireAdminRole();
        corpusService.ingest(request.getText(), request.getSource(), request.getTag(), request.getExpireAt(), "admin", false);
        return Result.ok(null, "入库成功");
    }

    @PostMapping("/ingest-file")
    public Result<String> ingestFile(@RequestParam("file") MultipartFile file,
                                     @RequestParam(value = "tag", required = false) String tag,
                                     @RequestParam(value = "expireAt", required = false) String expireAt) throws IOException {
        adminAuthService.requireAdminRole();
        String text = FileExtractionUtils.extractText(file);
        corpusService.ingest(text, file.getOriginalFilename(), tag, expireAt, "admin", false);
        return Result.ok(null, "文件入库成功");
    }

    @PostMapping("/cache/qa/clear")
    public Result<String> clearQaCache() {
        adminAuthService.requireAdminRole();
        long deletedCount = cacheService.clearQaCache();
        return Result.ok(null, "已清除问答缓存，共删除 " + deletedCount + " 条记录");
    }

    @GetMapping("/chat")
    public Result<ChatResponse> chat(@RequestParam("question") String question) {
        return Result.ok(ragQueryService.ask(question));
    }

    private String shorten(String text, int maxLength) {
        if (text == null) return "";
        String normalized = text.replaceAll("\\s+", " ").trim();
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength) + "...";
    }
}
