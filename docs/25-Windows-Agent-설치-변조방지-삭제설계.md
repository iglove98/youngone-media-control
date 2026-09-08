# Windows Agent 설치·변조방지·삭제 설계

## 보안 한계

로컬 Administrators/SYSTEM 권한과 물리 접근을 가진 공격자에게 일반 Windows 서비스만으로 절대 삭제 불가능을 보장할 수 없다. 목표는 표준사용자 삭제 차단, 관리자 오용 난이도 상승, 중앙 승인 없는 제거 탐지·복구, 증거 보존이다. 강한 보장은 BitLocker, Secure Boot, TPM, WDAC/AppLocker, BIOS 부팅통제, EDR과 함께 달성한다.

## 설치 구조

- Windows Service: `YoungOneMediaControlAgent`, 자동(지연) 시작, LocalSystem 또는 최소권한 전용 서비스 SID
- 설치 경로: `%ProgramFiles%\YoungOne\MediaControl\Agent`; 사용자 쓰기 금지
- 상태/큐: `%ProgramData%\YoungOne\MediaControl`; 서비스 SID와 SYSTEM만 수정
- 로그: Windows Event Log `YoungOne-MediaControl/Operational` + 암호화 로컬 큐
- 드라이버가 필요하면 Microsoft 서명·HVCI 호환 드라이버로 별도 패키지하고 서비스와 버전을 독립 관리
- MSI/MSIX 설치 ID와 제품 UpgradeCode를 고정하고 지원되는 업그레이드 경로만 허용

## 레지스트리

`HKLM\SOFTWARE\YoungOne\MediaControl\Agent` 아래에 비밀이 아닌 설정만 둔다.

| 값 | 용도 |
|---|---|
| `InstallationId` | DPAPI 보호 파일과 교차검증하는 설치 ID |
| `AgentId` | 서버 UUID |
| `Environment` | prod/stage/dev 고정값 |
| `ServerBaseUrl` | allowlist 검증된 HTTPS 주소 |
| `PolicyVersion` | 적용 정책 표시값 |
| `InstallState` | Pending/Active/Repair/UninstallPending |
| `ImageId`/`VdiMode` | 골든이미지와 VDI 동작 구분 |

Agent key, bootstrap token, 인증서 개인키는 평문 레지스트리·환경변수·명령행에 저장하지 않는다. TPM 비내보내기 키 또는 LocalMachine DPAPI로 보호하고 ACL을 서비스 SID/SYSTEM으로 제한한다. 레지스트리와 보호 파일의 값이 다르면 위변조로 판정한다.

정책에서 환경변수는 비밀 저장소로 인정하지 않는다. 서버/테스트 설정 편의 외에는 Agent 보안결정에 사용하지 않고, 운영 Agent는 서명된 설정 파일과 레지스트리 allowlist만 읽는다.

## 서비스 보호와 자기복구

- 서비스 중지/시작 유형 변경/실행파일 교체/ACL 변경을 감사
- Service Recovery로 비정상 종료 시 재시작하고 제한된 횟수 초과 시 EDR/중앙 경보
- 별도 watchdog은 상호 무한재시작을 피하도록 백오프·회로차단을 적용
- 실행파일·DLL·정책·드라이버의 Authenticode 서명과 SHA-256 manifest 검증
- 업데이트는 서명 검증, 원자 교체, 이전 정상버전 롤백, downgrade 차단
- Windows Defender exclusion을 광범위하게 설정하지 않음
- 서비스 제어 권한에서 일반 사용자와 로컬 일반 관리자의 STOP/DELETE 권한을 최소화하되 OS 운영 비상계정 절차는 유지

## 중앙 승인 삭제

일반 MSI 삭제만으로 제거되지 않게 Custom Action에서 서버가 서명한 1회용 `UninstallAuthorization`을 검증한다. 토큰은 `agentId`, `installationId`, `deviceEvidenceDigest`, 요청자, 사유, 만료(기본 10분), nonce, 허용 작업(UNINSTALL/REPAIR)을 포함한다.

