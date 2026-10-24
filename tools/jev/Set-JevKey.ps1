#Requires -Version 7.0
<#
.SYNOPSIS
在本机隐藏输入 Jev 中转站 Key，保存后供 Codex 的调用脚本读取。
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$keyFile = Join-Path $PSScriptRoot '.env'
$secureKey = Read-Host '请输入 Jev 中转站 Key（隐藏输入）' -AsSecureString
$apiKey = $null
try {
    $apiKey = [System.Net.NetworkCredential]::new('', $secureKey).Password.Trim()
    if ([string]::IsNullOrWhiteSpace($apiKey) -or $apiKey -match '[\r\n]') {
        throw 'Key 不能为空，也不能包含换行符。'
    }

    $existingLines = @()
    if (Test-Path -LiteralPath $keyFile) {
        $existingLines = @([System.IO.File]::ReadAllLines($keyFile) |
            Where-Object { $_ -notmatch '^\s*JEV_RELAY_API_KEY\s*=' })
    }
    $updatedLines = @($existingLines) + "JEV_RELAY_API_KEY=$apiKey"
    [System.IO.File]::WriteAllLines($keyFile, [string[]]$updatedLines,
        [System.Text.UTF8Encoding]::new($false))

    Write-Host '已将 Key 保存到 tools/jev/.env（本机明文配置，已被项目 Git 规则忽略）。'
    Write-Host '现在回到 Codex 聊天，输入：$typesafe-ai 请实际调用 Jev，判断“系统故障，请马上处理”是否紧急。'
}
finally {
    $secureKey.Dispose()
    $apiKey = $null
    $existingLines = $null
    $updatedLines = $null
}
