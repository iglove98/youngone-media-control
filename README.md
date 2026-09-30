# YoungOne Media Control

> 기업 환경의 USB·이동식 저장매체 사용을 탐지하고 정책에 따라 제어하기 위한 엔드포인트 보안 시스템 포트폴리오입니다.

중앙 관리 서버, Windows Agent, 커널 드라이버, 운영·보안 문서를 하나의 제품 구조로 설계했습니다. 단순 API 예제를 넘어 에이전트 식별 충돌, VDI/복제 환경, 서버 단절, 로그 재전송, 관리자 계정 보안, SIEM 및 인사 시스템 연계를 함께 다룹니다.

> **안전 안내:** 현재 커널 드라이버는 시험용 통신 골격입니다. 실제 `ACCESS_DENIED` 차단은 격리된 VM 검증 전까지 의도적으로 비활성화되어 있으며 업무 PC나 운영 환경에 설치하면 안 됩니다.

## 프로젝트 핵심

- 사용자·장치·VID/PID·SerialHash별 읽기/쓰기/실행 정책
- 로컬 PC, VDI, 골든 이미지 복제 환경의 Agent 중복 식별 및 재등록 처리
- 서버 미연결 시 마지막 정상 정책 유지, 최초 미연결 시 탐지 전용 동작
- SQLite 기반 Agent 로컬 로그 큐, 재시도 및 idempotency 처리
- 반복 위반에 따른 위험도 상승, 사용자 팝업과 소명 워크플로
- 관리자 MFA, 5회 로그인 실패 잠금, 90일 비밀번호 정책과 감사로그 설계
- Splunk 등 SIEM 연계를 위한 transactional outbox
- AD/LDAP, SCIM, REST, JDBC View, CSV/SFTP, 메시지 큐 기반 인사연동
- AP/DB/File 역할 분리와 MariaDB 계정별 최소권한 운영

## 시스템 구성

```text
Windows Endpoint
┌──────────────────────────────────────────────┐
│ Agent Service (.NET/C#)                      │
│  ├─ 장치·사용자 탐지 / 정책 캐시             │
│  ├─ SQLite 오프라인 이벤트 큐                 │
│  ├─ 사용자 세션별 트레이·경고 팝업            │
│  └─ 드라이버 정책 및 탐지 이벤트 통신         │
│           │                                  │
│  KMDF Control Driver + Minifilter (C/C++)    │
└───────────┼──────────────────────────────────┘
            │ HTTPS 8443
            ▼
┌──────────────────────────────────────────────┐
│ Spring Boot API (Java 21)                    │
│  ├─ Agent 등록 / Heartbeat / 정책 배포       │
│  ├─ 위험도 / 소명 / 관리자 감사 이벤트       │
│  ├─ HR Connector / SIEM Outbox               │
│  └─ MariaDB 11                               │
└──────────────────────────────────────────────┘
```

## 기술 스택

| 영역 | 기술 |
|---|---|
| Server | Java 21, Spring Boot, Spring Security, JPA, Validation, Gradle |
| Database | MariaDB 11, H2(local), Flyway |
| API | REST, OpenAPI/Swagger, idempotency key |
| Windows Agent | C#, .NET Worker Service, WMI, SQLite |
| Kernel | C/C++, KMDF, Minifilter 통신 ABI |
| Packaging | WiX Toolset, PowerShell |
| Infrastructure | Docker Compose, 환경별 Spring profile |
| Integration | Splunk/SIEM outbox, AD/LDAP, SCIM, REST, JDBC, CSV/SFTP |

## 구현된 범위

### 중앙 서버

- Agent 등록 및 Heartbeat API
- 서명 정책 배포와 정책 버전 관리 골격
- 위험 이벤트 멱등 수신, 반복 행위 위험도 누적 및 소명 모델
- SIEM transactional outbox와 다중 인사연동 Connector 모델
- 1회용 Agent 제거 승인 토큰 API

### Windows Agent

