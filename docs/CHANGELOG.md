# 0.25.1 - 2026-09-30

- GitHub 공개 포트폴리오용 README 전면 정리
- 프로젝트 핵심, 시스템 구성도, 기술 스택 및 구현 범위 명시
- 안전상 보류된 커널 차단 범위와 다음 개발 단계 구분
- 주요 설계·운영 문서 바로가기와 로컬 실행 방법 추가

# 0.25.0 - 2026-09-06

- Java 21 non-root 다단계 서버 Dockerfile 추가
- Spring production profile의 HTTPS 8443 및 필수 secret 환경변수 구성
- MariaDB와 AP를 internal data network로 분리한 Docker Compose 추가
- DB 3306 host 미노출 및 no-new-privileges/read-only AP 컨테이너 적용
- TLS PKCS#12, 정책/제거 Ed25519 키, bootstrap token 환경변수 템플릿 추가
- 실제 비밀값·인증서가 소스에 포함되지 않도록 ignore 규칙 추가
- Windows Agent 연결 appsettings 템플릿 추가
- docs/48 서버 초기 배포 및 Agent 연결 구성 추가

# 0.24.0 - 2026-09-06

- 커널 탐지 이벤트 volume/PID/operation 기준 1초 coalescing 추가
- 반복 I/O로 인한 Agent 메모리·SQLite·서버 이벤트 폭주 방지
- 최대 4,096개 rate key와 오래된 key 우선 정리 구현
- 서로 다른 프로세스 및 작업 유형은 별도 위험 이벤트로 유지
- 중복 억제와 분리 동작 단위 테스트 추가
- Agent Release 빌드 경고 0, 오류 0 및 단위 테스트 29/29 성공
- docs/47 커널 탐지 이벤트 폭주 방지 추가

# 0.23.0 - 2026-09-06

- Minifilter instance context에 removable kernel volume 이름 저장
- 커널 탐지 이벤트 ABI v2에 UTF-16 VolumeName 추가
- Agent QueryDosDevice 및 WMI association 기반 Volume→논리 디스크→파티션→PnP 디스크 매핑 구현
- USBSTOR 장치 SerialHash와 USB 부모 노드 SerialHash 일치 시 VID/PID 보완
- 장치 매핑 10초 캐시로 파일 I/O별 WMI 조회 방지
- 불명확한 volume은 UNRESOLVED_VOLUME으로 기록하고 DetectOnly 유지
- ABI v2 x64 구조 크기 168 bytes 단위 테스트 추가
- Agent Release 빌드 경고 0, 오류 0 및 단위 테스트 27/27 성공
- EWDK Minifilter 빌드·링크·INF signability·CAT 생성 성공
- docs/46 Removable Volume-PnP 식별 매핑 추가

# 0.22.0 - 2026-09-05

- Minifilter와 LocalSystem Agent 사이 Filter Manager 통신 포트 추가
- 크기·ABI·순번·시간·PID·I/O 종류를 포함한 고정 탐지 이벤트 ABI v1 추가
- 커널 송신을 timeout 0 비대기 방식으로 구성해 Agent 장애 시 I/O 지연 방지
- Agent `fltlib.dll` 수신기와 5초 재연결 경로 구현
- WMI 장치 탐지와 커널 I/O 탐지를 2,048건 bounded channel로 병합
- 기존 정책 평가·팝업·SQLite 오프라인 큐·서버 idempotency 경로 재사용
- 실제 ACCESS_DENIED 차단은 volume-PnP 매핑과 VM 검증 전까지 비활성 유지
- Agent Release 빌드 경고 0, 오류 0 및 단위 테스트 26/26 성공
- EWDK Control/Minifilter Release x64 빌드 경고 0, 오류 0
- docs/45 Minifilter-Agent 탐지 통신 ABI v1 추가

# 0.21.0 - 2026-09-05

