# SIEM 연계·탐지·차단·위험도·소명 설계

기준일: 2026-09-04

## 목표

Agent가 온라인 여부와 무관하게 매체 행위를 탐지하고 로컬 정책으로 즉시 허용·읽기전용·차단한다. 사용자가 결과를 이해할 수 있도록 팝업을 표시하고, 위험 이벤트를 서버에 멱등 등록한다. 서버는 반복 행위의 위험도를 높여 운영자 큐에 올리고 Splunk 등 SIEM으로 전달한다. 오탐 또는 업무상 필요는 사용자 소명과 관리자 처리 이력으로 관리한다.

## 처리 흐름

```text
장치 연결/파일 행위
 → C/C++ 드라이버가 장치·행위 식별
 → C# Agent가 서명 정책 평가
 → 즉시 ALLOW / READ_ONLY / BLOCK
 → 사용자 팝업 및 소명 식별번호 제공
 → 로컬 SQLite 큐에 원천 이벤트 저장
 → 서버 위험 이벤트 API 재전송
 → 위험점수·반복횟수 계산
 → 운영 위험 로그 등록
 → integration_outbox
 → Splunk/외부 SIEM adapter
```

SIEM 장애는 로컬 차단이나 서버 이벤트 수신을 중단시키지 않는다. 외부 전달은 transactional outbox에서 비동기로 수행한다.

## 정책 적용 축

- 사용자: 로그인 사용자 SID/조직 사용자 ID
- 단말: Agent UUID, installation UUID, 조직 자산 ID, VDI instance ID
- 사용자 그룹·단말 그룹·조직
- 매체 유형: USB_STORAGE, MTP, OPTICAL, BLUETOOTH_TRANSFER 등
- USB 개별값: VID, PID, serialHash, device instance ID
- 행위: CONNECT, READ, WRITE, EXECUTE, FORMAT, EJECT
- 환경: ONLINE, DEGRADED, OFFLINE, STALE

정책 payload schema v2는 매체/VID/PID/Serial 선택자와 READ·WRITE·EXECUTE별 action을 가진다. `READ_ONLY`는 읽기 허용·쓰기 차단의 축약 표현으로 UI에서 제공할 수 있지만 저장·감사 시 실제 행위별 결정을 기록한다.

## 정책 충돌 우선순위

1. 서버가 서명한 긴급 격리·전사 차단
2. 악성 또는 금지 USB serial/VID/PID 차단
3. 승인된 사용자+단말+USB 예외
4. USB 개별 정책
5. 사용자+단말 조합 정책
6. 사용자 정책
7. 단말 정책
8. 사용자/단말 그룹 정책
9. 조직 정책
10. 전역 기본 정책

같은 구체성·priority에서 충돌하면 `BLOCK > READ_ONLY > ALLOW`로 가장 제한적인 결과를 사용한다. 더 구체적인 정책이 덜 구체적인 정책을 변경할 수 있지만 긴급 차단은 일반 예외로 우회할 수 없다. 모든 결정은 `matchedPolicyIds`, 최종 정책, reasonCode를 남긴다.

### 식별 충돌 처리

- 같은 installation UUID에 다른 강한 장치 증거: 정책 수신 제한, DUPLICATE_SUSPECTED, 운영자 확인
- 같은 USB serial이 여러 물리 특성과 충돌: serial 단독 신뢰 금지, VID/PID/instance 조합으로 격리
- serial 미제공 장치: device instance와 VID/PID를 사용하되 재연결 동일성은 낮은 신뢰도로 표시
- VDI clone의 같은 MachineGuid: VDI provider instance ID와 boot session을 우선
- 사용자 미확인/로그온 전: 단말·USB·전역 정책만 적용하고 사용자 예외는 적용하지 않음
- 정책 버전 역행 또는 UUID 재사용: 거부하고 변조 위험 이벤트 생성

## 위험도 모델

현재 1차 구현은 24시간 동안 동일 `agentId + userId + deviceInstanceId` 조합의 이전 시도 수를 사용한다.

- BLOCK 기본 40점
- WRITE가 READ_ONLY에 걸린 경우 30점
- DETECT_ONLY 20점
- ALLOW 감사대상 5점
- 반복 1회마다 10점, 반복 가산 최대 50점
- 총점 최대 100점
- LOW 0~29, MEDIUM 30~59, HIGH 60~79, CRITICAL 80~100

