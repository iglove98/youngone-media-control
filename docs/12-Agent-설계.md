# 12-Agent-설계

SQLite WAL 큐에 ULID/payloadHash/attempt를 암호화 저장하고 배치·지수백오프+jitter로 재전송한다. 서버는 (agent,eventId)+hash로 중복 제거하고 내용충돌은 409 경보한다.

## 문서관리
기준일 2026-08-27 · 변경 시 코드와 CHANGELOG를 함께 갱신한다.

## 0.2 구현 상태
1회 등록 토큰, Agent별 256-bit 키 발급, 서버 SHA-256 해시 저장, Heartbeat 영수증 중복 제거가 구현됐다. 운영에서는 mTLS로 교체 또는 병행한다.

## 0.3 식별·상태 기준
`agentId`, `installationId`, 장치 증거, 조직 자산 ID, VDI provider ID, boot session을 분리한다. 상세 중복/재설치/복제/VDI/오프라인 상태는 `24-Agent-식별-상태-VDI-설계.md`를 기준으로 한다. Windows 설치·레지스트리·삭제 승인은 `25-Windows-Agent-설치-변조방지-삭제설계.md`를 따른다.

## 0.9 SIEM·탐지·차단·위험도·소명
Agent는 서버 연결을 기다리지 않고 서명 정책으로 로컬 차단하며 팝업과 로컬 큐를 남긴다. 서버는 사용자·단말·USB·행위별 이벤트를 멱등 등록하고 반복 시도 위험도를 높인다. HIGH/CRITICAL 및 변조 이벤트는 outbox를 통해 Splunk 등 외부 SIEM으로 비동기 전달한다. 사용자 소명은 차단을 자동 해제하지 않고 별도 승인 예외 정책으로 처리한다. 상세 기준은 `31-SIEM-탐지-차단-위험도-소명-연계설계.md`를 따른다.
## 0.10 인사·디렉터리 다중연동
AD/LDAP, SCIM, REST, JDBC View, CSV/SFTP, 메시지 큐, 수동 업로드를 Connector SPI로 지원한다. 입력은 내부 person UUID로 정규화하며 사번·로그인 ID만으로 자동 병합하지 않는다. 퇴사·휴직은 즉시 사용자 정책을 비활성화하되 단말 기본 제어를 유지한다. 상세 기준은 `32-인사-디렉터리-다중연동-설계.md`를 따른다.