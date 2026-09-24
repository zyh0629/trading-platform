# 实验三：Git 协作开发与持续集成

## 分支与提交约定

- `main`：稳定分支，只接受经过 Pull Request 的合并。
- `feature/<short-name>`：新功能开发，例如 `feature/user-module`。
- `fix/<short-name>`：问题修复。
- 提交信息使用简短、可读的动词开头，例如 `feat: add login`、`fix: reject expired token`。
- 一个提交尽量只完成一个逻辑变更，方便审查、回滚和解决冲突。

## 推荐协作流程

```bash
git switch main
git pull --ff-only origin main
git switch -c feature/example
# 修改代码并运行后端测试、前端构建
git add .
git commit -m "feat: implement example"
git push -u origin feature/example
```

推送后创建 Pull Request，填写 `.github/pull_request_template.md`，等待 CI 和人工 Review 通过后再合并。合并前同步主分支：

```bash
git fetch origin
git rebase origin/main
# 有冲突时修改文件，然后：
git add <resolved-file>
git rebase --continue
git push --force-with-lease
```

如果团队不使用 rebase，也可以在功能分支执行 `git merge origin/main`；不要用 `git push --force` 覆盖其他人的提交。

## CI

`.github/workflows/ci.yml` 在推送到 `main`/`feature/**` 或创建 PR 时运行：

- Java 17 + MySQL 8：执行 `./mvnw -B test`
- Node.js 20：执行 `npm ci` 和 `npm run build`

CI 中的数据库密码和 JWT 密钥仅为临时测试值，真实凭据必须放在 GitHub Actions Secrets 或部署环境变量中。

## AI 辅助代码评审

AI 只能做初步筛查，不能代替人工 Review。建议把 PR 描述、测试结果和最小必要 diff 提供给 AI，并使用以下提示：

> 请只依据提供的 diff 生成：1）变更摘要；2）按严重程度排序的潜在 Bug、安全问题和兼容性风险；3）需要补充的测试。不要假设未提供的代码。重点检查认证授权、明文密钥、输入校验、异常处理和数据泄露。每条意见引用文件和行号，并说明置信度。

评审者必须人工确认 AI 的每条意见，补充真实测试，并在 PR 中记录“人工复核结论”。不要把源代码、用户数据、密码、令牌或 API Key 发送给未获授权的第三方模型。
