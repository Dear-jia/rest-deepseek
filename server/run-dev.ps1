# ============================================================
# 文峰小馆 · 本地开发启动脚本
# 用法：.\server\run-dev.ps1              （默认端口 8080）
#        .\server\run-dev.ps1 -Port 8088  （8080 被占用时换端口）
# 可选：先设置 DATABASE_URL / DB_DRIVER / DB_USERNAME / DB_PASSWORD 以连接 PostgreSQL
# ============================================================
param([int]$Port = 8080)
$ErrorActionPreference = "Stop"

$jdk17 = "C:\Program Files\Java\jdk-17"
if (Test-Path $jdk17) {
    $env:JAVA_HOME = $jdk17
    Write-Host "使用 JDK 17: $jdk17"
} else {
    Write-Host "未找到 JDK 17，请确认已安装"
}

$mvn = "$env:LOCALAPPDATA\codex-tools\apache-maven-3.9.16\bin\mvn.cmd"
if (-not (Test-Path $mvn)) { $mvn = "mvn" }

Push-Location (Join-Path $PSScriptRoot "..\server")
try {
    $env:PORT = "$Port"
    if ($env:DATABASE_URL) {
        Write-Host "已检测到 DATABASE_URL，将连接外部 PostgreSQL 数据库"
    } else {
        Write-Host "未设置 DATABASE_URL，使用本地 H2 数据库（数据保存在 server/data/）"
    }
    Write-Host "启动端口: $Port （访问 http://localhost:$Port/）"
    & $mvn spring-boot:run
} finally {
    Pop-Location
}
