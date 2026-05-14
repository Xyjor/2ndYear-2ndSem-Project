param(
    [string] $PsqlPath = "C:\Program Files\PostgreSQL\18\bin\psql.exe",
    [string] $HostName = "localhost",
    [int] $Port = 5432,
    [string] $AdminUser = "postgres",
    [string] $DatabaseName = "company_dms",
    [string] $DatabaseUser = "company_app",
    [string] $DatabasePassword = "company_app_dev",
    [string] $AppUser = "manager",
    [string] $AppPassword = "ChangeMe123!",
    [string] $AppFullName = "System Manager"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path -LiteralPath $PsqlPath)) {
    throw "psql.exe was not found at '$PsqlPath'. Pass -PsqlPath with the installed PostgreSQL path."
}

$schemaPath = Join-Path $PSScriptRoot "schema.sql"
if (-not (Test-Path -LiteralPath $schemaPath)) {
    throw "schema.sql was not found at '$schemaPath'."
}

$securePassword = Read-Host "PostgreSQL password for user '$AdminUser'" -AsSecureString
$plainPassword = [Runtime.InteropServices.Marshal]::PtrToStringBSTR(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
)

try {
    $env:PGPASSWORD = $plainPassword

    & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "SELECT 1;" | Out-Null

    [string] $exists = & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -tAc "SELECT 1 FROM pg_database WHERE datname = '$DatabaseName';"
    if ([string]::IsNullOrWhiteSpace($exists)) {
        & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE DATABASE $DatabaseName;"
    }

    & $PsqlPath -h $HostName -p $Port -U $AdminUser -d $DatabaseName -v ON_ERROR_STOP=1 -f $schemaPath

    $escapedDatabaseUser = $DatabaseUser.Replace("'", "''")
    $escapedDatabasePassword = $DatabasePassword.Replace("'", "''")
    [string] $roleExists = & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -tAc "SELECT 1 FROM pg_roles WHERE rolname = '$escapedDatabaseUser';"
    if ([string]::IsNullOrWhiteSpace($roleExists)) {
        & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "CREATE ROLE $DatabaseUser WITH LOGIN PASSWORD '$escapedDatabasePassword';"
    } else {
        & $PsqlPath -h $HostName -p $Port -U $AdminUser -d postgres -v ON_ERROR_STOP=1 -c "ALTER ROLE $DatabaseUser WITH LOGIN PASSWORD '$escapedDatabasePassword';"
    }

    & $PsqlPath -h $HostName -p $Port -U $AdminUser -d $DatabaseName -v ON_ERROR_STOP=1 -c "GRANT CONNECT ON DATABASE $DatabaseName TO $DatabaseUser; GRANT USAGE ON SCHEMA public TO $DatabaseUser; GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO $DatabaseUser; GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO $DatabaseUser; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO $DatabaseUser; ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE, SELECT ON SEQUENCES TO $DatabaseUser;"

    $escapedAppUser = $AppUser.Replace("'", "''")
    $escapedAppPassword = $AppPassword.Replace("'", "''")
    $escapedAppFullName = $AppFullName.Replace("'", "''")
    $seedSql = @"
INSERT INTO app_users (username, password_hash, full_name, role)
VALUES ('$escapedAppUser', crypt('$escapedAppPassword', gen_salt('bf')), '$escapedAppFullName', 'MANAGER')
ON CONFLICT (username) DO UPDATE
SET password_hash = EXCLUDED.password_hash,
    full_name = EXCLUDED.full_name,
    role = EXCLUDED.role,
    active = TRUE,
    updated_at = now();
"@

    & $PsqlPath -h $HostName -p $Port -U $AdminUser -d $DatabaseName -v ON_ERROR_STOP=1 -c $seedSql

    Write-Host "Database '$DatabaseName' is ready."
    Write-Host "Database user: $DatabaseUser"
    Write-Host "Login user: $AppUser"
    Write-Host "Login password: $AppPassword"
}
finally {
    Remove-Item Env:\PGPASSWORD -ErrorAction SilentlyContinue
}
