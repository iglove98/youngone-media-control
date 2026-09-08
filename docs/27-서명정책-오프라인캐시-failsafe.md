# 서명 정책·오프라인 캐시·Failsafe

## 정책 수명주기

정책은 DRAFT→ACTIVE→RETIRED 불변 버전으로 관리한다. 활성화 시 이전 ACTIVE를 RETIRED로 전환한다. payload는 mediaType 순으로 정규화해 SHA-256 해시를 만들고 Ed25519로 서명한다. 운영 개인키는 HSM/KMS 또는 제한된 secret으로 주입하며 Agent에는 신뢰할 공개키와 keyId를 사전 고정한다.

## Agent 수신

Agent는 `GET /api/v1/agents/{agentId}/policy`를 Agent 키로 호출한다. 서버는 ETag `policy-{version}-{hash}`를 반환하고 동일 버전이면 304다. Agent는 응답에 포함된 공개키를 신뢰 근거로 사용하지 않고 설치 시 고정된 공개키 또는 승인된 키 회전 체인으로 서명을 검증한다.

## 로컬 적용

1. JSON SHA-256 확인
2. 고정 공개키로 Ed25519 서명 확인
3. schemaVersion, version, effective/expires 시각, 중복 매체 규칙 확인
4. 현재 버전보다 낮으면 downgrade 거부(승인된 rollback 예외 제외)
5. DPAPI/TPM 보호 후 임시 파일에 기록
6. flush 후 원자 rename
7. 다시 읽어 검증한 뒤 활성 포인터 전환
8. 직전 정상 정책 1개 보존

현재 Java 공통 모듈은 서명/해시 검증과 보호 인터페이스, 임시 파일 원자 교체를 구현했다. Windows Agent에서 `PolicyProtector`를 LocalMachine DPAPI 또는 TPM sealed key로 구현한다.

## 매체별 오프라인 기본

| 매체 | 온라인 기본 | 연결 단절 중 유효 정책 | TTL/grace 만료 |
|---|---|---|---|
| USB 저장장치 | 정책 | 마지막 정상 정책 | BLOCK |
| MTP/PTP | 정책 | 마지막 정상 정책 | BLOCK |
| 광학 기록 | 정책 | 마지막 정상 정책 | BLOCK 또는 READ_ONLY |
| Bluetooth 파일전송 | 정책 | 마지막 정상 정책 | BLOCK |
| 키보드/마우스/HID | 안전 allowlist | 안전 allowlist | 안전 allowlist |
| 승인된 보안 USB | 정책/Serial HMAC | 마지막 정상 정책 | 별도 비상 allowlist 여부 명시 |

각 규칙은 onlineAction, expiredOfflineAction, offlineBehavior, offlineGraceSeconds를 가진다. 정책 파일 손상·서명 오류는 마지막 정상 정책으로 rollback하고, 정상본도 없으면 SAFE_ALLOWLIST_ONLY 또는 매체별 fail-closed를 적용한다.

## 키 회전

정책 서명키는 keyId로 식별한다. 새 공개키는 기존 신뢰키로 서명된 key-rollover 문서로 배포하고 겹치는 유효기간을 둔다. 개인키 유출 시 긴급 폐기 목록과 새 설치 패키지/안전 채널을 사용한다. API 응답의 publicKey 단독 교체는 신뢰하지 않는다.

## 감사

정책 생성·활성화·은퇴, 서명 keyId, payloadHash, 대상 Agent, 200/304, 서명 실패, downgrade 거부, cache 적용/rollback, offline failsafe 진입·해제를 기록한다. payload 원문과 서명은 정책 버전 증적으로 보존한다.


## 0.15.0 최초 활성화 보정

정책을 한 번도 받지 않은 신규 Agent는 DETECT_ONLY다. 정상 서명 정책 최초 수신 후에만 제어를 활성화하며, 그 이후 정책 삭제·손상·만료는 fail-closed로 처리한다. 상세 상태 전이는 docs/38을 따른다.
