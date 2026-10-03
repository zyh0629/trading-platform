package com.campus.trading.controller;

import com.campus.trading.ai.AiAssistantService;
import com.campus.trading.ai.AskRequest;
import com.campus.trading.ai.AskResponse;
import com.campus.trading.security.UserPrincipal;
import com.campus.trading.utils.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private static final int MAX_DESCRIPTION_REQUESTS_PER_MINUTE = 5;
    private static final long RATE_LIMIT_WINDOW_NANOS = 60_000_000_000L;

    private final AiAssistantService assistantService;
    private final ConcurrentHashMap<Integer, Deque<Long>> descriptionRequests = new ConcurrentHashMap<>();
    private final AtomicInteger rateLimitChecks = new AtomicInteger();

    public AiController(AiAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public Result<AskResponse> ask(@RequestBody AskRequest request) {
        return Result.success(assistantService.ask(request.question()));
    }

    @PostMapping("/generate-description")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public ResponseEntity<Result<DescriptionResponse>> generateDescription(
            @RequestBody GenerateDescriptionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        validateDescriptionRequest(request);
        if (!allowDescriptionRequest(principal.getUserId())) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Result.error(429, "每分钟最多生成 5 次，请稍后再试"));
        }
        String description = assistantService.generateProductDescription(
                request.title().trim(), request.category().trim());
        return ResponseEntity.ok(Result.success(new DescriptionResponse(description)));
    }

    private void validateDescriptionRequest(GenerateDescriptionRequest request) {
        if (request == null || request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("商品标题不能为空");
        }
        String title = request.title().trim();
        int titleLength = title.codePointCount(0, title.length());
        if (titleLength < 1 || titleLength > 50) {
            throw new IllegalArgumentException("商品标题长度必须在 1~50 字之间");
        }
        if (request.category() == null || request.category().isBlank()) {
            throw new IllegalArgumentException("商品分类不能为空");
        }
    }

    private boolean allowDescriptionRequest(Integer userId) {
        long now = System.nanoTime();
        AtomicBoolean allowed = new AtomicBoolean();
        descriptionRequests.compute(userId, (ignored, existingAttempts) -> {
            Deque<Long> attempts = existingAttempts == null ? new ArrayDeque<>() : existingAttempts;
            while (!attempts.isEmpty() && now - attempts.peekFirst() >= RATE_LIMIT_WINDOW_NANOS) {
                attempts.removeFirst();
            }
            if (attempts.size() >= MAX_DESCRIPTION_REQUESTS_PER_MINUTE) {
                return attempts;
            }
            attempts.addLast(now);
            allowed.set(true);
            return attempts;
        });
        if (!allowed.get()) {
            return false;
        }
        if (rateLimitChecks.incrementAndGet() % 100 == 0) {
            descriptionRequests.forEach((id, ignored) -> descriptionRequests.computeIfPresent(id,
                    (key, attempts) -> attempts.isEmpty()
                            || now - attempts.peekLast() >= RATE_LIMIT_WINDOW_NANOS ? null : attempts));
        }
        return true;
    }

    public record GenerateDescriptionRequest(String title, String category) {}

    public record DescriptionResponse(String description) {}
}