- Microsoft EWDK 28000.2526 ISO 기반 실제 커널 빌드 환경 구성
- Visual Studio 2026 Build Tools 18.3.0 및 MSVC 14.50 사용
- Control KMDF 실제 compile/link/INF/Inf2Cat/CAT 생성 성공
- Removable Minifilter 실제 compile/link/INF/Inf2Cat/CAT 생성 성공
- 두 드라이버 모두 /W4 /WX /Qspectre, 경고 0, 오류 0
- 최신 INF SourceDisks, DIRID 13, Parameters\Instances 규칙 적용
- Kernel CNG Cng.lib 링크 및 FilesToPackage 구성 수정
- Inf2Cat 한국 현지시간 검증 옵션 적용
- EWDK 자동 탐지 prerequisite와 연속 빌드 스크립트 추가
- unsigned 테스트 전용 드라이버 패키지 산출
- 업무 PC 직접 설치 금지, VM Driver Verifier 검증 필요 상태 명시
- Agent 단위 테스트 20개 성공, 실패 0
- docs/44 EWDK 드라이버 실빌드 환경과 검증 결과 추가

# 0.20.0 - 2026-09-05

- Agent→Control KMDF 정책 계약을 binary rule table ABI v2로 변경
- 56바이트 header와 규칙별 64바이트 고정 wire format 구현
- VID/PID/SerialHash, read/write/execute, offline rule 컴파일 및 입력 검증
- compiled payload SHA-256 생성과 커널 측 길이·hash·enum 검증 구현
- NonPagedPool 정책 snapshot 원자 교체와 가장 구체적인 규칙 선택기 구현
- PolicyLoaded, EvaluatorReady, EnforcementReady 상태 분리
- IOCTL_YOMC_EVALUATE 판정 진단 경로 추가
- removable volume 전용 YoungOneMediaFilter detection-first 골격 추가
- create/read/write/delete·rename/execute 관련 minifilter callback 등록
- WDK prerequisite에 fltkernel.h와 cng.lib 검사 추가
- Agent 전체 빌드 및 단위 테스트 20개 성공, 실패 0
- WDK 부재로 커널 소스 컴파일·Driver Verifier 검증은 보류
- docs/43 Kernel Policy ABI v2와 Removable Minifilter 골격 추가

# 0.19.0 - 2026-09-05

- WiX 표준 제거 경로를 1회용 제거 승인 소비와 실제 연결
- 전체 제거 시 StopServices 전에 LocalSystem AgentCtl 검증 실행
- 승인 없음·만료·재사용이면 MSI 제거 중단, MajorUpgrade는 예외 처리
- Agent/UserUi/AgentCtl publish 경로를 WiX 프로젝트에 명시
- 현재 KMDF가 제어 장치일 뿐 실제 I/O filter가 아님을 코드 상태와 문서에 명확화
- Storage/PnP filter와 file-system minifilter 분리 구조, VDI·오프라인 판정 설계 확정
- WiX XML 구조 검증 성공; WiX SDK/WDK 부재로 MSI·드라이버 바이너리 빌드는 보류
- Agent 전체 빌드 및 단위 테스트 18개 성공, 실패 0
- docs/42 실제 매체 차단 드라이버 구조와 표준 제거 승인 연결 추가

# 0.18.0 - 2026-09-05

- 서버 서명 기반 1회용 Agent 제거 승인 토큰과 발급 API 추가
- AgentId와 InstallationId 이중 바인딩, 1~60분 만료, SHA-256 및 Ed25519 검증
- DPAPI 보호 승인 파일과 nonce 소비 ledger로 토큰 재사용 방지
- AgentCtl authorize-uninstall 및 consume-uninstall-approval 명령 구현
- 제거 서명키를 정책 서명키와 분리하고 MariaDB V11 발급 감사 테이블 추가
- AgentCtl을 win-x64 배포물과 WiX 설치 구성에 포함
- 실제 MSI 제거 차단 Custom Action과 커널 자기보호는 미구현 상태로 명시
- Agent 전체 빌드, publish 및 단위 테스트 18개 성공, 실패 0
- Java 서버 검증은 사내망 Gradle 소켓 제한으로 보류
- docs/41 Agent 제거 승인키 및 변조 방지 설계 추가

# 0.17.0 - 2026-09-05

