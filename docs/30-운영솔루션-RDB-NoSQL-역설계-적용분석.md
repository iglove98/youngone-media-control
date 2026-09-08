# 운영 솔루션 RDB·NoSQL 역설계 및 적용 분석

기준일: 2026-09-01

## 1. 분석 범위와 한계

첨부 자료는 운영 솔루션의 PostgreSQL `public` 스키마 테이블 목록 456개와, 별도 분석·로그 저장계층으로 보이는 객체 목록 1,600여 개다. 두 번째 목록은 `add_*`, `set_*`, `mv_*`, `raw_data_*`, `summary_data_*`, `tb_*` 객체가 함께 존재해 이벤트 적재, 최신 상태 갱신, 물리화 변환, 원천 조회, 요약 조회를 분리한 구조로 판단된다.

다만 첨부에는 `CREATE TABLE`, 컬럼, PK/FK, 인덱스, 파티션, TTL, 저장 엔진, 실제 NoSQL 제품명이 없다. 따라서 PostgreSQL 여부는 첫 파일 형식으로 확인되지만, 두 번째 저장소를 MongoDB·ClickHouse·Elasticsearch 중 하나로 단정하지 않는다. 이 문서는 객체 이름에서 확정 가능한 책임 분리만 반영한다.

## 2. 운영 솔루션에서 확인한 구조

### 2.1 RDB 제어 평면

PostgreSQL에는 관리자·권한·정책·조직·노드·예외·승인·라이선스·작업·설정처럼 관계와 트랜잭션이 중요한 데이터를 둔다.

YoungOne 적용 대상:

- `tb_node`, `tb_node_hash`, `tb_node_hw`, `tb_node_nic`, `tb_node_history`: Agent 기준정보와 식별 증거
- `tb_agent_api_key`, `tb_agent_uuid_type`: Agent 인증키와 식별 유형
- `tb_policy`, `tb_common_policy`, `tb_group_policy`, `tb_node_policy`, `tb_node_policy_assignment`: 정책과 할당
- `tb_edc_exception`, `tb_edc_except_approval`: 매체 예외와 승인
- `tb_admin`, `tb_admin_role`, `tb_admin_pw_history`, `tb_login_fail_status`: 관리자 계정 보안
- `tb_policy_log`, `tb_admin_search_history`: 정책 변경과 관리자 조회 흔적
- Spring Batch 및 report batch 테이블: 집계·보고서 작업 실행 이력

### 2.2 이벤트·분석 평면

운영 솔루션은 다음 이름으로 대량 로그와 분석 데이터를 분리한다.

- 원천 이벤트: `tb_agent_event_log`, `tb_agent_connection_status_event_log`, `tb_edc_agent_event_log`, `tb_edc_device_log`, `tb_edc_device_connection_info_log`, `tb_audit_log`, `tb_search_audit_log`
- 현재 상태: `tb_agent_alive`, `tb_node_status_latest`, `tb_edc_status_latest`, 각 제품별 `*_latest`
- 시간 집계: `*_hourly_timeline`, `*_daily_timeline`, `tb_issue_agent_timeline`
- 보고서: `tb_report_*`, `raw_data_*`, `summary_data_*`, `summary_raw_data_*`
- 변환: `mv_*` 객체와 `set_*`/`add_*` 적재 함수 또는 프로시저

이는 이벤트 원본을 매번 RDB 조인해 콘솔과 보고서를 만드는 구조가 아니라, 용도별 읽기 모델을 미리 만드는 CQRS형 운영 패턴이다.

### 2.3 마이그레이션·백업 흔적

- `tb_pre_*`: 이전 저장계층 또는 사전 적재 데이터의 staging/migration 계층
- `tb_node_from_pg`, `pg_*`: PostgreSQL 원본을 분석 저장계층으로 복제하는 브리지
- `tb_incremental_backup_nosql_info`, `tb_incremental_backup_objectid_info`: NoSQL 증분 백업 체크포인트
- `tb_report_*_nosql_data`: NoSQL 원본을 보고서용 RDB 데이터로 변환한 흔적

이 패턴은 데이터 이관과 재처리가 제품의 정상 기능으로 설계되어 있음을 보여준다.

## 3. YoungOne에 적용할 저장 책임

| 데이터 | 기준 저장소 | 보조 저장소 | 이유 |
|---|---|---|---|
| Agent 식별·자격증명·상태 판정 | RDB | 분석용 최신 상태 | 유일성·트랜잭션 필요 |
| 정책 버전·서명·승인·할당 | RDB | 배포 조회 캐시 | 변경 통제와 업무분리 필요 |
| 예외 신청·승인·만료 | RDB | 이벤트 검색 인덱스 | 승인 트랜잭션 필요 |
| Heartbeat 원본 | 이벤트 저장소 | RDB 최신 상태 | 대량 append와 빠른 현재 상태 조회를 분리 |
| 장치 연결·허용·차단 이벤트 | 이벤트 저장소 | 시간 집계·보고서 | 대량 적재·기간 검색·보존정책 필요 |
| 관리자 감사로그·검색 감사 | 불변 이벤트 저장소 | RDB 참조키 | 삭제 방지와 장기 추적 필요 |
| 콘솔 현재 상태 | 읽기 모델 | RDB 원장 | 조인 폭발 방지 |
| 일·시간 통계 | 집계 저장소 | 재생성 가능한 산출물 | 원천 이벤트에서 재생성 가능 |
| 첨부·정책 파일 | File 역할 저장소 | RDB 메타데이터 | 대용량 파일과 트랜잭션 데이터 분리 |

