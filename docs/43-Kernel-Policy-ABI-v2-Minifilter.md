# Kernel Policy ABI v2와 Removable Minifilter 골격

## 구현 결과

0.20.0에서 Agent와 Control KMDF 사이 정책 형식을 JSON payload 전달 방식에서 고정 길이 binary rule table로 변경했다. 커널 hot path가 JSON, 문자열 할당, 문화권 변환을 수행하지 않게 하기 위함이다.

## ABI v2

Header는 56바이트를 유지한다.

- magic YOMC
- abiVersion 2
- policyVersion
- ruleCount
- payloadLength
- compiled payload SHA-256

각 rule은 64바이트다.

- mediaType: USB_STORAGE, PORTABLE_DEVICE, OPTICAL_MEDIA, BLUETOOTH의 정수 코드
- read/write/execute action
- selector flags
- VID/PID: 16-bit
- SerialHash: 32-byte SHA-256
- offlineBehavior
- offlineGraceSeconds

Agent builder는 서명 검증이 끝난 JSON을 binary table로 컴파일한다. 잘못된 VID/PID, SerialHash, 지원하지 않는 매체·동작, 빈 규칙은 드라이버 전송 전에 거부한다. 커널은 header, ABI, 전체 길이, 규칙 수, SHA-256, enum 범위를 재검증한다.

## 커널 정책 snapshot

새 규칙은 NonPagedPool에 별도 할당하고 전체 검증 후 wait lock 안에서 포인터를 교체한다. 실패하면 이전 정상 규칙을 유지한다. 판정기는 MediaType을 먼저 비교한 후 VID/PID/SerialHash 선택자가 모두 맞는 규칙 중 가장 구체적인 규칙을 선택한다. 일치 규칙이 없으면 DetectOnly다.

상태는 다음처럼 구분한다.

- PolicyLoaded: binary 규칙이 저장됨
- EvaluatorReady: 커널 판정기 사용 가능
- EnforcementReady: 실제 I/O filter 차단 가능

0.20.0은 앞의 두 상태만 가능하며 EnforcementReady는 계속 false다.

## YoungOneMediaFilter

별도 파일시스템 minifilter 프로젝트를 추가했다.

- FltRegisterFilter와 FltStartFiltering
- create/read/write/set-information/acquire-for-section-synchronization 등록
- disk file-system이며 FILE_REMOVABLE_MEDIA인 볼륨만 attach
- 현재 모든 I/O는 통과하는 detection-first gate

실제 ACCESS_DENIED 반환은 다음 조건이 모두 갖춰진 이후에만 활성화한다.

1. SYSTEM 전용 Filter Manager communication port
2. Volume GUID와 USB PnP/VID/PID/SerialHash 상관관계
3. ABI v2 rule snapshot 전달
4. 부팅/시스템/페이지파일/덤프 볼륨 제외
5. 정책 세대·만료·오프라인 상태 동기화
6. Driver Verifier와 VM 테스트 통과
7. 서명된 명시적 enforcement activation

## 테스트와 빌드

C# builder 테스트는 header, compiled rule, SHA-256, VID/PID/SerialHash, 잘못된 선택자와 미지원 매체를 검증한다. Agent 테스트는 20개 모두 통과했다.

EWDK 28000.2526을 구성한 뒤 Control KMDF와 Minifilter 모두 /W4 /WX /Qspectre로 컴파일·링크·INF/Inf2Cat 검증에 성공했다. 경고와 오류는 0이다. Driver Verifier와 실제 로드 검증은 별도 VM에서 수행해야 한다.

## 보안 주의

minifilter pre-operation callback이 I/O를 차단하려면 IoStatus를 설정하고 FLT_PREOP_COMPLETE를 반환해야 한다. 이 동작은 removable 식별과 정책 snapshot이 검증된 뒤에만 허용한다. 현재 코드는 의도적으로 FLT_PREOP_SUCCESS_NO_CALLBACK만 반환한다.
