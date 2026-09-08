# 36. Windows 매체 탐지와 드라이버 통신 계약

## 구현 범위

Windows Agent는 3초 간격으로 장치 재고를 비교해 다음 연결·제거 변화를 감지한다.

- USB 저장장치: Win32_DiskDrive InterfaceType=USB
- 휴대용 장치: WPD 및 SWD\WPDBUSENUM
- 광학매체: CDROM 분류
- 일반 USB 장치, 허브, 키보드, 마우스는 현재 이벤트 소스에서 제외

VID/PID는 PnP Device ID에서 4자리 16진수로 정규화한다. Serial은 영문·숫자 대문자로 정규화한 뒤 SHA-256만 전송하며 원문은 이벤트에 저장하지 않는다. SHA-256은 익명화가 아니라 가명처리이므로 접근통제 대상이다.

서비스의 Environment.UserName은 SYSTEM일 수 있으므로 실제 사용자로 사용하지 않는다. Win32_ComputerSystem.UserName에서 대화형 로그인 사용자를 읽으며 없으면 null로 기록한다.

## 오탐 방지

USB라는 이유만으로 통제 이벤트를 만들지 않는다. USB Hub나 HID가 fail-closed 정책에 들어가면 키보드·마우스 장애를 유발할 수 있기 때문이다. 저장장치로 확인된 장치만 현재 파이프라인에 전달한다.

## 드라이버 ABI v1

Public.h는 다음을 고정한다.

- Magic: YOMC
- ABI version: 1
- 최대 정책 payload: 1 MiB
- Policy version, rule count, payload length, SHA-256
- QUERY_STATUS 결과: 정책 적재 여부, 실제 enforcement 준비 여부, 마지막 NTSTATUS
- 장치 경로: \\.\YoungOneMediaControl
- 접근 권한: LocalSystem 및 Administrators만 허용

드라이버는 구조체 크기, Magic, ABI, payload 길이, rule count를 검사한다. 현재 EnforcementReady는 항상 false이며 실제 USB 차단을 성공으로 반환하지 않는다. 커널 내부 SHA-256 재검증과 필터 드라이버 차단은 다음 단계다.

## 검증

- C# Agent 및 테스트 빌드 성공
- 단위 테스트 6개 성공, 실패 0
- KMDF C 코드는 WDK 미설치로 아직 컴파일하지 못함
- 물리 USB 테스트는 테스트 서명 전용 VM에서 수행해야 함