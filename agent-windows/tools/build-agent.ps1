param([ValidateSet("restore","build","test")][string]$Task="test")
$projectRoot=Split-Path -Parent $PSScriptRoot
$workspaceRoot=Split-Path -Parent $projectRoot
$solution=Join-Path $projectRoot "YoungOne.MediaControl.Agent.slnx"
$dotnet=Join-Path $workspaceRoot ".tools\dotnet\dotnet.exe"
if(-not(Test-Path -LiteralPath $dotnet)){throw ".NET SDK not found: $dotnet"}
$env:DOTNET_CLI_HOME=Join-Path $workspaceRoot ".dotnet-home"
$env:NUGET_PACKAGES=Join-Path $workspaceRoot ".nuget\packages"
$env:APPDATA=Join-Path $workspaceRoot ".appdata"
$env:LOCALAPPDATA=Join-Path $workspaceRoot ".localappdata"
$env:DOTNET_CLI_TELEMETRY_OPTOUT="1"
New-Item -ItemType Directory -Force -Path $env:APPDATA,$env:LOCALAPPDATA|Out-Null
Push-Location -LiteralPath $projectRoot
try{
 if($Task-eq"restore"){& $dotnet restore $solution --configfile (Join-Path $projectRoot "NuGet.Config")}
 elseif($Task-eq"build"){& $dotnet build $solution}
 else{& $dotnet test $solution}
 $code=$LASTEXITCODE
}finally{Pop-Location}
exit $code