동일 행위가 지속되면 OPEN 이벤트를 단순 덮어쓰지 않고 각 시도를 보존한다. HIGH/CRITICAL은 운영 위험 큐와 SIEM 전송 대상이며 소명 권고를 반환한다. 이후 탐지 룰, 시간 감쇠, 다른 PC에서 같은 사용자 반복, 같은 USB의 여러 PC 확산을 상관분석 항목으로 확장한다.

## 사용자 팝업

팝업에는 제품명, 차단 행위, 장치 유형, 정책 사유, 발생시각, 위험 이벤트 식별번호, 소명 버튼을 표시한다. 관리자 이름·정책 내부식별자·원본 serial·민감 파일경로·인증키는 표시하지 않는다.

팝업은 Agent 서비스가 검증한 로컬 UI 프로세스만 호출한다. 웹 콘텐츠나 서버가 보낸 임의 HTML을 실행하지 않는다. 반복 차단 시 팝업 폭주를 막되 이벤트 기록은 모두 유지한다.

## 위험 로그 필수 필드

`eventId`, `payloadHash`, `agentId`, `installationId`, `bootSessionId`, 사용자 ID/SID, 장치 instance, VID/PID, serialHash, mediaType, operation, decision, reasonCode, policyId/version, popupShown, occurredAt/receivedAt, 24시간 시도횟수, riskScore/severity, 처리상태, traceId를 기록한다.

원본 USB serial과 파일명은 기본적으로 중앙 저장하지 않는다. 운영상 필요하면 별도 승인된 개인정보·민감정보 정책과 마스킹을 적용한다.

## 소명 흐름

```text
OPEN → APPEAL_PENDING → ACKNOWLEDGED → RESOLVED
                           └──────────→ FALSE_POSITIVE
```

소명에는 사건 ID, 사용자 ID, 업무 사유, 제출시각을 기록한다. Agent 인증 후에도 사건의 소유 Agent와 사용자 일치를 서버가 다시 확인한다. 소명 제출 자체가 차단을 자동 해제하지 않는다. 예외가 필요하면 별도의 승인·유효기간·대상 USB/사용자/단말 범위를 가진 정책으로 발급한다.

## 외부 솔루션 연계

### 송신

공통 adapter 인터페이스 뒤에 Splunk HEC, Syslog CEF/JSON, Kafka, 범용 HTTPS webhook 구현을 둔다. 기본 전달 대상은 HIGH/CRITICAL, 정책 변조, Agent 중복식별, 반복차단, 관리자 고위험 변경이다.

outbox는 destination+eventId 유일키, 상태, 시도횟수, 다음시도시각, 마지막 오류, 전송시각을 가진다. 429/5xx는 지수 백오프하고 영구 오류는 dead-letter로 전환한다. 외부 필드명과 내부 원장 스키마를 직접 결합하지 않고 mappingVersion을 관리한다.

### 외부 탐지 결과 수신

SIEM에서 돌아오는 차단 명령을 Agent에 직접 전달하지 않는다. 외부 탐지는 서명 검증, source allowlist, replay nonce, 만료시각, 관리자 승인 또는 사전 승인 playbook을 거쳐 YoungOne 정책/격리 작업으로 변환한다. 모든 인바운드 조치는 감사로그와 rollback 대상을 가진다.

## 구현 현황

- 완료: MariaDB V6 `risk_events`, `risk_appeals`, `integration_outbox`
- 완료: Agent 위험 이벤트 멱등 등록 API 및 payloadHash 충돌 거부
- 완료: 반복 시도 위험점수와 운영자 활성 위험 목록/확인 API
- 완료: 사용자 소명 API의 Agent·사용자 소유권 검증
- 완료: C# Agent 위험 이벤트 계약과 전송 클라이언트
- 완료: 정책 payload schema v2의 USB 선택자 및 READ/WRITE/EXECUTE action
- 다음: C# SQLite 큐와 팝업 UI, 드라이버 실탐지, outbox dispatcher, Splunk adapter, 관리자 화면

## API

- `POST /api/v1/agents/{agentId}/risk-events`
- `POST /api/v1/agents/{agentId}/risk-events/{riskEventId}/appeals`
- `GET /api/v1/risk-events`
- `POST /api/v1/risk-events/{riskEventId}/acknowledge`