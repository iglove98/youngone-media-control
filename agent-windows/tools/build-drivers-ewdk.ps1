[CmdletBinding()]
param([ValidateSet("Release","Debug")][string]$Configuration="Release",[ValidateSet("x64")][string]$Platform="x64",[switch]$Signed)
$ErrorActionPreference="Stop"
$projectRoot=Split-Path -Parent $PSScriptRoot
$workspaceRoot=Split-Path -Parent $projectRoot
$iso=Join-Path $workspaceRoot ".tools\EWDK_28000_202607.iso"
$volume=[IO.DriveInfo]::GetDrives()|Where-Object{$_.IsReady-and$_.VolumeLabel-eq"EW_MULFREO_EN-US_DV9"}|Select-Object -First 1
if(-not $volume){if(-not(Test-Path -LiteralPath $iso)){throw "EWDK ISO not found: $iso"};throw "Mount the EWDK ISO first: Mount-DiskImage -ImagePath $iso"}
$drive=$volume.RootDirectory.FullName.TrimEnd('\')
$setup=Join-Path $drive "BuildEnv\SetupBuildEnv.cmd"
$signMode=if($Signed){"TestSign"}else{"Off"}
$projects=@((Join-Path $projectRoot "driver\YoungOneMediaControl\YoungOneMediaControl.vcxproj"),(Join-Path $projectRoot "driver\YoungOneMediaFilter\YoungOneMediaFilter.vcxproj"))
foreach($project in $projects){
 $arguments="/d /s /c `"call `"$setup`" amd64 && msbuild `"$project`" /t:Rebuild /p:Configuration=$Configuration /p:Platform=$Platform /p:SignMode=$signMode /p:Inf2CatUseLocalTime=true /m`""
 $process=Start-Process -FilePath "$env:SystemRoot\System32\cmd.exe" -ArgumentList $arguments -Wait -NoNewWindow -PassThru
 if($process.ExitCode-ne 0){exit $process.ExitCode}
}