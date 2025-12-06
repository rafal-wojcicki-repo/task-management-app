<#
PowerShell helper: set Heroku SPRING datasource env vars from a Postgres DATABASE_URL
Usage:
  1) If your DB is an addon in Heroku and Heroku already has DATABASE_URL set:
     .\set-heroku-db.ps1 -AppName task-management-app-v1

  2) If you have an external DATABASE_URL (from Supabase, ElephantSQL etc):
     .\set-heroku-db.ps1 -AppName task-management-app-v1 -DatabaseUrl "postgres://user:pass@host:5432/dbname"

Notes:
 - Requires Heroku CLI and you're logged in (heroku login).
 - Will set: SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD, SPRING_JPA_HBM2DDL=update
 - Adjust SPRING_JPA_HBM2DDL as desired (update/none)
#>
param(
    [Parameter(Mandatory=$true)]
    [string]$AppName,

    [Parameter(Mandatory=$false)]
    [string]$DatabaseUrl
)

function Parse-PostgresUrl {
    param([string]$url)
    # url like: postgres://user:pass@host:port/dbname
    try {
        $uri = [System.Uri]$url
    } catch {
        throw "Invalid URL: $url"
    }
    $userInfo = $uri.UserInfo.Split(':')
    $user = $userInfo[0]
    $pass = $userInfo[1]
    $host = $uri.Host
    $port = $uri.Port
    $db = $uri.AbsolutePath.TrimStart('/')
    $jdbc = "jdbc:postgresql://$($host):$($port)/$db"
    return @{jdbc=$jdbc; user=$user; pass=$pass}
}

if (-not $DatabaseUrl) {
    Write-Host "No DatabaseUrl provided. Trying to read DATABASE_URL from Heroku for app '$AppName'..."
    $DatabaseUrl = heroku config:get DATABASE_URL -a $AppName 2>$null
    if (-not $DatabaseUrl) {
        Write-Error "Could not read DATABASE_URL from Heroku. Provide -DatabaseUrl or create/provision a DB first."
        exit 1
    }
    Write-Host "Found DATABASE_URL: $DatabaseUrl"
}

$parsed = Parse-PostgresUrl -url $DatabaseUrl
$jdbc = $parsed['jdbc']
$user = $parsed['user']
$pass = $parsed['pass']

Write-Host "Setting Heroku config vars for app: $AppName"
heroku config:set SPRING_DATASOURCE_URL="$jdbc" SPRING_DATASOURCE_USERNAME="$user" SPRING_DATASOURCE_PASSWORD="$pass" SPRING_JPA_HBM2DDL=update -a $AppName

Write-Host "Done. Your Heroku app '$AppName' now has SPRING_DATASOURCE_URL set to $jdbc"
Write-Host "Redeploy your container:"
Write-Host "  heroku container:push web -a $AppName"
Write-Host "  heroku container:release web -a $AppName"
Write-Host "Then check logs: heroku logs --tail -a $AppName"
