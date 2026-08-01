# ============================================================
# 文峰小馆 · 本地开发启动脚本
# 用法：.\server\run-dev.ps1
# 可选：先设置 DATABASE_URL / DB_DRIVER / DB_USERNAME / DB_PASSWORD 以连接 PostgreSQL
# ============================================================
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
    if ($env:DATABASE_URL) {
        Write-Host "已检测到 DATABASE_URL，将连接外部 PostgreSQL 数据库"
    } else {
        Write-Host "未设置 DATABASE_URL，使用本地 H2 数据库（数据保存在 server/data/）"
    }
    & $mvn spring-boot:run
} finally {
    Pop-Location
}
