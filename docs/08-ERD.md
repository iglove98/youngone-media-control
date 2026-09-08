# 08-ERD

Agent 1:N Receipt/Event, Policy 1:N Rule/Assignment, Admin N:M Role, Admin 1:N Audit, Exception N:1 Policy 관계다.

## 문서관리
기준일 2026-08-27 · 변경 시 코드와 CHANGELOG를 함께 갱신한다.

## 0.8 운영 솔루션 RDB·이벤트 저장계층 반영
RDB는 Agent·정책·승인·예외의 원장으로 사용한다. 대량 Heartbeat·매체 이벤트·감사 이벤트는 별도 Event Store에 append하고, transactional outbox로 전달한다. 콘솔은 `AgentStatusLatest`, `EdcStatusLatest`, 시간 집계 읽기 모델을 조회한다. 구체 NoSQL 제품은 운영 DDL과 조회량 확인 후 확정한다.
## 0.9 SIEM·탐지·차단·위험도·소명
Agent는 서버 연결을 기다리지 않고 서명 정책으로 로컬 차단하며 팝업과 로컬 큐를 남긴다. 서버는 사용자·단말·USB·행위별 이벤트를 멱등 등록하고 반복 시도 위험도를 높인다. HIGH/CRITICAL 및 변조 이벤트는 outbox를 통해 Splunk 등 외부 SIEM으로 비동기 전달한다. 사용자 소명은 차단을 자동 해제하지 않고 별도 승인 예외 정책으로 처리한다. 상세 기준은 `31-SIEM-탐지-차단-위험도-소명-연계설계.md`를 따른다.
## 0.10 인사·디렉터리 다중연동
AD/LDAP, SCIM, REST, JDBC View, CSV/SFTP, 메시지 큐, 수동 업로드를 Connector SPI로 지원한다. 입력은 내부 person UUID로 정규화하며 사번·로그인 ID만으로 자동 병합하지 않는다. 퇴사·휴직은 즉시 사용자 정책을 비활성화하되 단말 기본 제어를 유지한다. 상세 기준은 `32-인사-디렉터리-다중연동-설계.md`를 따른다.