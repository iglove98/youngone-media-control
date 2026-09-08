# YoungOne Media Control

기업용 매체제어 시스템의 설계 기준본과 초기 구현이다. 중앙 서버는 Java/Spring Boot, Windows Agent 서비스는 C#/.NET, 실제 장치 제어 드라이버는 C/C++ KMDF, 설치 패키지는 WiX로 분리한다.

> Portfolio / engineering prototype. 커널 드라이버는 미서명 시험용이며 업무 PC나 운영 환경에 설치하면 안 된다. 현재 실제 `ACCESS_DENIED` 차단은 VM 검증 전까지 의도적으로 비활성화되어 있다.

## 주요 설계 목표

- 사용자·장치·VID/PID·SerialHash 기준의 읽기/쓰기/실행 정책
- 서버 미연결 시 마지막 정상 정책과 로컬 SQLite 로그 큐 유지
- Agent 재설치·복제·VDI 환경에서 중복 식별과 충돌 예방
- 관리자 MFA, 5회 실패 잠금, 감사로그와 역할 분리
- Splunk/SIEM 연계를 위한 transactional outbox와 idempotency
- AP/DB/File 역할 분리 및 MariaDB 계정 최소권한 설계

## 구조

```text
Windows Agent (.NET/C#)
  ├─ 장치 인벤토리 및 사용자 알림
  ├─ 오프라인 정책/SQLite 이벤트 큐
  ├─ KMDF Control 정책 채널
  └─ Minifilter 탐지 이벤트 채널
                │ HTTPS 8443
                ▼
Spring Boot API (Java 21)
  ├─ Agent 등록/Heartbeat/정책 배포
  ├─ 위험도·소명·감사 이벤트
  ├─ HR 연계 및 SIEM outbox
  └─ MariaDB 11
```

## 구성

- `docs/`: 기획부터 운영까지의 설계·운영 문서
- `server/`: Java 21 + Spring Boot + Gradle 서버
- `agent-windows/`: C# Windows Service, C/C++ KMDF 드라이버, WiX 설치 프로젝트
- `infra/`: Docker Compose, production profile, Agent 연결 템플릿

설계·운영 자료는 `docs/`에 기획, 아키텍처, 인증, 로깅, DB 권한, 배포, 장애대응, 백업복구 및 커널 ABI 변경 이력까지 포함한다.

## 실행

1. MariaDB를 준비하고 `YMC_DB_URL`, `YMC_DB_USERNAME`, `YMC_DB_PASSWORD`를 설정한다.
2. `cd server`
3. Windows: `gradlew.bat test`, 실행: `gradlew.bat bootRun`
4. OpenAPI: `http://localhost:8080/swagger-ui.html`, 상태: `/actuator/health`

개발 프로필은 H2를 사용한다: `gradlew.bat bootRun --args='--spring.profiles.active=local'`.


Windows Agent 개발자는 먼저 `agent-windows/tools/check-prerequisites.ps1`를 실행하고 `docs/29-Windows-Agent-개발환경-설정가이드.md`를 따른다. 현재 드라이버는 안전한 통신 골격이며 실제 장치 차단은 아직 구현하지 않았다.

## 0.21 현재 구현 상태

서버는 Agent 등록·Heartbeat, 서명 정책 배포, 위험도·소명·SIEM outbox, 인사연동 골격과 1회용 제거 승인 토큰 API를 포함한다. Windows Agent는 WMI 매체 탐지, 오프라인 정책 캐시, 로컬 SQLite 재전송 큐, 사용자 세션별 트레이/팝업, 드라이버 정책 전달, 제거 토큰 검증과 표준 MSI 제거 승인 연결을 구현했다. C/C++ KMDF 드라이버의 실제 I/O 차단과 MSI 제거 승인 Custom Action은 아직 안전 비활성/미연결 상태다.

## 0.9 구현 범위

USB VID/PID/Serial별 읽기·쓰기·실행 정책, 위험 이벤트 멱등 수신, 반복 위험도, 사용자 소명, SIEM outbox 골격을 포함한다. 로컬 팝업·실제 드라이버 차단·Splunk dispatcher는 다음 구현 단계다.

## 0.10 인사연동

AD/LDAP, SCIM, REST, JDBC View, CSV/SFTP, 메시지 큐, 수동 업로드를 공통 Connector와 내부 person UUID 모델로 수용한다. V7은 원천·사용자·외부ID·그룹·동기화 실행·충돌 원장을 추가한다.
