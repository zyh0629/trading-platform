# 实验五：LLM 应用开发与智能功能原型

## 原型功能

项目新增“学习搭子”入口（`/assistant`）。用户问题先经过长度和提示注入校验，再使用关键词检索项目资料片段，最后把限定上下文发送到 OpenAI 兼容的 `/chat/completions` 接口。页面展示模型回答和参考资料标题。

## 配置与运行

后端不会把 Key 写入配置文件或数据库：

```powershell
$env:LLM_API_KEY="your-api-key"
$env:LLM_BASE_URL="https://api.openai.com/v1"
$env:LLM_MODEL="gpt-4o-mini"
$env:JWT_SECRET="at-least-32-character-jwt-secret"
```

通义、DeepSeek 或本地 Ollama 只需替换 `LLM_BASE_URL` 和 `LLM_MODEL`，接口需兼容 OpenAI Chat Completions 格式。

## 提示工程与 RAG 说明

- System 提示定义角色、回答边界、资料不足时的固定答复和来源要求。
- 文档目前以小型 `KnowledgeBase` 片段形式保存，检索采用词项匹配；后续可替换为 Embedding + 向量数据库。
- 资料被包在 `<context>` 中并明确标记为不可信内容，用户问题不能覆盖系统规则。

## 反思记录

1. 模型可能把资料外的常识当成项目事实。应限定上下文，并要求“资料中没有足够依据”。
2. 模型可能漏掉出处。响应中单独返回检索到的资料标题，前端展示来源。
3. 模型服务或 Key 缺失时不能伪造成功，接口返回明确错误，由人工或普通帮助页兜底。
