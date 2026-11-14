# ==============================================================================
# DREMLDS — DEVICE 1 (FRONTEND) STARTUP SCRIPT (WINDOWS 11)
# Starts a lightweight zero-dependency local web server on port 3000
# using Windows native .NET HttpListener and launches the browser.
# ==============================================================================

param(
    [int]$Port = 3000
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $scriptDir

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " DREMLDS — Starting Device 1 Frontend Command Center (Windows 11)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

# Check if config/config.js has been configured
$configFile = Join-Path $scriptDir "config\config.js"
if (Test-Path $configFile) {
    $configContent = Get-Content $configFile -Raw
    if ($configContent -match 'API_BASE_URL:\s*"([^"]+)"') {
        $backendUrl = $matches[1]
        Write-Host " Configured Backend (Device 2): $backendUrl" -ForegroundColor Green
    }
} else {
    Write-Host " Note: config/config.js not found, falling back to UI dynamic configuration." -ForegroundColor Yellow
}

$url = "http://localhost:$Port/"
Write-Host " Starting lightweight local HTTP server at: $url" -ForegroundColor Cyan
Write-Host " Press CTRL+C to stop the frontend server.`n" -ForegroundColor Gray

# Launch browser
Start-Process "http://localhost:$Port/src/index.html"

# Windows .NET native HttpListener (Zero software installation required!)
$listener = New-Object System.Net.HttpListener
$listener.Prefixes.Add("http://localhost:$Port/")
$listener.Prefixes.Add("http://127.0.0.1:$Port/")

try {
    $listener.Start()
} catch {
    Write-Host " [WARNING] Could not bind dual loopback prefixes ($($_.Exception.Message)). Falling back to port $Port single listener." -ForegroundColor Yellow
    $listener = New-Object System.Net.HttpListener
    $listener.Prefixes.Add("http://localhost:$Port/")
    $listener.Start()
}

$mimeTypes = @{
    ".html" = "text/html"
    ".css"  = "text/css"
    ".js"   = "application/javascript"
    ".json" = "application/json"
    ".png"  = "image/png"
    ".jpg"  = "image/jpeg"
    ".svg"  = "image/svg+xml"
    ".ico"  = "image/x-icon"
    ".txt"  = "text/plain"
}

try {
    while ($listener.IsListening) {
        $context = $listener.GetContext()
        $request = $context.Request
        $response = $context.Response

        $rawPath = $request.Url.LocalPath.TrimStart('/')
        if ([string]::IsNullOrEmpty($rawPath) -or $rawPath -eq "/") {
            $rawPath = "src/index.html"
        }

        # Normalize relative path
        $filePath = Join-Path $scriptDir $rawPath.Replace('/', [System.IO.Path]::DirectorySeparatorChar)

        if (Test-Path $filePath -PathType Leaf) {
            $ext = [System.IO.Path]::GetExtension($filePath).ToLower()
            $contentType = if ($mimeTypes.ContainsKey($ext)) { $mimeTypes[$ext] } else { "application/octet-stream" }
            $response.ContentType = $contentType
            $response.AddHeader("Access-Control-Allow-Origin", "*")

            $bytes = [System.IO.File]::ReadAllBytes($filePath)
            $response.ContentLength64 = $bytes.Length
            $response.OutputStream.Write($bytes, 0, $bytes.Length)
        } else {
            $response.StatusCode = 404
            $errBytes = [System.Text.Encoding]::UTF8.GetBytes("404 Not Found: $rawPath")
            $response.ContentLength64 = $errBytes.Length
            $response.OutputStream.Write($errBytes, 0, $errBytes.Length)
        }
        $response.OutputStream.Close()
    }
} finally {
    $listener.Stop()
    $listener.Close()
    Write-Host "`nFrontend server stopped." -ForegroundColor Yellow
}

