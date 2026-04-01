param(
    [string]$SourceConnectionString,
    [string]$SourceHost,
    [int]$SourcePort = 5432,
    [string]$SourceDatabase,
    [string]$SourceUser,
    [string]$SourcePassword,
    [string]$SourceSslMode = "require",

    [string]$LocalHost = "localhost",
    [int]$LocalPort = 5432,
    [string]$LocalDatabase = "extrato",
    [string]$LocalUser = "extrato",
    [string]$LocalPassword = "extrato123",
    [string]$LocalSslMode = "disable",

    [string]$DumpDirectory = "$PSScriptRoot\.tmp\db-migration",
    [string]$DockerImage = "postgres:17-alpine",

    [switch]$BackupLocalFirst,
    [switch]$KeepDumpFile,
    [switch]$SkipRestore
)

$ErrorActionPreference = "Stop"
$PSNativeCommandUseErrorActionPreference = $false

function Test-CommandExists {
    param([string]$CommandName)
    return [bool](Get-Command $CommandName -ErrorAction SilentlyContinue)
}

function Assert-CommandExists {
    param([string]$CommandName)
    if (-not (Test-CommandExists -CommandName $CommandName)) {
        throw "Comando obrigatório não encontrado: $CommandName"
    }
}

function New-DirectoryIfMissing {
    param([string]$Path)
    if (-not (Test-Path -LiteralPath $Path)) {
        New-Item -ItemType Directory -Path $Path -Force | Out-Null
    }
}

function Parse-PostgresConnectionString {
    param([string]$ConnectionString)

    if ([string]::IsNullOrWhiteSpace($ConnectionString)) {
        return $null
    }

    $builder = [System.UriBuilder]::new($ConnectionString)
    $database = $builder.Path.TrimStart('/')
    $queryValues = @{}

    if ($builder.Query) {
        foreach ($pair in $builder.Query.TrimStart('?').Split('&', [System.StringSplitOptions]::RemoveEmptyEntries)) {
            $parts = $pair.Split('=', 2)
            $key = [System.Uri]::UnescapeDataString($parts[0])
            $value = if ($parts.Length -gt 1) { [System.Uri]::UnescapeDataString($parts[1]) } else { "" }
            $queryValues[$key] = $value
        }
    }

    return @{
        Host = $builder.Host
        Port = if ($builder.Port -gt 0) { $builder.Port } else { 5432 }
        Database = $database
        User = $builder.UserName
        Password = $builder.Password
        SslMode = if ($queryValues.ContainsKey("sslmode")) { $queryValues["sslmode"] } else { "require" }
        ChannelBinding = if ($queryValues.ContainsKey("channel_binding")) { $queryValues["channel_binding"] } else { $null }
    }
}

function Build-PostgresConnectionString {
    param(
        [string]$HostName,
        [int]$Port,
        [string]$Database,
        [string]$User,
        [string]$Password,
        [string]$SslMode,
        [string]$ChannelBinding
    )

    $encodedUser = [System.Uri]::EscapeDataString($User)
    $encodedPassword = [System.Uri]::EscapeDataString($Password)
    $queryParts = @()

    if ($SslMode) {
        $queryParts += "sslmode=$([System.Uri]::EscapeDataString($SslMode))"
    }

    if ($ChannelBinding) {
        $queryParts += "channel_binding=$([System.Uri]::EscapeDataString($ChannelBinding))"
    }

    $queryString = if ($queryParts.Count -gt 0) { "?" + ($queryParts -join "&") } else { "" }
    return "postgresql://${encodedUser}:${encodedPassword}@${HostName}:${Port}/${Database}${queryString}"
}

function Get-DockerAwareHost {
    param(
        [string]$HostName,
        [bool]$UsingDocker
    )

    if (-not $UsingDocker) {
        return $HostName
    }

    if ($HostName -in @("localhost", "127.0.0.1", "::1", "[::1]")) {
        return "host.docker.internal"
    }

    return $HostName
}

