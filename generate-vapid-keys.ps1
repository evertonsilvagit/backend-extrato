param(
    [string]$Subject = "mailto:dev@extrato.local"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command npx -ErrorAction SilentlyContinue)) {
    throw "Comando obrigatório não encontrado: npx"
}

$raw = npx --yes web-push generate-vapid-keys --json
if ($LASTEXITCODE -ne 0) {
    throw "Falha ao gerar chaves VAPID."
}

$keys = $raw | ConvertFrom-Json

Write-Host "VAPID subject:" -ForegroundColor Cyan
Write-Host $Subject
Write-Host ""
Write-Host "VAPID public key:" -ForegroundColor Cyan
Write-Host $keys.publicKey
Write-Host ""
Write-Host "VAPID private key:" -ForegroundColor Cyan
Write-Host $keys.privateKey
Write-Host ""
Write-Host "Comando para subir o backend com push habilitado:" -ForegroundColor Green
Write-Host "C:\dev\projects\extrato\backend-extrato\run-backend.ps1 ``" 
Write-Host "  -VapidSubject `"$Subject`" ``"
Write-Host "  -VapidPublicKey `"$($keys.publicKey)`" ``"
Write-Host "  -VapidPrivateKey `"$($keys.privateKey)`""
