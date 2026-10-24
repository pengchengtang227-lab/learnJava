# 在 Codex 中使用 Jev

Codex 根据 TypeSafe 技能组织判断任务，再通过项目内的脚本调用 Jev，并解释结果。
调用脚本使用 PowerShell 7 和 TypeSafe 原生接口，无需安装 SDK。

## 一次性配置 Key

在 Codex 的本机终端中进入当前项目根目录，执行：

```powershell
pwsh -File .\tools\jev\Set-JevKey.ps1
```

按提示输入你的 Jev 中转站 Key，输入隐藏。Key 保存在 `tools/jev/.env` 中，
是本机明文配置；项目现有 `.gitignore` 已忽略该文件。
该文件与调用脚本共享，不依赖终端环境变量传播，因此配置完成后可以直接回到当前 Codex 聊天使用。

## 在 Codex 聊天中使用

发送这样的消息：

```text
$typesafe-ai 请实际调用 Jev，判断“系统一直报错，请马上处理”是否紧急，并解释返回结果。
```

或者：

```text
$typesafe-ai 使用项目的 Jev 中转站，把以下评论分类为正面、中性或负面：……
```

项目根目录 `AGENTS.md` 已记录脚本与配置位置，供后续 Codex 聊天读取。
Codex 负责组织任务、执行脚本和解释结果，Jev 负责请求中的结构化判断。
仅要求解释技能或设计代码时，不一定需要发送模型请求；明确说“实际调用”可表达你的调用意图。

## 接入配置

| 配置 | 值 |
| --- | --- |
| 中转站 Base URL | `https://slb-v1.api.fan/v1` |
| 请求地址 | `https://slb-v1.api.fan/v1/systemone` |
| 默认模型 | `jev-1.14.0`（用户指定的固定版本） |
| 令牌分组 | `typesafe-officially`（请在中转站后台确认自己的令牌权限） |
| 认证 | `Authorization: Bearer <中转站 Key>` |

## 手动测试

在项目根目录执行：

```powershell
pwsh -File .\tools\jev\Invoke-Jev.ps1
```

按提示在本机输入中转站 Key，输入内容隐藏。脚本不会把 Key 写入文件。
每次正常运行会向中转站发送一次模型请求。

替换要分析的文本：

```powershell
pwsh -File .\tools\jev\Invoke-Jev.ps1 -Text '系统一直报错，所有订单都无法提交，请马上处理。'
```

脚本优先读取当前进程的 `JEV_RELAY_API_KEY` 环境变量，再读取 `tools/jev/.env`；
均未配置时才会提示输入。Codex 调用应加 `-NoPrompt`，缺少配置时立即报错，避免等待交互输入。

## Codex 自定义判断

Codex 可把需要发送的完整请求写入临时 JSON，再运行：

```powershell
pwsh -File .\tools\jev\Invoke-Jev.ps1 -RequestFile '<请求文件的绝对路径>' -NoPrompt
```

文本和请求文件两种调用方式均默认使用 `jev-1.14.0`。脚本会将请求文件的 `model`
统一设置为该默认版本，以免复用旧请求时调用旧模型。只有用户明确要求其他版本时，
才通过 `-Model` 指定；两种调用方式均支持此参数。

请求文件只包含 `state`、`model` 和 `questions` 等 API 请求字段，不包含 Key。例如：

```json
{
  "state": "系统一直报错，请马上处理。",
  "model": "jev-1.14.0",
  "questions": {
    "is_urgent": {
      "type": "noul",
      "instructions": "这条消息是否表达了需要尽快处理的紧迫性？"
    }
  }
}
```

## 结果含义

脚本对同一段文本提出两个问题，返回原始 JSON：

| 字段 | 含义 |
| --- | --- |
| `answers.category.choice` | `billing`（付款问题）、`technical`（技术问题）或 `other` |
| `answers.category.probabilities` | 各类别的概率 |
| `answers.category.confidence` | 类别概率分布的集中程度 |
| `answers.is_urgent.noul` | 消息表达紧迫性的概率，范围 0～1 |
| `usage` | 接口报告的 Token 用量 |

这些判断需要结合实际业务数据验证，模型结果可能变化。

## 仅检查请求

```powershell
pwsh -File .\tools\jev\Invoke-Jev.ps1 -DryRun
```

该命令展示请求地址和 JSON，不读取 Key，也不发送网络请求。

## 排错

| 现象 | 检查内容 |
| --- | --- |
| HTTP 401 | Key 是否完整、有效 |
| 模型不可用或无权限 | Key 的分组、模型限制和账户额度 |
| HTTP 404 | 中转站是否仍提供 `/v1/systemone` 路由 |
| HTTP 422 | 请求字段和模型要求是否一致 |
| HTTP 429 | 降低调用频率后重试 |

## 已核对的资料

- [TypeSafe HTTP API](https://docs.typesafe.ai/api)
- 2026-10-15 使用本机 Key 查询中转站 `GET /v1/models`，列出了 `jev-1.13.0`、`jev-1.14.0` 和 `jev-latest`。

已于 2026-10-15 通过中转站实际调用 `jev-1.14.0` 成功，响应中的模型版本为 `jev-1.14.0`。
