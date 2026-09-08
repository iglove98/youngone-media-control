# Minifilter-Agent 탐지 통신 ABI v1

## 목적

Removable Minifilter가 관찰한 파일 I/O를 LocalSystem Agent로 전달하고, 기존 정책 평가·사용자 팝업·SQLite 오프라인 큐·서버 재전송 경로에 합류시킨다. 0.22.0은 탐지 통신 검증 단계이며 커널에서 `ACCESS_DENIED`를 반환하지 않는다.

## 통신 구조

- Filter Manager 포트: `\\YoungOneMediaFilterPort`
- 서버 측: `FltCreateCommunicationPort`, 최대 동시 연결 1개
- Agent 측: `FilterConnectCommunicationPort`, `FilterGetMessage`
- 포트 ACL: Filter Manager 기본 보안 설명자(`FLT_PORT_ALL_ACCESS`), Agent 서비스는 LocalSystem 실행
- 연결 실패·드라이버 미설치: 5초 간격 재연결하며 WMI 장치 탐지는 계속 동작
- 커널 송신: timeout 0의 비대기 `FltSendMessage`; Agent 장애가 파일 I/O를 지연시키지 않음
- Agent 병합 큐: 최대 2,048건, 과부하 시 오래된 탐지 이벤트부터 폐기

## 고정 이벤트 구조

64비트 정렬 기준 payload는 `Size`, `AbiVersion`, `Sequence`, Windows system time 100ns, 요청 PID, major function, requestor mode, IRP flags로 구성한다. 모든 수신 메시지는 `Size`와 `AbiVersion=1`을 먼저 검증한다.

I/O 매핑은 CREATE→Open 후보, READ→Read, WRITE/SET_INFORMATION→Write, ACQUIRE_FOR_SECTION_SYNCHRONIZATION(0xFF)→Execute 후보로 변환한다. 이는 탐지 후보이며 최종 차단 판단 값으로 바로 쓰지 않는다.

## 기존 처리 경로

커널 탐지와 WMI 연결·해제 탐지를 `CompositeMediaEventSource`로 병합한다. 병합된 이벤트는 정책 결정, 사용자 알림, canonical payload SHA-256, SQLite `INSERT OR IGNORE`, 온라인 서버 재전송 및 idempotency 처리를 동일하게 사용한다.

## 현재 제한과 다음 안전 게이트

- 커널 volume과 PnP DeviceInstanceId/VID/PID/SerialHash 연결은 아직 구현 전이다.
- 파일 경로와 사용자 SID는 커널 메시지에 넣지 않아 개인정보·성능 노출을 최소화했다.
- 동일 파일의 반복 I/O 집계·rate limit은 VM 계측값을 기준으로 추가해야 한다.
- 실제 차단 전 필수 조건은 volume-PnP 매핑, 서명 정책 검증, 프로세스/사용자 귀속, fail-open/fail-closed 정책, Driver Verifier와 충돌 시험이다.
- 미서명 드라이버는 회사 PC나 운영 환경에 설치하지 않는다.

## 검증

- C# Agent Release 빌드: 경고 0, 오류 0
- Agent 단위 테스트: 26/26 성공
- EWDK 28000.2526 Control/Minifilter Release x64: 경고 0, 오류 0
- INF signability 및 CAT 생성 성공
- 실제 포트 연결·이벤트 수신은 격리된 스냅샷 가능 VM에서 검증 예정
