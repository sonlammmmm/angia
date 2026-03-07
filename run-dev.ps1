# run-dev.ps1 — Chạy Spring Boot với env vars từ file .env
# Dùng: .\run-dev.ps1

Write-Host "Loading .env and starting Spring Boot..." -ForegroundColor Cyan

# Đọc từng dòng trong .env, bỏ qua comment và dòng trống
Get-Content .env | Where-Object { $_ -notmatch '^\s*#' -and $_ -match '=' } | ForEach-Object {
    $parts = $_ -split '=', 2
    $key   = $parts[0].Trim()
    $value = $parts[1].Trim()
    [System.Environment]::SetEnvironmentVariable($key, $value, 'Process')
    Write-Host "  SET $key" -ForegroundColor Gray
}

Write-Host ""
Write-Host "Starting ManagementApplication..." -ForegroundColor Green
.\mvnw.cmd spring-boot:run