function Invoke-ToolCommand {
    param(
        [string]$Tool,
        [string[]]$Arguments,
        [string]$Password,
        [bool]$UsingDocker,
        [string]$DockerImageName,
        [string]$MountedDirectory
    )

    $previousPassword = $env:PGPASSWORD
    try {
        if ($UsingDocker) {
            Assert-CommandExists -CommandName "docker"
            $dockerArgs = @(
                "run", "--rm",
                "-e", "PGPASSWORD=$Password",
                "-v", "${MountedDirectory}:/work",
                "-w", "/work",
                $DockerImageName,
                $Tool
            ) + $Arguments
            & docker @dockerArgs
        }
        else {
            $env:PGPASSWORD = $Password
            & $Tool @Arguments
        }

        if ($LASTEXITCODE -ne 0) {
            throw "O comando PostgreSQL falhou com exit code $LASTEXITCODE."
        }
    }
    finally {
        $env:PGPASSWORD = $previousPassword
    }
}

function Ensure-LocalDatabaseExists {
    param(
        [string]$HostName,
        [int]$Port,
        [string]$Database,
        [string]$User,
        [string]$Password,
        [bool]$UsingDocker,
        [string]$DockerImageName,
        [string]$MountedDirectory
    )

    $effectiveHost = Get-DockerAwareHost -HostName $HostName -UsingDocker $UsingDocker
    $checkSql = "SELECT 1 FROM pg_database WHERE datname = '$Database';"

    $exists = Invoke-ToolCommandCapture `
        -Tool "psql" `
        -Arguments @(
            "--host=$effectiveHost",
            "--port=$Port",
            "--username=$User",
            "--dbname=postgres",
            "--tuples-only",
            "--no-align",
            "--command=$checkSql"
        ) `
        -Password $Password `
        -UsingDocker $UsingDocker `
        -DockerImageName $DockerImageName `
        -MountedDirectory $MountedDirectory

    if ($exists.Trim() -eq "1") {
        return
    }

    Write-Host "Criando banco local '$Database'..." -ForegroundColor Cyan

    Invoke-ToolCommand `
        -Tool "psql" `
        -Arguments @(
            "--host=$effectiveHost",
            "--port=$Port",
            "--username=$User",
            "--dbname=postgres",
            "--command=CREATE DATABASE `"$Database`";"
        ) `
        -Password $Password `
        -UsingDocker $UsingDocker `
        -DockerImageName $DockerImageName `
        -MountedDirectory $MountedDirectory
}

function Invoke-ToolCommandCapture {
    param(
        [string]$Tool,
        [string[]]$Arguments,
        [string]$Password,
        [bool]$UsingDocker,
        [string]$DockerImageName,
        [string]$MountedDirectory,
        [switch]$AllowNonZeroExitCode
    )

    $previousPassword = $env:PGPASSWORD
    $tempStdOutPath = Join-Path $MountedDirectory ("tool-output-" + [System.Guid]::NewGuid().ToString("N") + ".out.log")
    $tempStdErrPath = Join-Path $MountedDirectory ("tool-output-" + [System.Guid]::NewGuid().ToString("N") + ".err.log")
    try {
        if ($UsingDocker) {
            Assert-CommandExists -CommandName "docker"
            $dockerArgs = @(
                "run", "--rm",
                "-e", "PGPASSWORD=$Password",
                "-v", "${MountedDirectory}:/work",
                "-w", "/work",
                $DockerImageName,
                $Tool
            ) + $Arguments
            $process = Start-Process -FilePath "docker" -ArgumentList $dockerArgs -NoNewWindow -Wait -PassThru -RedirectStandardOutput $tempStdOutPath -RedirectStandardError $tempStdErrPath
            $script:LASTEXITCODE = $process.ExitCode
        }
        else {
            $env:PGPASSWORD = $Password
            $process = Start-Process -FilePath $Tool -ArgumentList $Arguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $tempStdOutPath -RedirectStandardError $tempStdErrPath
            $script:LASTEXITCODE = $process.ExitCode
        }

        $stdout = if (Test-Path -LiteralPath $tempStdOutPath) { Get-Content -LiteralPath $tempStdOutPath -Raw } else { "" }
        $stderr = if (Test-Path -LiteralPath $tempStdErrPath) { Get-Content -LiteralPath $tempStdErrPath -Raw } else { "" }
        $output = ($stdout + [Environment]::NewLine + $stderr).Trim()

        if ($LASTEXITCODE -ne 0 -and -not $AllowNonZeroExitCode) {
            throw "O comando PostgreSQL falhou com exit code $LASTEXITCODE."
        }

        return $output
    }
    finally {
        if (Test-Path -LiteralPath $tempStdOutPath) { Remove-Item -LiteralPath $tempStdOutPath -Force -ErrorAction SilentlyContinue }
        if (Test-Path -LiteralPath $tempStdErrPath) { Remove-Item -LiteralPath $tempStdErrPath -Force -ErrorAction SilentlyContinue }
        $env:PGPASSWORD = $previousPassword
    }
}

if ($SourceConnectionString) {
    $parsedConnection = Parse-PostgresConnectionString -ConnectionString $SourceConnectionString
    $SourceHost = $parsedConnection.Host
    $SourcePort = $parsedConnection.Port
    $SourceDatabase = $parsedConnection.Database
    $SourceUser = $parsedConnection.User
    $SourcePassword = $parsedConnection.Password
    $SourceSslMode = $parsedConnection.SslMode
    $SourceChannelBinding = $parsedConnection.ChannelBinding
}

if (-not $SourceHost) { throw "Informe -SourceHost ou -SourceConnectionString." }
if (-not $SourceDatabase) { throw "Informe -SourceDatabase ou -SourceConnectionString." }
if (-not $SourceUser) { throw "Informe -SourceUser ou -SourceConnectionString." }
if (-not $SourcePassword) { throw "Informe -SourcePassword ou -SourceConnectionString." }

New-DirectoryIfMissing -Path $DumpDirectory
$ResolvedDumpDirectory = (Resolve-Path -LiteralPath $DumpDirectory).Path

$UseDockerPgTools = -not (
    (Test-CommandExists -CommandName "pg_dump") -and
    (Test-CommandExists -CommandName "pg_restore") -and
    (Test-CommandExists -CommandName "psql")
)

if ($UseDockerPgTools) {
    Assert-CommandExists -CommandName "docker"
    Write-Host "pg_dump/pg_restore/psql não encontrados localmente. Usando ferramentas PostgreSQL via Docker." -ForegroundColor Yellow
}

$timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
$remoteDumpFileName = "neon-$($SourceDatabase)-$timestamp.dump"
$localBackupFileName = "local-$($LocalDatabase)-before-$timestamp.dump"
$remoteDumpPath = Join-Path $ResolvedDumpDirectory $remoteDumpFileName
$localBackupPath = Join-Path $ResolvedDumpDirectory $localBackupFileName

Write-Host "Exportando banco remoto..." -ForegroundColor Cyan
Write-Host "Origem: ${SourceHost}:${SourcePort}/$SourceDatabase" -ForegroundColor DarkGray
Write-Host "Destino local: ${LocalHost}:${LocalPort}/$LocalDatabase" -ForegroundColor DarkGray

$effectiveSourceHost = Get-DockerAwareHost -HostName $SourceHost -UsingDocker $UseDockerPgTools
$effectiveSourceConnectionString = Build-PostgresConnectionString `
    -HostName $effectiveSourceHost `
    -Port $SourcePort `
    -Database $SourceDatabase `
    -User $SourceUser `
    -Password $SourcePassword `
    -SslMode $SourceSslMode `
    -ChannelBinding $SourceChannelBinding

$dumpArgs = @(
    "--dbname=$effectiveSourceConnectionString",
    "--format=custom",
    "--no-owner",
    "--no-privileges",
    "--file=/work/$remoteDumpFileName",
    "--verbose"
)

Invoke-ToolCommand `
    -Tool "pg_dump" `
    -Arguments $dumpArgs `
    -Password $SourcePassword `
    -UsingDocker $UseDockerPgTools `
    -DockerImageName $DockerImage `
    -MountedDirectory $ResolvedDumpDirectory

Write-Host "Dump remoto salvo em $remoteDumpPath" -ForegroundColor Green

if ($SkipRestore) {
    Write-Host "Restauração local ignorada por opção." -ForegroundColor Yellow
    return
}

Ensure-LocalDatabaseExists `
    -HostName $LocalHost `
    -Port $LocalPort `
    -Database $LocalDatabase `
    -User $LocalUser `
    -Password $LocalPassword `
    -UsingDocker $UseDockerPgTools `
    -DockerImageName $DockerImage `
    -MountedDirectory $ResolvedDumpDirectory

if ($BackupLocalFirst) {
    Write-Host "Gerando backup do banco local antes da restauração..." -ForegroundColor Cyan
    $effectiveLocalHost = Get-DockerAwareHost -HostName $LocalHost -UsingDocker $UseDockerPgTools
    $effectiveLocalConnectionString = Build-PostgresConnectionString `
        -HostName $effectiveLocalHost `
        -Port $LocalPort `
        -Database $LocalDatabase `
        -User $LocalUser `
        -Password $LocalPassword `
        -SslMode $LocalSslMode `
        -ChannelBinding $null

    Invoke-ToolCommand `
        -Tool "pg_dump" `
        -Arguments @(
            "--dbname=$effectiveLocalConnectionString",
            "--format=custom",
            "--no-owner",
            "--no-privileges",
            "--file=/work/$localBackupFileName",
            "--verbose"
        ) `
        -Password $LocalPassword `
        -UsingDocker $UseDockerPgTools `
        -DockerImageName $DockerImage `
        -MountedDirectory $ResolvedDumpDirectory

    Write-Host "Backup local salvo em $localBackupPath" -ForegroundColor Green
}

Write-Host "Restaurando dump no banco local..." -ForegroundColor Cyan
$effectiveLocalHost = Get-DockerAwareHost -HostName $LocalHost -UsingDocker $UseDockerPgTools
$effectiveLocalConnectionString = Build-PostgresConnectionString `
    -HostName $effectiveLocalHost `
    -Port $LocalPort `
    -Database $LocalDatabase `
    -User $LocalUser `
    -Password $LocalPassword `
    -SslMode $LocalSslMode `
    -ChannelBinding $null

$restoreOutput = Invoke-ToolCommandCapture `
    -Tool "pg_restore" `
    -Arguments @(
        "--dbname=$effectiveLocalConnectionString",
        "--clean",
        "--if-exists",
        "--no-owner",
        "--no-privileges",
        "--verbose",
        "/work/$remoteDumpFileName"
    ) `
    -Password $LocalPassword `
    -UsingDocker $UseDockerPgTools `
    -DockerImageName $DockerImage `
    -MountedDirectory $ResolvedDumpDirectory `
    -AllowNonZeroExitCode

$restoreText = $restoreOutput | Out-String
if ($restoreText) {
    Write-Host $restoreText.TrimEnd()
}

$isKnownPgCompatibilityWarning = $restoreText -match 'unrecognized configuration parameter "transaction_timeout"' -and $restoreText -match 'errors ignored on restore: 1'

if ($LASTEXITCODE -ne 0 -and -not $isKnownPgCompatibilityWarning) {
    throw "O comando PostgreSQL falhou com exit code $LASTEXITCODE."
}

if ($isKnownPgCompatibilityWarning) {
    Write-Host "Aviso conhecido ignorado: dump do Postgres 17 restaurado em banco local com parâmetro transaction_timeout não suportado." -ForegroundColor Yellow
}

Write-Host "Migração concluída com sucesso." -ForegroundColor Green

if (-not $KeepDumpFile) {
    Remove-Item -LiteralPath $remoteDumpPath -Force
    Write-Host "Dump temporário removido." -ForegroundColor DarkGray
}
