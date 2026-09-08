# Agent 식별·상태·VDI 설계

기준일: 2026-08-31. 식별자는 하나의 값에 의존하지 않고 서버 ID, 설치 ID, 장치 증거, VDI 인스턴스 및 조직 자산 ID를 분리한다.

## 식별 계층

| 값 | 의미 | 생성/보관 | 식별키 사용 |
|---|---|---|---|
| `agentId` | 서버가 관리하는 Endpoint 레코드 UUID | 서버 | 최종 PK |
| `installationId` | 설치 인스턴스 UUID | 설치 후 Agent, DPAPI+ACL | 등록/재설치 판단 |
| `deviceEvidence` | TPM EK 공개키 해시, SMBIOS UUID, BIOS/보드/디스크 계열 해시 | Agent 수집, 원문 미전송 | 점수 기반 장치 판정 |
| `machineSidHash`/`machineGuidHash` | OS 이미지 증거 | 해시 전송 | 복제 탐지 보조값만 |
| `organizationDeviceId` | CMDB/자산번호 | 관리자/연동 | 업무 자산 연결 |
| `vdiProviderId` | 하이퍼바이저/브로커 머신 ID | VDI 연동 | VDI 머신 식별 |
| `imageId` | 승인된 골든이미지 버전 | 빌드 파이프라인 | 복제 원인 판정 |
| `bootSessionId` | 부팅마다 생성되는 ULID | 메모리/로컬 상태 | 동시 실행·snapshot 감지 |
| `credentialId` | 현재 단말 인증키/인증서 버전 | 서버 | 회전·폐기 |

Hostname, IP, MAC, 사용자명은 변경·중복될 수 있어 고유키로 사용하지 않는다. 하드웨어 원문 Serial은 기본 수집하지 않고 정규화된 구성요소 해시와 품질등급만 전송한다.

## 환경 유형

| 유형 | 식별·등록 방식 | 정책/로그 원칙 |
|---|---|---|
| 물리 PC | agentId+installationId+TPM 우선 증거 | 장기 자격증명, 일반 보존 |
| 영구 VDI | 물리 PC와 동일하되 vdiProviderId 병행 | VM별 Agent 레코드 |
| 비영구 VDI | 골든이미지에 ID/키를 넣지 않고 부팅 후 브로커 토큰으로 임시 등록 | `ephemeral=true`, TTL 자동정리, 로그는 중앙 보존 |
| 공용/키오스크 | 장치 ID와 로그인 세션을 분리 | 사용자 전환 이벤트 필수 |
| 오프라인 독립망 | 사전 발급된 만료형 등록 패키지와 서명 정책 | 반출입 절차, 수동 로그 수거 지원 |

골든이미지에는 `installationId`, agentKey, 인증서 개인키, SQLite 큐를 절대 포함하지 않는다. 이미지 sealing 단계에서 제거하고 첫 부팅 후 생성한다.

## 중복·혼동 경우의 수와 처리

| 상황 | 탐지 | 처리 |
|---|---|---|
| 동일 installationId가 다른 장치 증거에서 동시 접속 | bootSession/IP/evidence 불일치 | 둘 다 `DUPLICATE_SUSPECTED`, 민감 정책 fail-closed, 관리자 병합/분리 |
| 골든이미지에 ID가 잘못 포함됨 | 짧은 시간 다수 동일 installationId | 해당 credential 즉시 폐기, 이미지 격리, 각 VM 재등록 |
| PC 재설치 | 장치 증거 유사, installationId 변경 | `REINSTALL_CANDIDATE`; 기존 레코드 이력 연결 후 새 credential |
| 메인보드 교체 | 일부 증거 급변, 자산번호 동일 | 관리자/CMDB 확인 후 `HARDWARE_CHANGED` 승인 |
| VM clone | providerId 또는 bootSession 다름, ID 동일 | 원본/복제 분리, 복제 키 회전 |
| snapshot rollback | event sequence/monotonic counter 역행 | 재동기화·키 회전, 미전송 큐 중복은 eventId로 제거 |
| hostname/IP/MAC 변경 | 보조값만 변경 | 동일 Agent 유지, 변경 감사 |
| 디스크 복제 | installationId 동일, TPM/보드 증거 다름 | 중복 의심으로 자동 병합 금지 |
| Agent 이중 설치/프로세스 | 단일 서비스 mutex와 설치 락 | 두 번째 실행 차단·감사 |
| DB 레코드만 삭제/복원 | credential/sequence 불일치 | 재등록 승인 전 격리 |
| 장기간 미접속 후 복귀 | lastSeen 임계 초과 | 정책·키·시간 전체 재검증 후 온라인 전환 |

자동 병합은 하지 않는다. 점수는 `TPM 일치` 강, `vdiProviderId/CMDB 일치` 중강, `SMBIOS/보드` 중, `MachineGuid/디스크/MAC/hostname` 약으로 계산하며 강한 충돌이 하나라도 있으면 수동 검토한다.

## 상태 모델

`PENDING → ONLINE → DEGRADED → OFFLINE → STALE`을 기본으로 하고 별도 보안 상태 `DUPLICATE_SUSPECTED`, `QUARANTINED`, `REVOKED`, `UNINSTALL_PENDING`을 둔다.

- ONLINE: 최근 2회 Heartbeat 이내, 키·정책 정상
- DEGRADED: 통신 지연, 큐 증가, 정책 만료 임박, 구성요소 일부 실패
- OFFLINE: Heartbeat 3회 또는 설정 임계 초과
- STALE: 장기 미접속(기본 30일), 복귀 시 전체 재인증
- QUARANTINED/REVOKED: 서버 명령 또는 위변조 탐지; 일반 허용정책을 적용하지 않음

서버 시각과 Agent monotonic 시간을 함께 사용한다. 사용자 화면의 OFFLINE은 서버 관점이며, Agent 로컬 제어 상태(`ENFORCING`, `FAILSAFE`, `DRIVER_ERROR`)를 별도 필드로 표시한다.

## 통신 단절 시 제어

마지막으로 서명 검증된 정책을 암호화 저장하고 네트워크 없이 적용한다. 정책 TTL과 grace period를 둔다. 저장매체·MTP·미승인 USB는 TTL 만료 후 fail-closed, 키보드/마우스·필수 업무장치는 안전 allowlist를 유지한다. 서버 장애만으로 전체 USB를 무조건 차단하지 않고 매체 유형별 안전 기본값을 정책에 명시한다.

로컬 이벤트는 내구 큐에 기록하고 용량 한계 시 차단/감사 이벤트를 우선 보존한다. 정책 파일 손상·서명실패·시간 역행·드라이버 실패 시 `FAILSAFE`로 전환하고 로컬 Windows Event Log와 중앙 복구 후 감사로그에 남긴다.

## 등록 판정 순서

1. 환경유형 및 이미지 ID 확인
2. bootstrap/VDI 브로커 토큰 검증
3. installationId 중복 조회
4. 장치 증거 점수 및 동시 bootSession 검사
5. 신규/재설치/복제/교체/의심으로 분류
6. agentId와 credential 발급 또는 격리
7. 서명 정책 동기화 후 ONLINE 전환

## 0.4 구현 상태
환경유형·장치증거·부팅세션·조직/VDI 식별자, 연결/제어/보안 상태, 신규/기존/재설치/비영구/중복거부 판정, 충돌 기록, 주기적 연결상태 평가가 서버에 구현됐다.
