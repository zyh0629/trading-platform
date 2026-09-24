package com.campus.trading.ai;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class KnowledgeBase {
    private static final Logger log = LoggerFactory.getLogger(KnowledgeBase.class);
    private static final Pattern WORD_PATTERN = Pattern.compile("[A-Za-z0-9_]{2,}");
    private static final int MAX_CHUNK_LENGTH = 2000;

    private final List<DocumentChunk> chunks = new ArrayList<>(List.of(
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
    ));

    @PostConstruct
    public void loadDocuments() {
        Path projectRoot = Paths.get("").toAbsolutePath().normalize();
        Set<Path> files = new LinkedHashSet<>();
        collectMarkdownFiles(projectRoot.resolve("docs"), files);
        collectFile(projectRoot.resolve("README.md"), files);

        try (Stream<Path> entries = Files.list(projectRoot)) {
            entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                    .forEach(files::add);
        } catch (IOException exception) {
            log.warn("Unable to scan Markdown files in {}", projectRoot, exception);
        }

        int loadedFiles = 0;
        for (Path file : files) {
            try {
                String content = Files.readString(file, StandardCharsets.UTF_8);
                chunks.addAll(toChunks(file.getFileName().toString(), content));
                loadedFiles++;
            } catch (IOException exception) {
                log.warn("Unable to read knowledge document {}", file, exception);
            }
        }
        log.info("KnowledgeBase loaded {} chunks from {} files", chunks.size(), loadedFiles);
    }

    private void collectMarkdownFiles(Path directory, Set<Path> files) {
        if (!Files.isDirectory(directory)) {
            log.warn("Knowledge document directory does not exist: {}", directory);
            return;
        }
        try (Stream<Path> entries = Files.walk(directory)) {
            entries.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".md"))
                    .forEach(files::add);
        } catch (IOException exception) {
            log.warn("Unable to scan knowledge document directory {}", directory, exception);
        }
    }

    private void collectFile(Path file, Set<Path> files) {
        if (Files.isRegularFile(file)) {
            files.add(file);
        }
    }

    private List<DocumentChunk> toChunks(String fileName, String content) {
        if (content.length() <= MAX_CHUNK_LENGTH) {
            return List.of(new DocumentChunk(fileName, content));
        }

        String[] paragraphs = content.split("\\R\\s*\\R");
        List<DocumentChunk> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : paragraphs) {
            if (paragraph.length() > MAX_CHUNK_LENGTH) {
                addChunk(result, fileName, current);
                for (int start = 0; start < paragraph.length(); start += MAX_CHUNK_LENGTH) {
                    int end = Math.min(start + MAX_CHUNK_LENGTH, paragraph.length());
                    result.add(new DocumentChunk(fileName, paragraph.substring(start, end)));
                }
            } else if (current.length() > 0
                    && current.length() + paragraph.length() + 2 > MAX_CHUNK_LENGTH) {
                addChunk(result, fileName, current);
                current.append(paragraph);
            } else {
                if (current.length() > 0) {
                    current.append("\n\n");
                }
                current.append(paragraph);
            }
        }
        addChunk(result, fileName, current);
        return result;
    }

    private void addChunk(List<DocumentChunk> result, String fileName, StringBuilder content) {
        if (content.length() == 0) {
            return;
        }
        result.add(new DocumentChunk(fileName + "#" + (result.size() + 1), content.toString()));
        content.setLength(0);
    }

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
        int score = 0;

        // English and numeric terms retain the original word-based matching.
        Matcher matcher = WORD_PATTERN.matcher(question);
        while (matcher.find()) {
            if (content.contains(matcher.group())) {
                score++;
            }
        }

        // Chinese text is matched with overlapping 2-grams so phrases such as
        // "实验三做了什么" can match the document term "实验三".
        StringBuilder chineseRun = new StringBuilder();
        for (int offset = 0; offset < question.length();) {
            int codePoint = question.codePointAt(offset);
            if (Character.UnicodeScript.of(codePoint) == Character.UnicodeScript.HAN) {
                chineseRun.appendCodePoint(codePoint);
            } else {
                score += scoreChineseBigrams(chineseRun, content);
                chineseRun.setLength(0);
            }
            offset += Character.charCount(codePoint);
        }
        score += scoreChineseBigrams(chineseRun, content);
        return score;
    }

    private int scoreChineseBigrams(StringBuilder chineseRun, String content) {
        int score = 0;
        for (int index = 0; index + 1 < chineseRun.length(); index++) {
            String bigram = chineseRun.substring(index, index + 2);
            if (content.contains(bigram)) {
                score++;
            }
        }
        return score;
    }

    public record DocumentChunk(String source, String content) {}
    private record ScoredChunk(DocumentChunk chunk, int score) {}
}
