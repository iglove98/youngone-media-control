# 34. Agent 엔진 0.12 구현 상태

## 이번 구현

- 서명 검증 후 저장된 정책 schemaVersion 2를 읽어 매체 종류, VID, PID, Serial Hash를 가장 구체적인 규칙 우선으로 판정한다.
- 읽기/쓰기/실행/포맷 행위를 서로 다른 정책 동작으로 판정한다.
- 정책 없음, 손상, 적용 전, 만료, 미지원 스키마는 필수 키보드·마우스만 허용하고 나머지는 fail-closed 판정한다.
- SQLite 로컬 위험 이벤트 큐는 WAL, synchronous=FULL, eventId 기본키 멱등성, 지수 백오프와 jitter 재시도를 사용한다.
- 정책 결정과 실제 적용 성공을 분리한다. 현재 KMDF 통신이 미구현이므로 제한 정책은 DRIVER_NOT_CONNECTED, Applied=false이며 차단 성공으로 기록하면 안 된다.

## 남은 엔진 작업

1. Windows 장치 알림 수집과 VID/PID/Serial 정규화
2. 서비스-드라이버 IOCTL 인증 통신 및 KMDF 실제 통제
3. 사용자 세션 UI helper와 named pipe 팝업
4. 완료: 큐 dispatcher와 서버 enforcementApplied/enforcementResultCode 필드 연계
5. 테스트 서명 드라이버로 물리 장비/VDI/재연결/오프라인 시험

## 0.12 연계 완료

MediaControlPipeline과 RiskEventDispatcher를 서비스에 등록했다. V9 migration은 실제 적용 여부와 결과 코드를 감사 원장에 보존한다. 현재 장치 이벤트 소스는 안전한 Null 구현이며 Windows 장치 알림 수집 구현 전까지 실제 탐지를 주장하지 않는다.


## 0.19.0 차단 엔진 판정

현 KMDF는 정책 IOCTL 제어 장치이며 USB I/O stack 또는 파일시스템에 attach하지 않는다. 따라서 EnforcementReady=false가 맞고 실제 차단은 미구현이다. 구현 대상은 장치/볼륨 상관관계용 Storage/PnP filter와 read/write/execute용 minifilter의 분리 구조다. 자세한 내용은 docs/42를 따른다.


## 0.20.0

Agent binary policy compiler와 Control KMDF kernel evaluator가 구현됐다. 별도 YoungOneMediaFilter는 removable volume callback까지 등록하지만 detection-first로 모든 I/O를 통과시킨다. 실제 차단 완료 조건은 communication port, 볼륨-장치 상관관계, signed activation과 WDK/VM 검증이다.
