# 40. 서버 정책의 Agent·드라이버 전달경로

## 처리 흐름

1. Agent가 서버에서 서명 정책을 HTTPS로 수신한다.
2. 고정된 Ed25519 공개키로 서명을 검증한다.
3. payload SHA-256, 정책 버전, 유효기간을 검증한다.
4. DPAPI LocalMachine 정책 캐시에 원자적으로 저장한다.
5. DriverPolicyPacketBuilder가 ABI v1 바이너리 패킷을 생성한다.
6. DriverControlClient가 IOCTL_YOMC_SET_POLICY로 드라이버에 전달한다.
7. 결과를 Agent 운영로그와 Heartbeat 제어 상태에 반영한다.

정책 캐시 저장이 성공하고 드라이버 전달이 실패하면 정책은 보존되지만 서버 상태는 DRIVER_ERROR다.

## ABI v1 패킷

고정 56바이트 Header 뒤에 UTF-8 정책 JSON을 배치한다.

| Offset | 크기 | 필드 |
|---|---:|---|
| 0 | 4 | Magic YOMC |
| 4 | 4 | ABI Version 1 |
| 8 | 8 | Policy Version |
| 16 | 4 | Rule Count |
| 20 | 4 | Payload Length |
| 24 | 32 | SHA-256 |
| 56 | 가변 | UTF-8 Policy JSON |

최대 payload는 1 MiB다. 규칙이 없거나 Hash 길이가 올바르지 않거나 최대 크기를 초과하면 Agent 단계에서 전달하지 않는다.

## 현재 안전 상태

KMDF 제어 드라이버는 구조체 크기, Magic, ABI, 길이, 규칙 수를 검사한다. 정책 메타데이터를 상태에 기록하지만 실제 필터 차단은 아직 구현하지 않았으므로 SET_POLICY 요청을 STATUS_NOT_SUPPORTED로 완료한다.

따라서 다음을 보장한다.

- 정책 수신 성공과 실제 차단 성공을 혼동하지 않음
- EnforcementReady=false 유지
- 서버 콘솔에 DRIVER_ERROR 표시
- 사용자 팝업도 실제 적용되지 않았음을 명시
- WDK 및 테스트 VM 검증 전 운영 차단 활성화 금지

## 다음 구현 조건

실제 차단을 활성화하기 전에 커널 SHA-256 재검증, 정책 double-buffer 교체, USBSTOR 디스크 식별, 읽기·쓰기·포맷 요청 구분, 키보드·부팅 디스크 안전장치, Watchdog와 rollback이 필요하다.

## 검증

Agent/UI 빌드 성공. 단위 테스트 14개 성공, 실패 0. ABI Header offset, Magic, 버전, 정책 버전, 규칙 수, payload 길이와 원문 복원을 검증했다.

## ABI v2 변경 (0.20.0)

기존 ABI v1의 UTF-8 JSON payload는 폐기하고 Agent가 검증된 정책을 64바이트 고정 rule table로 컴파일한다. Header는 56바이트를 유지하되 abiVersion=2이고 payloadHash는 compiled binary payload의 SHA-256이다. 커널은 JSON을 파싱하지 않는다.
