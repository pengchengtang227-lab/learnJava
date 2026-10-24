#Requires -Version 7.0
<#
.SYNOPSIS
通过 PackyAPI 中转站调用 Jev，供 Codex 聊天及本机终端使用。
.EXAMPLE
./tools/jev/Invoke-Jev.ps1 -Text '我被重复扣款，请尽快处理。'
.EXAMPLE
./tools/jev/Invoke-Jev.ps1 -DryRun
.EXAMPLE
./tools/jev/Invoke-Jev.ps1 -RequestFile ./request.json -NoPrompt
#>
[CmdletBinding(DefaultParameterSetName = 'Text')]
param(
    [Parameter(ParameterSetName = 'Text', Position = 0)]
    [ValidateNotNullOrEmpty()]
    [string]$Text = '我刚刚被扣了两次款，请尽快帮我处理。',

    [ValidateNotNullOrEmpty()]
    [string]$Model = 'jev-1.14.0',

    [Parameter(ParameterSetName = 'Request', Mandatory)]
    [ValidateNotNullOrEmpty()]
    [string]$RequestFile,

    [switch]$DryRun,

    [switch]$NoPrompt
)

$ErrorActionPreference = 'Stop'
$endpoint = 'https://slb-v1.api.fan/v1/systemone'
if ($PSCmdlet.ParameterSetName -eq 'Request') {
    $requestBody = Get-Content -LiteralPath $RequestFile -Raw -Encoding utf8 |
        ConvertFrom-Json -AsHashtable
    if ($requestBody -isnot [System.Collections.IDictionary] -or
        -not $requestBody.Contains('state') -or $null -eq $requestBody.state -or
        $requestBody.model -isnot [string] -or
        [string]::IsNullOrWhiteSpace($requestBody.model) -or
        $requestBody.questions -isnot [System.Collections.IDictionary] -or
        $requestBody.questions.Count -eq 0) {
        throw '请求文件必须包含 state、非空字符串 model，以及非空对象 questions。'
    }
    # 所有调用入口统一采用项目默认版本；显式传入 -Model 时才更换。
    $requestBody.model = $Model
}
else {
    $requestBody = [ordered]@{
        state = $Text
        model = $Model
        questions = [ordered]@{
            category = @{
                type = 'choice'
                instructions = '这条消息主要属于哪一类？'
                criteria = [ordered]@{
                    billing = '付款、扣费、账单或退款问题'
                    technical = '软件故障、接口错误或技术问题'
                    other = '不属于付款或技术问题的其他消息'
                }
            }
            is_urgent = @{
                type = 'noul'
                instructions = '这条消息是否表达了需要尽快处理的紧迫性？'
            }
        }
    }
}
$requestJson = $requestBody | ConvertTo-Json -Depth 64

if ($DryRun) {
    [ordered]@{
        endpoint = $endpoint
        method = 'POST'
        body = $requestBody
    } | ConvertTo-Json -Depth 66
    return
}

# 使用中转站专用变量，避免误用其他服务的 Key。
$apiKey = $env:JEV_RELAY_API_KEY
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    $keyFile = Join-Path $PSScriptRoot '.env'
    if (Test-Path -LiteralPath $keyFile) {
        foreach ($line in [System.IO.File]::ReadAllLines($keyFile)) {
            if ($line -match '^\s*JEV_RELAY_API_KEY\s*=(.*)$') {
                $apiKey = $Matches[1].Trim()
                # 仅解析这一项配置；不会执行配置文件中的内容。
                if ($apiKey.Length -ge 2 -and
                    (($apiKey.StartsWith('"') -and $apiKey.EndsWith('"')) -or
                     ($apiKey.StartsWith("'") -and $apiKey.EndsWith("'")))) {
                    $apiKey = $apiKey.Substring(1, $apiKey.Length - 2)
                }
                break
            }
        }
        $line = $null
        $Matches = $null
    }
}
if ([string]::IsNullOrWhiteSpace($apiKey) -and $NoPrompt) {
    throw '尚未配置中转站 Key。请先在本机终端运行：pwsh -File .\tools\jev\Set-JevKey.ps1'
}
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    $secureKey = Read-Host '请在本机输入中转站 Key（隐藏输入）' -AsSecureString
    try {
        $apiKey = [System.Net.NetworkCredential]::new('', $secureKey).Password
    }
    finally {
        $secureKey.Dispose()
    }
}
if ([string]::IsNullOrWhiteSpace($apiKey)) {
    throw 'Key 不能为空。请重新运行并输入中转站 Key。'
}
$apiKey = $apiKey.Trim()
$headers = @{ Authorization = "Bearer $apiKey" }

try {
    $response = Invoke-RestMethod -Method Post -Uri $endpoint `
        -Headers $headers -ContentType 'application/json; charset=utf-8' `
        -Body ([System.Text.Encoding]::UTF8.GetBytes($requestJson)) `
        -TimeoutSec 30 -MaximumRedirection 0

    $response | ConvertTo-Json -Depth 64
}
catch [Microsoft.PowerShell.Commands.HttpResponseException] {
    $statusCode = [int]$_.Exception.Response.StatusCode
    $detail = $_.ErrorDetails.Message
    if ([string]::IsNullOrWhiteSpace($detail)) {
        $detail = $_.Exception.Message
    }
    $detail = $detail.Replace($apiKey, '[REDACTED]')
    throw "Jev 调用失败，HTTP ${statusCode}：$detail"
}
finally {
    $apiKey = $null
    $headers = $null
}
