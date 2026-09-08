# 11-API-명세

POST /api/v1/agents/register는 멱등 201, POST /agents/{id}/heartbeat는 200/404이며 운영 전 단말 인증을 적용한다.

## 문서관리
기준일 2026-08-27 · 변경 시 코드와 CHANGELOG를 함께 갱신한다.

## 0.2 인증 API
등록 요청에 `bootstrapToken`이 필요하며 응답의 `agentKey`는 최초/재등록 시 한 번만 전달된다. Heartbeat는 `X-Agent-Key` 헤더가 필수다. 동일 eventId·동일 payload는 `duplicate=true`, 내용 불일치는 409다.

## 0.4 등록 입력과 판정
등록 요청은 `environmentType`, `deviceEvidenceHash`(비영구 VDI는 provider ID 대체 가능), `tpmEkHash`, `vdiProviderId`, `organizationDeviceId`, `bootSessionId`를 받는다. 응답은 connection/control/security 상태, identityDisposition, ephemeral을 반환한다. 동일 installationId에 다른 장치 증거가 오면 키를 발급하지 않고 409와 DUPLICATE_SUSPECTED를 반환한다.

## 0.5 정책 API
관리 API `POST /api/v1/policies`, `POST /api/v1/policies/{id}/activate`는 인증된 관리자만 사용한다. Agent는 `GET /api/v1/agents/{agentId}/policy`와 `X-Agent-Key`를 사용하며 ETag가 같으면 304다. Envelope는 payloadJson/hash/signature/keyId/publicKey를 포함하지만 Agent 신뢰 기준은 사전 고정 공개키다.

## 0.6 관리 API
정책 작성과 승인은 서로 다른 `X-Admin-Id`가 필요하다. `/policy-assignments`로 GLOBAL/ORGANIZATION/USER/AGENT 및 priority를 저장하고, `/policy-deployments`에서 1~100% 배포, `/{id}/rollback`으로 이전 정책을 복원한다.

## 0.9 SIEM·탐지·차단·위험도·소명
Agent는 서버 연결을 기다리지 않고 서명 정책으로 로컬 차단하며 팝업과 로컬 큐를 남긴다. 서버는 사용자·단말·USB·행위별 이벤트를 멱등 등록하고 반복 시도 위험도를 높인다. HIGH/CRITICAL 및 변조 이벤트는 outbox를 통해 Splunk 등 외부 SIEM으로 비동기 전달한다. 사용자 소명은 차단을 자동 해제하지 않고 별도 승인 예외 정책으로 처리한다. 상세 기준은 `31-SIEM-탐지-차단-위험도-소명-연계설계.md`를 따른다.
## 0.10 인사·디렉터리 다중연동
AD/LDAP, SCIM, REST, JDBC View, CSV/SFTP, 메시지 큐, 수동 업로드를 Connector SPI로 지원한다. 입력은 내부 person UUID로 정규화하며 사번·로그인 ID만으로 자동 병합하지 않는다. 퇴사·휴직은 즉시 사용자 정책을 비활성화하되 단말 기본 제어를 유지한다. 상세 기준은 `32-인사-디렉터리-다중연동-설계.md`를 따른다.
## 0.11 인사 CSV dry-run·적용
수동 CSV 원천 생성 API와 CSV dry-run/적용 API를 제공한다. 10MiB·10만 행 제한, 필수 헤더·상태·시각·중복 검증, payloadHash 멱등 처리, 외부 ID 재귀속 충돌 방지, 실행자·파일 hash 감사기록을 적용한다. 파일 누락만으로 사용자를 삭제하거나 퇴사 처리하지 않는다. 상세는 `33-인사-CSV-dry-run-가이드.md`를 따른다.

## Agent 제거 승인 토큰 (0.18.0)

POST /api/v1/admin/agents/{agentId}/uninstall-tokens 요청은 reason(최대 500자), validMinutes(1~60), X-Admin-Id를 받는다. 응답의 token 객체는 payloadJson, payloadHash, signature, signingKeyId이며 민감한 개인키는 절대 반환하지 않는다. 관리자 인증·MFA·권한 검증과 감사로그가 전제다.
