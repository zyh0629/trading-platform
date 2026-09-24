package com.campus.trading.controller;

import com.campus.trading.ai.AiAssistantService;
import com.campus.trading.ai.AskRequest;
import com.campus.trading.ai.AskResponse;
import com.campus.trading.utils.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiAssistantService assistantService;

    public AiController(AiAssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    @PreAuthorize("isAuthenticated()")
    public Result<AskResponse> ask(@RequestBody AskRequest request) {
        return Result.success(assistantService.ask(request.question()));
    }
}
