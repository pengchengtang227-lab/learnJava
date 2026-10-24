# 项目工作约定

- 使用中文回答；较长的总结、计划和任务说明优先使用表格整理。

## 在 Codex 中调用 TypeSafe / Jev

- 用户要求使用 TypeSafe 技能时，读取 `.agents/skills/typesafe-ai/SKILL.md`。
- 用户要求实际调用 Jev 时，使用 `tools/jev/Invoke-Jev.ps1 -NoPrompt`。具体配置和调用示例见 `tools/jev/README.md`。
- 中转站请求地址为 `https://slb-v1.api.fan/v1/systemone`。用户已指定：今后所有 Jev 调用使用固定版本 `jev-1.14.0`，自定义请求文件中的 `model` 也应填写该版本；只有用户明确要求更换时才调整，不自动改用别名或回退其他版本。
- 按用户任务构造 `state` 和 `questions`，把完整请求保存为不含密钥的临时 JSON，通过 `-RequestFile` 传给脚本；单纯测试消息分类时可使用 `-Text`。
- 密钥由脚本内部读取 `JEV_RELAY_API_KEY` 环境变量或 `tools/jev/.env`。不要读取并展示密钥，不要把密钥放入聊天、请求文件、命令行参数或浏览器工具。
- 未配置密钥时，提示用户在本机终端运行 `pwsh -File .\tools\jev\Set-JevKey.ps1`，无需要求用户发送 Key。
- 只发送完成当前用户任务所需的内容。遵循当前环境的网络和审批要求；仅在收到真实 API 响应后报告实际调用结果，明确区分预览、模拟和真实调用。
