param(
    [string]$EsUrl = "http://localhost:9200"
)

$ErrorActionPreference = "Stop"

$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$dataDir = Join-Path $base "..\..\loadtest\sample-data"

$indexes = @("users_search", "posts_search", "hashtags_search")

foreach ($index in $indexes) {
    try {
        Invoke-WebRequest -Method Head -Uri "$EsUrl/$index" -UseBasicParsing | Out-Null
    }
    catch {
        throw "Index not found: $index"
    }
}

function Invoke-BulkLoad {
    param(
        [string]$Index,
        [string]$FileName
    )

    $payload = Get-Content (Join-Path $dataDir $FileName) -Raw
    Invoke-RestMethod -Method Post -Uri "$EsUrl/$Index/_bulk?refresh=true" -ContentType "application/x-ndjson" -Body $payload | Out-Null
    Write-Host "Seeded: $Index"
}

Invoke-BulkLoad -Index "users_search" -FileName "users.ndjson"
Invoke-BulkLoad -Index "posts_search" -FileName "posts.ndjson"
Invoke-BulkLoad -Index "hashtags_search" -FileName "hashtags.ndjson"

Write-Host "Sample data seeding completed."
