[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$requiredDotnet=[Version]'10.0.302'
$requiredSdkBuild='10.0.28000.0'
$failures=[Collections.Generic.List[string]]::new()
Write-Host 'YoungOne Windows Agent prerequisite check'
$workspaceRoot=Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
$localDotnet=Join-Path $workspaceRoot '.tools\dotnet\dotnet.exe'
$dotnet=if(Test-Path -LiteralPath $localDotnet){Get-Item -LiteralPath $localDotnet}else{Get-Command dotnet -ErrorAction SilentlyContinue}
if(-not $dotnet){$failures.Add('.NET SDK command (dotnet) was not found.')}else{$sdks=@(& $dotnet.FullName --list-sdks);Write-Host "Installed .NET SDKs: $($sdks -join ', ')";$hasRequiredSdk=$sdks|Where-Object{$versionText=($_-split ' ')[0];try{[Version]$versionText-ge $requiredDotnet}catch{$false}};if(-not $hasRequiredSdk){$failures.Add(".NET SDK $requiredDotnet or newer is required.")}}
$ewdkVolume=[IO.DriveInfo]::GetDrives()|Where-Object{$_.IsReady-and$_.VolumeLabel-eq'EW_MULFREO_EN-US_DV9'}|Select-Object -First 1
if($ewdkVolume){$ewdkRoot=$ewdkVolume.RootDirectory.FullName.TrimEnd('\');$kitsRoot=Join-Path $ewdkRoot 'Program Files\Windows Kits\10';$setup=Join-Path $ewdkRoot 'BuildEnv\SetupBuildEnv.cmd';if(-not(Test-Path -LiteralPath $setup)){$failures.Add('Mounted EWDK build environment is incomplete.')}else{Write-Host "EWDK: $ewdkRoot (28000.2526)"}}
else{$programFilesX86=[Environment]::GetFolderPath('ProgramFilesX86');$vswhere=Join-Path $programFilesX86 'Microsoft Visual Studio\Installer\vswhere.exe';if(-not(Test-Path -LiteralPath $vswhere)){$failures.Add('Visual Studio Installer/vswhere was not found and EWDK is not mounted.')}else{$vsPath=& $vswhere -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath;if(-not $vsPath){$failures.Add('Visual Studio with Desktop development with C++ was not found.')}else{Write-Host "Visual Studio: $vsPath"}};$kitsRoot=Join-Path $programFilesX86 'Windows Kits\10'}
$wdkHeader=Join-Path $kitsRoot "Include\$requiredSdkBuild\km\wdm.h"
$fltHeader=Join-Path $kitsRoot "Include\$requiredSdkBuild\km\fltkernel.h"
$cngLibrary=Join-Path $kitsRoot "Lib\$requiredSdkBuild\km\x64\cng.lib"
if(-not(Test-Path -LiteralPath $wdkHeader)){$failures.Add("Matching Windows SDK/WDK headers $requiredSdkBuild were not found.")}
if(-not(Test-Path -LiteralPath $fltHeader)){$failures.Add('Filter Manager header fltkernel.h was not found.')}
if(-not(Test-Path -LiteralPath $cngLibrary)){$failures.Add('Kernel CNG library cng.lib was not found.')}
if($failures.Count-gt 0){Write-Host '';Write-Host 'NOT READY' -ForegroundColor Red;$failures|ForEach-Object{Write-Host "- $_"};exit 1}
Write-Host '';Write-Host 'READY: .NET and WDK driver build prerequisites were detected.' -ForegroundColor Green