- 정상 서명 정책 설치 직후 Agent에서 KMDF 제어 드라이버로 전달하는 경로 연결
- ABI v1 56바이트 Header와 최대 1 MiB UTF-8 정책 payload builder 구현
- Magic, ABI, 정책 버전, 규칙 수, 길이, SHA-256 직렬화
- IOCTL_YOMC_SET_POLICY 호출 및 드라이버 미설치·미지원·ABI 오류 결과 코드 구분
- 정책 캐시 성공·드라이버 실패 시 DRIVER_ERROR와 운영로그 기록 유지
- 실제 KMDF 차단은 EnforcementReady=false 및 STATUS_NOT_SUPPORTED로 안전 비활성화
- 전체 빌드 및 단위 테스트 14개 성공, 실패 0
- docs/40 서버 정책의 Agent·드라이버 전달경로 추가

# 0.16.0 - 2026-09-05

- 온라인 초록, 오프라인 마지막 정책 노랑, 최초 미연결 회색, 보호·오류 빨강 트레이 상태 구현
- Heartbeat 성공·실패 상태를 로그인 세션별 UI Helper에 전달
- 최초 서버 미연결은 탐지만, 오프라인은 마지막 정상 정책 유지
- C:\ProgramData\YoungOne\MediaControl을 Agent 데이터 루트로 명시
- 날짜·20 MiB 분할, 30일 보존 JSON Lines 운영로그 provider 구현
- SQLite 위험 이벤트 큐 실제 건수 Heartbeat 연계 및 connection pooling 비활성화
- Windows Forms 아이콘·팝업 변경을 UI 스레드로 marshal하도록 안정화
- 전체 빌드 및 단위 테스트 12개 성공, 실패 0
- docs/39 온라인·오프라인 아이콘과 로컬 로그 정책 추가

# 0.15.0 - 2026-09-04

- 최초 정책 미수신 Agent를 BOOTSTRAP_DETECT_ONLY로 변경하여 서버 정책 전 차단 방지
- 정상 서명 정책 최초 수신 시 DPAPI activation latch와 HKLM 활성화 이력 기록
- 활성화 이후 정책 삭제·손상·만료 시 필수 HID 제외 fail-closed 처리
- 정상 정책의 미매칭 장치는 명시적 차단 없이 DETECT_ONLY 처리
- Heartbeat에 실제 로컬 큐 건수, 정책 버전, 제어 상태 전송
- 서버 ControlStatus와 V10 control_status 길이 확장 migration 추가
- Agent/UI 전체 테스트 10개 성공, 실패 0
- docs/38 최초 정책 활성화 및 오프라인 제어 상태 문서 추가

# 0.14.0 - 2026-09-04

- Session 0 서비스와 분리된 Windows UserUi Helper 프로젝트 추가
- WTS 활성 세션 열거와 세션 ID별 named pipe 팝업 전달 구현
- UI가 LocalSystem SID 송신자만 신뢰하도록 impersonation 검증 추가
- 실제 알림 전달 성공 시에만 risk event popupShown=true 기록
- 다중 사용자/VDI 세션별 팝업 격리와 Common Startup 설치 구성 추가
- Agent/UI 배포용 publish-agent.ps1 추가
- 전체 빌드 및 단위 테스트 8개 성공, 실패 0
- docs/37 사용자 세션 팝업 및 VDI 설계 추가

# 0.13.0 - 2026-09-04

- Windows WMI 장치 재고 비교 기반 USB 저장장치/WPD/광학매체 연결·제거 탐지 구현
- VID/PID/Serial 정규화 및 Serial SHA-256 가명처리 구현
- 일반 USB Hub/HID를 통제 이벤트에서 제외해 키보드·마우스 오차단 방지
- 서비스 SYSTEM 계정 대신 대화형 로그인 사용자 조회
- C# DriverControlClient와 KMDF IOCTL ABI v1, 상태 조회, 관리자/SYSTEM 전용 장치 ACL 추가
- 드라이버 미완성 상태는 EnforcementReady=false로 유지하여 차단 성공 오기록 방지
- Agent 빌드 성공 및 단위 테스트 6개 성공, 실패 0
- docs/36 Windows 매체 탐지 및 드라이버 통신 계약 추가

# 0.12.1 - 2026-09-04