삭제 흐름:

1. 관리자 콘솔에서 대상과 사유를 입력하고 별도 승인자가 승인
2. 서버가 단말에 묶인 짧은 만료의 서명 토큰 발급
3. 오프라인 삭제가 필요하면 이중승인으로 오프라인 해제 파일 발급하고 사용 즉시 폐기목록에 기록
4. Uninstaller가 서명·대상·시각·nonce와 재사용 여부를 검증
5. Agent가 마지막 큐를 전송하거나 암호화 증적을 보존하고 `UNINSTALL_STARTED` 기록
6. 서비스/드라이버/파일/레지스트리를 순서대로 제거
7. 재부팅 후 서버가 `UNINSTALLED` 확인; 미완료 시 경보

고정된 만능 삭제 비밀번호나 공통 삭제 키는 사용하지 않는다. 서버 장애를 대비한 break-glass 키는 HSM/금고, 2인 통제, 짧은 만료, 건별 발급, 사후감사를 적용한다.

## 우회 시나리오와 대응

| 시도 | 대응 |
|---|---|
| 서비스 중지/비활성화 | SDDL 최소권한, watchdog/EDR, 다음 Heartbeat에서 tamper 상태 |
| 파일 삭제/교체 | Program Files ACL, 서명/manifest, 실행 중 보호, repair |
| 레지스트리 삭제/변경 | ACL, 보호 파일 교차검증, signed config 복구 |
| 시스템 시간 변경 | 서버시각+monotonic counter, 큰 역행 시 failsafe |
| 네트워크 차단 | 마지막 정상 정책 계속 적용, 큐 저장, OFFLINE 경보 |
| 안전모드/외부부팅 | BitLocker+Secure Boot+BIOS 통제; 복귀 후 부팅증거 검사 |
| VM snapshot/디스크 복원 | sequence/bootSession/credential 불일치로 재등록 또는 격리 |
| 프로세스 메모리/키 탈취 | 비내보내기 TPM 키, 짧은 토큰, 키 회전, mTLS |
| MSI 강제 제거 | 중앙 승인 Custom Action+EDR 경보; 로컬 최고권한 우회 가능성은 정책통제로 보완 |

## 오프라인 운영

오프라인에서도 서비스·드라이버·정책 서명검증과 매체 통제를 수행한다. 서버 접속이 필요한 삭제 승인에는 유효한 오프라인 해제 파일을 사용한다. 파일은 특정 단말·작업·만료·nonce에 묶고 사용 후 로컬 영수증을 저장하여 재접속 때 중앙 대사한다.

## 필수 배포 전 검증

표준사용자, 로컬 관리자, 도메인 관리자, SYSTEM, 안전모드, 네트워크 차단, 시간변경, snapshot rollback, Golden Image clone, VDI 로그오프/재생성, OS upgrade, Agent upgrade/rollback, 디스크 부족, DB/AP 장애에서 제어와 감사 결과를 시험한다.


## 0.18.0 구현 상태

서버 발급, 서명 검증, 대상 설치 바인딩, DPAPI 승인 보관, nonce 재사용 차단과 AgentCtl 배포까지 구현했다. WiX에는 AgentCtl 파일이 포함되지만 MSI 제거를 승인 소비 성공에 연동하는 Custom Action은 아직 구현·검증되지 않았다. 따라서 현 버전은 승인 기반 제거 기반 기능이며 최종 tamper protection 완성본이 아니다.


## 0.19.0 구현 상태

표준 MSI 제거가 StopServices 전에 AgentCtl의 1회용 승인 소비를 실행하도록 연결했다. 승인 실패는 MSI 실패로 전파한다. MajorUpgrade는 제외한다. WiX SDK가 없는 현재 장비에서는 MSI 바이너리 빌드 검증이 아직 필요하며, 직접 파일 삭제나 SYSTEM/커널 공격 방지는 별도 ACL·WDAC·EDR·드라이버 보호가 필요하다.
