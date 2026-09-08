# Agent 제거 승인키 및 변조 방지 설계

## 목표와 현재 구현 범위

일반 운영자의 임의 제거를 막고, 중앙 관리자 승인 후 짧은 시간 동안 특정 설치 인스턴스만 제거할 수 있게 한다. 0.18.0에는 서버의 1회용 토큰 발급 API, MariaDB 발급 이력, Agent의 서명·대상·만료·재사용 검증, DPAPI 보호 승인 저장소와 AgentCtl 도구가 구현되었다.

현재 MSI 제거 Custom Action과 커널 드라이버 자체 보호는 아직 연결되지 않았다. 따라서 0.18.0은 제거 승인 체인의 기반 구현이며 “로컬 SYSTEM 또는 커널 권한 공격자도 절대 삭제 불가”를 보장하지 않는다.

## 토큰 구조

- Ed25519 제거 전용 키로 서명하며 정책 서명키와 분리한다.
- Payload: schemaVersion, agentId, installationId, nonce, issuedAt, expiresAt, reason, approvedBy
- 유효기간: 1~60분, Agent 검증기는 최대 1시간만 허용
- AgentId와 InstallationId를 모두 비교해 재설치, 복제 VDI, 다른 PC 토큰 오용을 차단
- SHA-256 payloadHash 고정시간 비교 후 Ed25519 서명 검증
- nonce는 소비 후 DPAPI 보호 ledger에 기록해 재사용 차단
- 서버 DB의 ISSUED는 발급 감사 상태다. 오프라인 제거를 고려해 실제 소비 완료 콜백은 다음 단계에서 추가한다.

## 운영 절차

1. 관리자 콘솔에서 대상 Agent를 정확히 선택하고 제거 사유와 1~60분 유효시간을 입력한다.
2. 서버가 POST /api/v1/admin/agents/{agentId}/uninstall-tokens로 토큰을 발급하고 발급자·사유·대상·만료를 DB에 기록한다.
3. 토큰 파일을 승인된 관리 채널로 해당 단말에 전달한다.
4. 상승된 콘솔에서 YoungOne.MediaControl.AgentCtl.exe authorize-uninstall <token-file>을 실행한다.
5. MSI 제거 진입점은 향후 consume-uninstall-approval을 Custom Action으로 호출하고 성공할 때만 제거를 계속하도록 연결한다.
6. 만료·대상 불일치·서명 불일치·재사용이면 제거를 거부하고 감사 이벤트를 서버로 전송한다.

## 키 배포

운영 서버는 YMC_UNINSTALL_SIGNING_PRIVATE_KEY와 YMC_UNINSTALL_SIGNING_PUBLIC_KEY에 각각 PKCS#8/X.509 Base64 키를 설정한다. Agent의 PinnedUninstallPublicKey에는 동일 공개키의 Ed25519 raw 32-byte Base64 값을 설치 패키지 서명 시 주입한다. 개발용 ephemeral 키는 서버 재시작 때 변경되므로 운영 사용 금지다. 개인키는 HSM/KMS 또는 접근 통제된 Secret Store에 보관하고 AP 설정 파일이나 DB에 평문 저장하지 않는다.

## 방어 계층과 한계

- Windows 서비스/설치 디렉터리/ProgramData ACL은 SYSTEM과 승인 관리자 중심으로 제한
- WDAC/AppLocker와 코드서명으로 실행 파일 교체 방지
- 서비스 중지·파일 삭제·레지스트리 변경·드라이버 unload 시도를 변조 이벤트로 기록
- Safe Mode, 오프라인 부팅, 디스크 직접 접근, 로컬 SYSTEM/커널 권한 공격은 사용자 모드 키 하나로 완전히 막을 수 없다.
- 완성 단계에는 MSI Custom Action, 서비스 보호 ACL, 서명된 KMDF 드라이버, watchdog, EDR/SIEM 연계를 함께 검증한다.

## 검증

Agent 단위 테스트는 정상 서명, 다른 Agent 대상, 만료, payload 위변조를 포함해 18개 모두 통과했다. Agent, UserUi, AgentCtl win-x64 self-contained publish가 성공했다. Java 서버는 이 호스트의 Gradle 배포본 다운로드 소켓 제한 때문에 컴파일을 완료하지 못했으며, 사내 빌드 환경에서 compileJava와 migration 검증이 필요하다.
