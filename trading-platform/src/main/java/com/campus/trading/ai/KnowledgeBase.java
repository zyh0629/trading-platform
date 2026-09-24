package com.campus.trading.ai;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KnowledgeBase {
    private static final Pattern TERM_PATTERN =
            Pattern.compile("[\\p{IsHan}]{2,}|[\\p{L}\\p{N}_]{2,}");
    private final List<DocumentChunk> chunks = List.of(
            new DocumentChunk("实验四：登录与权限",
                    "系统使用 Spring Security 实现无状态 JWT 认证。用户角色包括 USER 和 ADMIN，管理员接口使用 @PreAuthorize 限制访问。"),
            new DocumentChunk("实验四：密码安全",
                    "用户密码和安全问题答案使用 BCrypt 哈希后保存，JWT 签名密钥从环境变量读取，不能把密钥或明文密码提交到代码仓库。"),
            new DocumentChunk("实验五：提示工程",
                    "问答提示由稳定的 System 指令、检索到的资料上下文和用户问题组成。资料仅用于回答事实，用户输入不能覆盖系统指令。"),
            new DocumentChunk("实验五：RAG",
                    "RAG 流程是文档切块、关键词检索、把相关片段拼入提示，再让模型基于限定资料回答；回答应给出资料来源，找不到依据时明确说明。"),
            new DocumentChunk("交易平台功能",
                    "平台包含商品发布、商品搜索、收藏、私信、交易订单、行为推荐和管理员数据统计等功能。")
    );

    public List<DocumentChunk> search(String question, int limit) {
        String normalized = question.toLowerCase(Locale.ROOT);
        return chunks.stream()
                .map(chunk -> new ScoredChunk(chunk, score(normalized, chunk.content().toLowerCase(Locale.ROOT))))
                .filter(item -> item.score() > 0)
                .sorted(Comparator.comparingInt(ScoredChunk::score).reversed())
                .limit(limit)
                .map(ScoredChunk::chunk)
                .collect(Collectors.toList());
    }

    private int score(String question, String content) {
        Matcher matcher = TERM_PATTERN.matcher(question);
        int score = 0;
        while (matcher.find()) {
            if (content.contains(matcher.group())) {
                score++;
            }
        }
        return score;
    }

    public record DocumentChunk(String source, String content) {}
    private record ScoredChunk(DocumentChunk chunk, int score) {}
}
