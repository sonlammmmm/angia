$ErrorActionPreference = "Continue"
$base = "http://localhost:8080/api/v1"

function Test-Endpoint {
    param($Method, $Path, $Body, $Token, $Label)
    Write-Host "`n=== $Label ===" -ForegroundColor Cyan
    Write-Host "$Method $Path"
    try {
        $headers = @{ "Content-Type" = "application/json" }
        if ($Token) { $headers["Authorization"] = "Bearer $Token" }
        $params = @{
            Method = $Method
            Uri = "$base$Path"
            Headers = $headers
            UseBasicParsing = $true
        }
        if ($Body) { $params["Body"] = $Body }
        $resp = Invoke-WebRequest @params
        Write-Host "STATUS: $($resp.StatusCode)" -ForegroundColor Green
        $resp.Content | ConvertFrom-Json | ConvertTo-Json -Depth 5
    } catch {
        $code = [int]$_.Exception.Response.StatusCode
        Write-Host "STATUS: $code" -ForegroundColor Red
        try {
            $stream = $_.Exception.Response.GetResponseStream()
            $reader = New-Object System.IO.StreamReader($stream)
            $reader.ReadToEnd()
        } catch { Write-Host $_.Exception.Message }
    }
}

# 1. LOGIN
Test-Endpoint -Method POST -Path "/auth/login" -Body '{"username":"admin","password":"admin123"}' -Label "AUTH LOGIN (admin)"
