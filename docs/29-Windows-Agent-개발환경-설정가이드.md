# Windows Agent 개발환경·설정 가이드

기준일: 2026-08-31.

## 필수 도구

1. Windows 11 x64 개발 PC
2. Visual Studio 2026 Community 이상
3. Workload: Desktop development with C++, .NET desktop development
4. Individual Components: Windows Driver Kit, 최신 MSVC x64/x86 Spectre 완화 라이브러리
5. Windows SDK 10.0.28000.x와 WDK 28000.2526 — SDK/WDK 빌드 계열을 맞춘다
6. .NET 10 LTS SDK 10.0.302 이상 10.0 feature band
7. Git for Windows, PowerShell 7, 선택적으로 Windows Terminal
8. Sysinternals: Process Monitor, Process Explorer, Autoruns
9. WinDbg, Driver Verifier, WDK Test Target Setup
10. WiX Toolset 5 또는 회사 표준 MSI 도구(설치 프로젝트 구현 시)

현재 이 PC는 .NET 8 Runtime만 있고 .NET SDK·WDK가 없어 Agent/driver 빌드를 할 수 없다.

## Visual Studio 설정

- C#: nullable, warnings as errors, .NET analyzers latest-recommended, x64/ARM64 publish
- C++ driver: KMDF, WindowsKernelModeDriver10.0, `/W4`, warnings as errors, Spectre mitigation, SDL checks
- 저장소 문자셋 UTF-8, 줄바꿈 정책은 `.editorconfig`로 고정 예정
- NuGet lock file과 패키지 취약점 검사를 CI에 추가
- Driver INF의 TargetOSVersion과 실제 지원 Windows 버전을 릴리스 전에 확정

## 권장 개발 분담

- 일반적인 서버/API 작업: IntelliJ IDEA 또는 VS Code + Java 21
- C# Agent Service: Visual Studio 2026 또는 Rider
- C++ KMDF Driver: Visual Studio 2026 + WDK 필수
- DB 검증: MariaDB + DBeaver
- API 검증: Swagger UI, Bruno/Postman
- Git: 기능별 branch와 PR, 코드 변경 시 docs/CHANGELOG 동시 수정

## 개발·테스트 PC 분리

드라이버는 일상 개발 PC에 직접 설치하지 않는다. Host PC에서 빌드하고 별도 Target VM/PC에서 설치·디버깅한다. Microsoft 공식 안내처럼 Host와 Target의 Windows 버전을 맞추고 Visual Studio WDK 장치 프로비저닝을 사용한다.

권장 Target VM:

- Hyper-V Generation 2 Windows 11 VM
- checkpoint를 설치 전마다 생성
- 개발 전용 가상 스위치; 운영망/개인파일 접근 금지
- 초기 test-signing 단계에서는 Secure Boot 조건을 공식 절차에 맞게 조정
- WDK Test Target Setup 설치
- Driver Verifier는 YoungOne 드라이버에만 제한 적용
- 커널 크래시 dump와 WinDbg symbol 경로 설정

`bcdedit /set testsigning on`은 테스트 VM에서 관리자 권한으로만 사용하고 재부팅한다. 운영 PC에는 사용하지 않는다. 배포용 드라이버는 Partner Center/WHCP·WHQL 서명 절차를 거친다.

## 비밀과 로컬 설정

- bootstrap token을 appsettings.json, Git, 환경변수에 장기 저장하지 않는다
- 최초 설치 시 MSI custom action 또는 안전한 설치 채널로 전달하고 등록 성공 후 제거
- agentKey는 LocalMachine DPAPI, 장기적으로 TPM 비내보내기 키/mTLS
- 정책 공개키는 설치 패키지에 pin하고 서버 응답 PublicKey만 믿지 않는다
- `ServerBaseUrl`, 환경, 비밀이 아닌 동작값만 HKLM 레지스트리/서명 설정에 둔다
- 개발 HTTPS 인증서는 개발 VM에서만 신뢰

## 빌드 순서

```text
1. dotnet --info
2. dotnet restore agent-windows/YoungOne.MediaControl.Agent.slnx
3. dotnet build -c Debug
4. dotnet test
5. dotnet publish ... -c Release -r win-x64
6. Visual Studio에서 driver vcxproj x64 Debug 빌드
7. Test certificate로 SYS/CAT 서명
8. Target VM에 WDK deployment 또는 pnputil 설치
9. Driver Verifier·재부팅·장치 연결 테스트
```

## 디버깅

C# 서비스는 개발 중 Console로 실행하고, 서비스 설치 후 Event Viewer `YoungOne-MediaControl/Operational`과 dump를 사용한다. 커널 드라이버는 WinDbg/KDNET로 Target VM을 원격 디버깅한다. SetupAPI 로그, Device Manager, `sc query`, `pnputil /enum-drivers`를 함께 확인한다.

## 설치 권장값

- 서비스명 `YoungOneMediaControlAgent`
- 자동 지연 시작, 실패 시 제한적 재시작
- `%ProgramFiles%\YoungOne\MediaControl\Agent` 실행파일
- `%ProgramData%\YoungOne\MediaControl` DPAPI 데이터·SQLite 큐
- `HKLM\SOFTWARE\YoungOne\MediaControl\Agent` 비밀이 아닌 설정
- 서비스 SID/SYSTEM 전용 ACL
- 중앙 1회용 삭제 승인 토큰 없이는 정상 제거 차단

## 공식 자료

- .NET 10 LTS: https://dotnet.microsoft.com/en-us/download
- Windows Service: https://learn.microsoft.com/en-us/dotnet/core/extensions/windows-service
- WDK: https://learn.microsoft.com/en-us/windows-hardware/drivers/download-the-wdk
- Target provisioning: https://learn.microsoft.com/en-us/windows-hardware/drivers/gettingstarted/provision-a-target-computer
- Test signing: https://learn.microsoft.com/en-us/windows-hardware/drivers/install/test-signing-driver-packages
- Release signing: https://learn.microsoft.com/en-us/windows-hardware/drivers/develop/signing-a-driver-for-public-release
