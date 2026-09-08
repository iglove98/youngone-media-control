param([ValidateSet("win-x64","win-arm64")][string]$Runtime="win-x64")
$projectRoot=Split-Path -Parent $PSScriptRoot
$workspaceRoot=Split-Path -Parent $projectRoot
$dotnet=Join-Path $workspaceRoot ".tools\dotnet\dotnet.exe"
if(-not(Test-Path -LiteralPath $dotnet)){throw ".NET SDK not found: $dotnet"}
$env:DOTNET_CLI_HOME=Join-Path $workspaceRoot ".dotnet-home"
$env:NUGET_PACKAGES=Join-Path $workspaceRoot ".nuget\packages"
$env:APPDATA=Join-Path $workspaceRoot ".appdata"
$env:LOCALAPPDATA=Join-Path $workspaceRoot ".localappdata"
$env:DOTNET_CLI_TELEMETRY_OPTOUT="1"
$out=Join-Path $projectRoot "artifacts\$Runtime"
& $dotnet publish (Join-Path $projectRoot "src\YoungOne.MediaControl.Agent\YoungOne.MediaControl.Agent.csproj") -c Release -r $Runtime -o (Join-Path $out "agent")
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
& $dotnet publish (Join-Path $projectRoot "src\YoungOne.MediaControl.UserUi\YoungOne.MediaControl.UserUi.csproj") -c Release -r $Runtime -o (Join-Path $out "ui")
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
& $dotnet publish (Join-Path $projectRoot "src\YoungOne.MediaControl.AgentCtl\YoungOne.MediaControl.AgentCtl.csproj") -c Release -r $Runtime -o (Join-Path $out "ctl")
exit $LASTEXITCODE