- 프로젝트 전용 .NET SDK 10.0.302 설치 및 재현 가능한 Agent 빌드 스크립트 추가
- NSec.Cryptography Ed25519 정책 서명 검증 연결
- 누락된 Options DataAnnotations/Http 패키지와 SecurityException 참조 수정
- .NET 10 Microsoft Testing Platform 설정 및 테스트 프로젝트 런타임 정합성 수정
- Agent 솔루션 빌드 성공: 경고 0, 오류 0
- 정책 fail-closed 단위 테스트 2개 성공, 실패 0
- docs/35 로컬 개발도구 및 검증 가이드 추가

# 0.12.0 - 2026-09-04

- C# 정책 판정 엔진: schema v2, VID/PID/Serial, 행위별 결정, 필수 HID 안전 허용, 정책 오류 fail-closed
- 실제 차단 성공과 정책 결정을 분리하는 IMediaEnforcer/EnforcementResult 계약 추가
- SQLite WAL/FULL 오프라인 위험 이벤트 큐와 멱등 enqueue, 백오프 재전송 기반 추가
- docs/34 Agent 엔진 구현 상태와 남은 KMDF·팝업·dispatcher 작업 기록
- MediaControlPipeline 및 RiskEventDispatcher 연결, 실제 적용 결과를 SQLite 큐에서 서버로 재전송
- 서버 V9 risk_events에 enforcement_applied/enforcement_result_code 감사 필드 추가
- 정책 미존재 시 USB fail-closed와 필수 키보드 허용 단위 테스트 추가

# CHANGELOG

[0.1.0] 2026-08-27: 전체 문서, Java 21 Spring Boot 골격, Agent 등록/Heartbeat, Flyway V1, Gradle wrapper 추가. Known gaps: mTLS, 영수증 연결, 정책·로그·콘솔.

## 문서관리
기준일 2026-08-27 · 변경 시 코드와 CHANGELOG를 함께 갱신한다.

## [0.2.0] - 2026-08-27
- 등록 bootstrap token과 Agent별 키 발급/해시 저장
- Heartbeat 인증, eventId/payloadHash 영수증, 동일 재전송 응답과 409 충돌
- Flyway V2 및 HTTP 통합 테스트 추가
- API/Agent/보안/테이블 문서 갱신

## [0.3.0] - 2026-08-31
- 물리 PC, 영구/비영구 VDI, 골든이미지, 재설치, clone, snapshot rollback 식별 기준 확정
- ONLINE/DEGRADED/OFFLINE/STALE 및 보안 격리 상태 모델 추가
- 통신 단절 시 마지막 서명 정책과 매체별 failsafe 설계
- Windows 서비스·Program Files/ProgramData·레지스트리·DPAPI/TPM 구조 설계
- 중앙 1회용 삭제 승인과 break-glass·변조탐지 설계
- TCP 443 중심 포트·방화벽 매트릭스 추가

## [0.4.0] - 2026-08-31
- Agent 환경 유형, 장치 증거, TPM/VDI/조직 자산, boot session 데이터 모델
- ONLINE/DEGRADED/OFFLINE/STALE, 제어·보안 상태와 주기적 상태 평가
- 신규/기존/재설치 후보/비영구 VDI/중복 거부 판정 서비스
- 동일 설치 ID의 장치 증거 충돌 기록 및 409/키 발급 차단
- Flyway V3, 도메인·HTTP 판정 테스트 추가
- Java 21 서버 소스 전체 수동 컴파일 성공; Gradle 통합 러너는 호스트 loopback 제한으로 재검증 보류

## [0.5.0] - 2026-08-31
- 정책 DRAFT/ACTIVE/RETIRED 불변 버전과 Flyway V4
- USB/MTP/광학/Bluetooth 등 매체별 온라인·오프라인 action/grace 모델
- 정규 payload SHA-256 및 Ed25519 서명/keyId
- Agent 인증 정책 조회와 ETag/304
- 고정 공개키 서명 검증, 보호 인터페이스, 원자적 로컬 정책 캐시
- 서명/변조/캐시 테스트 및 오프라인 failsafe 문서 추가
- Java 21 전체 서버 소스 컴파일 성공