제품명을 정하기 전 논리 명칭은 `RDB Control Plane`, `Event Store`, `Read Model`, `File Store`로 사용한다. NoSQL을 쓴다는 이유만으로 모든 로그를 한 컬렉션에 넣지 않는다.

## 4. 즉시 적용하는 논리 모델

```text
RDB 원장
  Agent ─ IdentityEvidence ─ Credential
    ├─ CurrentState
    ├─ PolicyAssignment ─ ImmutablePolicyVersion
    └─ ExceptionApproval

Event Store
  AgentEvent
    ├─ CONNECTION_STATUS
    ├─ HEARTBEAT
    ├─ DEVICE_CONNECTION
    ├─ MEDIA_CONTROL_RESULT
    ├─ POLICY_APPLY_RESULT
    └─ TAMPER_DETECTION

Read Model
  AgentStatusLatest
  EdcStatusLatest
  DeviceEventHourly
  DeviceEventDaily
  PolicyComplianceLatest
  IssueAgentTimeline
```

### 이벤트 공통 필드

- `eventId`: Agent가 생성한 UUID, 재전송 중 변경 금지
- `tenantId`, `agentId`, `installationId`, `bootSessionId`
- `eventType`, `schemaVersion`, `occurredAt`, `receivedAt`
- `sequence`, `payloadHash`, `traceId`
- `policyId`, `policyVersion`, `result`, `reasonCode`
- `deviceClass`, `vid`, `pid`, `serialHash`
- `payload`: 유형별 확장 데이터

중복 기준은 `(tenantId, agentId, eventId)`다. 같은 키와 같은 `payloadHash`는 기존 영수증을 반환하고, 같은 키에 다른 해시는 충돌·변조 후보로 감사한다.

## 5. 동기화와 장애 원칙

1. Agent 수신 API는 RDB와 이벤트 저장소에 분산 트랜잭션을 걸지 않는다.
2. 수신 영수증과 전달할 이벤트를 RDB transactional outbox에 같은 트랜잭션으로 기록한다.
3. 별도 publisher가 Event Store로 전송하고 성공 체크포인트를 기록한다.
4. `*_latest`, 시간 집계, 보고서는 이벤트 소비자가 만든다.
5. 소비자는 `eventId` 기반 멱등 upsert를 사용한다.
6. Event Store 장애 시 Agent 요청을 무조건 실패시키지 않고 outbox 용량·보존 한도 안에서 수신을 지속한다.
7. 적체량, 최고 지연시간, 재시도 횟수, dead-letter 수를 운영 지표로 노출한다.
8. 집계 데이터는 원천 이벤트로 재생성 가능해야 하며 원본보다 먼저 삭제하지 않는다.

## 6. 그대로 복제하지 않을 부분

- 456개 RDB 테이블과 1,600여 분석 객체 전체
- 제품별 `tb_pre_*`, `tb_user_report_*`, 다국어별 export 객체의 반복
- EDC 외 APM·AV·EDR·ESA·APRM 기능 테이블
- 근거 없이 `latest`, `status`, `timeline`, `report`를 모두 물리 테이블로 생성하는 방식
- RDB와 NoSQL에 동일 데이터를 각각 원장으로 취급하는 이중 원장

YoungOne 1차 범위는 매체제어에 필요한 최소 읽기 모델만 만들고 실제 조회 부하가 확인될 때 확장한다.

## 7. 실제 DDL 적용 전에 추가로 필요한 운영 자료

- PostgreSQL `pg_dump --schema-only` 또는 대상 테이블의 `\d+`
- NoSQL/분석 저장소 제품명·버전과 `SHOW CREATE TABLE` 또는 collection/index 정의
- EDC 관련 테이블의 PK, partition key, order/sort key, TTL
- `add_edc_*`, `set_edc_status`, 관련 `mv_*` 정의
- 한 달 이벤트 건수, 평균/최대 payload, 보존기간, 콘솔 조회 SLA
- RDB↔분석 저장소 동기화 방식과 재처리 절차
- 백업 체크포인트와 복구 테스트 결과

이 자료가 확보되면 YoungOne Flyway DDL, Event Store 스키마, 인덱스·파티션·TTL을 운영 구조와 대조하여 확정한다.

## 8. 적용 결정

- 채택: 제어 평면/이벤트 평면 분리, 원천/latest/hourly/daily/report 계층, EDC 로그 세분화, 검색 감사, 증분 체크포인트, 재처리 구조
- 조정 채택: 운영 솔루션의 `node`를 YoungOne `agent + installation identity`로 분해
- 보류: 구체 NoSQL 제품과 물리 스키마, 파티션·TTL, materialized view 수
- 제외: YoungOne 범위 밖 제품 테이블과 중복 다국어 export 객체