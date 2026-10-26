$ErrorActionPreference = "Stop"
Push-Location "$PSScriptRoot/.."
try { mvn verify; if ($LASTEXITCODE -ne 0) { throw "Validacao falhou" } } finally { Pop-Location }
