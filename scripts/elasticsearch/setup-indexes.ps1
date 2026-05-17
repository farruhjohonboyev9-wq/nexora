param(
    [string]$EsUrl = "http://localhost:9200"
)

$ErrorActionPreference = "Stop"

$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$resourceDir = Join-Path $base "..\..\src\main\resources\elasticsearch"

$indexes = @(
    @{ Name = "users_search"; File = "users-index.json" },
    @{ Name = "posts_search"; File = "posts-index.json" },
    @{ Name = "hashtags_search"; File = "hashtags-index.json" }
)

foreach ($idx in $indexes) {
    $indexExists = $false
    try {
        Invoke-WebRequest -Method Head -Uri "$EsUrl/$($idx.Name)" -UseBasicParsing | Out-Null
        $indexExists = $true
    }
    catch {
        $statusCode = $null
        if ($_.Exception.Response) {
            $statusCode = [int]$_.Exception.Response.StatusCode
        }

        if ($statusCode -ne 404) {
            throw
        }
    }

    if ($indexExists) {
        Write-Host "Index exists: $($idx.Name)"
        continue
    }

    $body = Get-Content (Join-Path $resourceDir $idx.File) -Raw
    Invoke-RestMethod -Method Put -Uri "$EsUrl/$($idx.Name)" -ContentType "application/json" -Body $body | Out-Null
    Write-Host "Created index: $($idx.Name)"
}