- WMI 기반 이동식 매체 탐지 및 장치 식별
- 온라인/오프라인 상태, 마지막 정상 정책 캐시
- SQLite 로컬 이벤트 큐와 재전송
- 사용자 세션별 트레이 아이콘과 경고 팝업
- 드라이버 정책 전달 및 탐지 이벤트 수신 계약
- 제거 승인 토큰 검증과 MSI 제거 승인 연결 골격

### 안전상 보류된 범위

- 커널 레벨의 실제 I/O 차단 활성화
- 서명된 운영용 드라이버 배포
- MSI 제거 승인 Custom Action의 운영 연결
- 실제 Splunk HEC 전송 Dispatcher

## 저장소 구조

```text
youngone-media-control/
├─ server/          Java 21 Spring Boot 중앙 서버
├─ agent-windows/   C# Agent, C/C++ 드라이버, WiX 설치 프로젝트
├─ infra/           로컬·운영 배포 구성과 연결 템플릿
└─ docs/            기획, 설계, 보안, 테스트, 운영 문서
```

핵심 문서:

- [아키텍처](docs/05-아키텍처.md)
- [로깅 및 감사 설계](docs/07-로깅-및-감사.md)
- [API 명세](docs/11-API-명세.md)
- [Agent 설계](docs/12-Agent-설계.md)
- [관리자 인증 및 계정보안](docs/14-관리자-인증-및-계정보안.md)
- [법규 및 ISMS-P 매핑](docs/15-법규-및-ISMS-P-매핑.md)
- [Agent 식별·상태·VDI 설계](docs/24-Agent-식별-상태-VDI-설계.md)
- [SIEM·탐지·차단·위험도·소명 설계](docs/31-SIEM-탐지-차단-위험도-소명-연계설계.md)
- [Agent 엔진 구현 상태](docs/34-Agent-엔진-구현상태.md)
- [서버 초기배포 및 실행 구성](docs/48-서버-초기배포-실행구성.md)
- [변경 이력](docs/CHANGELOG.md)

## 로컬 실행

개발용 서버는 H2 프로필로 실행할 수 있습니다.

```powershell
cd server
.\gradlew.bat test
.\gradlew.bat bootRun --args='--spring.profiles.active=local'
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health check: `http://localhost:8080/actuator/health`

MariaDB 사용 시 `YMC_DB_URL`, `YMC_DB_USERNAME`, `YMC_DB_PASSWORD`를 설정합니다. Windows Agent 개발 환경은 [설정 가이드](docs/29-Windows-Agent-개발환경-설정가이드.md)를 먼저 확인하세요.

## 설계에서 중점적으로 다룬 문제

1. **식별 충돌:** 장비 UUID 하나에 의존하지 않고 설치 인스턴스, 장비 지문, 서버 발급 ID를 분리했습니다.
2. **서버 단절:** 마지막 검증 정책과 로컬 큐로 통제를 지속하고 재연결 시 순서와 중복을 보장합니다.
3. **VDI 복제:** 골든 이미지 복제 시 설치 ID를 재발급하고 동일 장비 오탐을 방지합니다.
4. **운영 추적성:** 관리자 로그인, 정책 변경, 예외 승인, Agent 상태와 차단 결과를 감사 이벤트로 연결합니다.
5. **최소권한:** 서비스·운영조회·운영변경·백업·DBA 계정을 분리하고 서버 역할도 논리적으로 분리했습니다.

## 다음 단계

- 격리 VM에서 KMDF/Minifilter 실제 차단 검증
- 관리자 웹 콘솔 구현과 MFA 실제 연동
- Splunk HEC Dispatcher 및 장애 재처리 구현
- 서명·배포 파이프라인과 통합 테스트 자동화
- 부하·장애·오프라인 장기 지속 테스트

## 프로젝트 성격

개인 포트폴리오 및 엔지니어링 프로토타입입니다. 운영 제품으로 사용하려면 드라이버 서명, 보안성 검토, 장애 복구 훈련, 성능 시험, 법무·개인정보 검토가 추가로 필요합니다.
