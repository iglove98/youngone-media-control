# 37. 사용자 세션 팝업 및 VDI 설계

## 구조

Windows 서비스는 Session 0에서 실행되므로 MessageBox를 직접 표시하지 않는다. 별도 YoungOne.MediaControl.UserUi 프로세스가 각 로그인 세션에서 실행되고 세션 ID가 포함된 named pipe를 연다.

- Pipe: YoungOne.MediaControl.Notifications.{sessionId}
- 서비스: 활성 WTS 세션을 열거하고 각 세션 pipe에 알림 전달
- UI Helper: 자기 세션 pipe만 수신
- 설치: Common Startup 바로가기로 각 로그인 세션에서 UI Helper 시작

이 구조는 물리 PC의 단일 사용자와 VDI/RDS의 다중 사용자 세션을 분리한다. 한 세션의 팝업을 다른 세션에 잘못 귀속시키지 않는다.

## 보안

UI Helper는 연결 클라이언트를 impersonation하고 SID가 LocalSystem인지 확인한다. 일반 사용자가 같은 pipe에 연결해 가짜 보안 경고를 보내면 거부한다. 메시지에는 Serial, Device Instance ID, 사용자 ID를 넣지 않는다.

## 표시 원칙

- EnforcementApplied=true: 실제 통제가 적용되었다고 표시
- EnforcementApplied=false: 정책상 제한 요청이지만 드라이버가 적용하지 못했다고 명시
- 단순 Allow 이벤트에는 팝업을 표시하지 않음
- 서비스가 한 개 이상의 세션에 실제 전달한 경우에만 popupShown=true

## 빌드 및 배포

    .\agent-windows\tools\build-agent.ps1 test
    .\agent-windows\tools\publish-agent.ps1 win-x64

WiX 빌드 시 PublishDir은 Agent publish 폴더, UiPublishDir은 UI publish 폴더를 지정한다. WDK/WiX 도구가 설치되지 않아 MSI 자체 빌드는 아직 검증 전이다.

## 검증

Agent, UI Helper, 테스트 프로젝트 빌드 성공. 단위 테스트 8개 성공, 실패 0. 실제 다중 사용자 팝업 시험은 Windows Enterprise 다중 세션 또는 RDS 테스트 VM에서 수행한다.

## 0.14.0 publish 검증

win-x64 Agent와 UserUi publish가 성공했다. Agent는 SQLite와 libsodium 네이티브 DLL을 함께 배포해야 하며 UserUi는 단일 EXE로 생성된다.
