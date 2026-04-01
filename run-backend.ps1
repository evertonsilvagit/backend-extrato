param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 5432,
    [string]$DbName = "extrato",
    [string]$DbUser = "extrato",
    [string]$DbPassword = "extrato123",
    [string]$JwtSecret = "extrato-dev-secret-2026",
    [string]$VapidSubject = "mailto:dev@extrato.local",
    [string]$VapidPublicKey = "",
    [string]$VapidPrivateKey = "",
    [string]$OtelEndpoint = "http://localhost:4318",
    [string]$OtelEnvironment = "local",
    [switch]$EnableOtel
)

$ErrorActionPreference = "Stop"

function Read-DotEnvFile {
    param([string]$Path)

    $values = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        return $values
    }

    foreach ($line in Get-Content -LiteralPath $Path) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#")) {
            continue
        }

        $separatorIndex = $trimmed.IndexOf("=")
        if ($separatorIndex -lt 1) {
            continue
        }

        $key = $trimmed.Substring(0, $separatorIndex).Trim()
        $value = $trimmed.Substring($separatorIndex + 1).Trim()

        if (
            ($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'"))
        ) {
            $value = $value.Substring(1, $value.Length - 2)
        }

        $values[$key] = $value
    }

    return $values
}

function Resolve-Setting {
    param(
        [string]$ParameterName,
        [string]$EnvKey,
        [string]$Fallback
    )

    if ($PSBoundParameters.ContainsKey($ParameterName)) {
        return (Get-Variable -Name $ParameterName -ValueOnly)
    }

    if ($dotenvValues.ContainsKey($EnvKey) -and -not [string]::IsNullOrWhiteSpace($dotenvValues[$EnvKey])) {
        return $dotenvValues[$EnvKey]
    }

    return $Fallback
}

$dotenvPath = Join-Path $PSScriptRoot ".env.local"
$dotenvValues = Read-DotEnvFile -Path $dotenvPath

$DbHost = Resolve-Setting -ParameterName "DbHost" -EnvKey "DB_HOST" -Fallback $DbHost
$DbPort = [int](Resolve-Setting -ParameterName "DbPort" -EnvKey "DB_PORT" -Fallback "$DbPort")
$DbName = Resolve-Setting -ParameterName "DbName" -EnvKey "DB_NAME" -Fallback $DbName
$DbUser = Resolve-Setting -ParameterName "DbUser" -EnvKey "DB_USER" -Fallback $DbUser
$DbPassword = Resolve-Setting -ParameterName "DbPassword" -EnvKey "DB_PASSWORD" -Fallback $DbPassword
$JwtSecret = Resolve-Setting -ParameterName "JwtSecret" -EnvKey "AUTH_JWT_SECRET" -Fallback $JwtSecret
$VapidSubject = Resolve-Setting -ParameterName "VapidSubject" -EnvKey "PUSH_VAPID_SUBJECT" -Fallback $VapidSubject
$VapidPublicKey = Resolve-Setting -ParameterName "VapidPublicKey" -EnvKey "PUSH_VAPID_PUBLIC_KEY" -Fallback $VapidPublicKey
$VapidPrivateKey = Resolve-Setting -ParameterName "VapidPrivateKey" -EnvKey "PUSH_VAPID_PRIVATE_KEY" -Fallback $VapidPrivateKey
$OtelEndpoint = Resolve-Setting -ParameterName "OtelEndpoint" -EnvKey "OTEL_EXPORTER_OTLP_ENDPOINT" -Fallback $OtelEndpoint
$OtelEnvironment = Resolve-Setting -ParameterName "OtelEnvironment" -EnvKey "OTEL_DEPLOYMENT_ENV" -Fallback $OtelEnvironment

$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://${DbHost}:${DbPort}/${DbName}"
$env:SPRING_DATASOURCE_USERNAME = $DbUser
$env:SPRING_DATASOURCE_PASSWORD = $DbPassword
$env:AUTH_JWT_SECRET = $JwtSecret
$env:PUSH_VAPID_SUBJECT = $VapidSubject
$env:PUSH_VAPID_PUBLIC_KEY = $VapidPublicKey
$env:PUSH_VAPID_PRIVATE_KEY = $VapidPrivateKey
$env:OTEL_SERVICE_NAME = "backend-extrato"
$env:OTEL_SERVICE_VERSION = "0.0.1-SNAPSHOT"
$env:OTEL_DEPLOYMENT_ENV = $OtelEnvironment

if ($EnableOtel) {
    $env:OTEL_EXPORTER_OTLP_ENDPOINT = $OtelEndpoint
    $env:OTEL_EXPORTER_OTLP_PROTOCOL = "http/protobuf"
    $env:OTEL_TRACES_EXPORTER = "otlp"
    $env:OTEL_METRICS_EXPORTER = "otlp"
    $env:OTEL_LOGS_EXPORTER = "otlp"
}
else {
    $env:OTEL_EXPORTER_OTLP_ENDPOINT = $OtelEndpoint
    $env:OTEL_EXPORTER_OTLP_PROTOCOL = "http/protobuf"
    $env:OTEL_TRACES_EXPORTER = "none"
    $env:OTEL_METRICS_EXPORTER = "none"
    $env:OTEL_LOGS_EXPORTER = "none"
}

Write-Host "Starting backend-extrato on http://localhost:8083" -ForegroundColor Cyan
Write-Host "Database: $($env:SPRING_DATASOURCE_URL)" -ForegroundColor DarkGray
Write-Host "Database user: $($env:SPRING_DATASOURCE_USERNAME)" -ForegroundColor DarkGray
Write-Host "Dotenv file: $dotenvPath" -ForegroundColor DarkGray
Write-Host "OpenTelemetry endpoint: $($env:OTEL_EXPORTER_OTLP_ENDPOINT)" -ForegroundColor DarkGray
Write-Host "OpenTelemetry exporters: traces=$($env:OTEL_TRACES_EXPORTER), metrics=$($env:OTEL_METRICS_EXPORTER), logs=$($env:OTEL_LOGS_EXPORTER)" -ForegroundColor DarkGray

& "$PSScriptRoot\gradlew.bat" bootRun