## [0.6.0] - 2026-08-31
- 정책 DRAFT/APPROVED/ACTIVE/RETIRED와 작성자·승인자 업무분리
- GLOBAL/ORGANIZATION/USER/AGENT 할당과 priority
- Agent UUID 결정적 hash 기반 1~100% canary 배포
- previousPolicyId 기반 롤백과 배포 이력
- Flyway V5, 승인·할당·배포·롤백 API 및 단위 테스트
- Java 21 전체 서버 소스 컴파일 성공

## [0.7.0] - 2026-08-31
- Windows Agent를 서버 Java 코드와 분리하고 C#/.NET 10 Windows Service 골격 생성
- DPAPI 설치 식별자·Agent 자격증명, WMI 장치 증거 해시, 등록·Heartbeat·정책 조회 클라이언트 구현
- Ed25519 고정 공개키 검증, 보호된 정책 캐시, ETag 및 통신 실패 지수 백오프 골격 구현
- C/C++ KMDF 드라이버 IOCTL·INF 프로젝트와 WiX MSI 프로젝트 골격 생성
- Visual Studio/.NET/SDK/WDK 설정 가이드와 개발 PC 사전 점검 스크립트 추가
- 현재 PC에 .NET SDK·WDK가 없어 Windows Agent/driver 실제 빌드는 설치 후 검증 필요

## [0.8.0] - 2026-09-01
- 운영 솔루션 PostgreSQL 456개 테이블과 별도 로그·분석 객체 목록 역설계
- RDB 제어 평면과 Event Store·Latest·Timeline·Report 읽기 계층 분리 패턴 반영
- EDC 장치/연결/Agent 이벤트, 검색 감사, 정책 상태, 증분 체크포인트 매핑
- transactional outbox, 멱등 소비, 재처리 및 적체 운영 기준 추가
- 물리 NoSQL 스키마는 운영 DDL·엔진·파티션·TTL 확인 전까지 보류
## [0.9.0] - 2026-09-04
- 사용자·단말·USB VID/PID/Serial 선택자와 READ/WRITE/EXECUTE 정책 schema v2
- Agent 위험 이벤트 멱등 수신, payloadHash 충돌 탐지, 24시간 반복 위험도 누적
- 위험 이벤트·소명·SIEM outbox MariaDB V6 스키마 및 서버 API
- 다른 Agent 사건 소명 방지와 사용자 일치 검증
- C# Agent 위험 이벤트 계약·전송 클라이언트 골격
- 로컬 즉시 차단·팝업·운영 위험 큐·Splunk 등 외부 연계·정책 충돌 설계
- Gradle은 호스트 loopback 제한으로 실행 실패; Java 21 수동 컴파일은 클래스 생성까지 완료했으나 호스트 JAR close 권한 오류로 exit 3
## [0.10.0] - 2026-09-04
- AD/LDAP·SCIM·REST·JDBC View·CSV/SFTP·메시지 큐·수동 업로드 Connector 유형 확정
- 내부 person UUID, 원천별 external identity, 그룹 membership, sync run, 충돌 V7 스키마
- Full/Delta/Event 동기화와 cursor 멱등 처리·대량 변경 안전 중지 설계
- 사번 재사용·SID/UPN 중복·외부 ID 재귀속·다중 원천 상태 충돌 방지
- HrConnector SPI, NormalizedIdentity, IdentityReconciliationService 구현
- 최소 개인정보, secretRef, read-only 연계계정, 퇴사·휴직 정책 안전 기준 반영
## [0.11.0] - 2026-09-04
- 인사연동 source 생성과 CSV dry-run/적용 API
- UTF-8 BOM·quoted field 파서, 10MiB·10만 행·필수 헤더·중복 검증
- Person·ExternalIdentity·SyncRun·Conflict JPA 원장과 reconciliation 적용
- 동일 payload unchanged, external ID 재귀속 충돌, immutable key 기반 외부 ID 연결
- V8 실행자·파일 SHA-256·dry-run 감사 필드와 CSV 템플릿·운영 가이드
- Java 21 수동 컴파일에서 신규 포함 전체 main source error 0 확인; Gradle은 호스트 loopback 제한
