$ErrorActionPreference = "Stop"
if (-not $env:DB_PASSWORD) { throw "Defina DB_PASSWORD." }
Push-Location "$PSScriptRoot/.."
try { mvn spring-boot:run; if ($LASTEXITCODE -ne 0) { throw "Falha no backend" } } finally { Pop-Location }
