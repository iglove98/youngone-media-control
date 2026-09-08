# EWDK 드라이버 실빌드 환경과 검증 결과

## 도구

Microsoft Enterprise WDK 28000.2526 ISO를 사용한다. 이 패키지는 Visual Studio 2026 Build Tools 18.3.0, MSVC 14.50, Windows SDK/WDK 10.0.28000.0을 포함한다.

- 로컬 ISO: C:\Project\.tools\EWDK_28000_202607.iso
- 크기: 19,785,189,376 bytes
- SHA-256: A2928191A0E975D0BE6CA911BF3F11CA00E93C161BCEA6B922B248CBB833358F
- 원본: Microsoft download.microsoft.com 공식 배포 URL
- ISO는 소스 ZIP과 제품 배포본에 포함하지 않는다.

## 재현 절차

1. 관리자 PowerShell에서 ISO를 읽기 전용 마운트한다.
2. agent-windows/tools/check-prerequisites.ps1로 .NET과 EWDK를 확인한다.
3. 관리자 PowerShell에서 agent-windows/tools/build-drivers-ewdk.ps1을 실행한다.
4. 기본값은 Release/x64/SignMode=Off/Inf2CatUseLocalTime=true다.
5. 운영 서명 또는 승인된 테스트 인증서가 준비된 경우에만 Signed 옵션을 사용한다.

## 실제 수정된 빌드 오류

- INF SourceDisksNames/SourceDisksFiles 누락
- DIRID 13 사용 시 대상 OS decoration 누락
- C symbolic link 문자열 backslash escape 오류
- Kernel BCryptHash를 위한 Cng.lib 링크 누락
- SYS가 driver package에 포함되지 않던 FilesToPackage 누락
- 한국 시간과 UTC 날짜 차이로 인한 Inf2Cat future DriverVer
- minifilter instance registry가 Parameters 하위가 아니던 문제
- 인식되지 않는 DefaultInstall 세부 OS decoration

모든 오류를 수정하고 재현 스크립트로 두 프로젝트를 연속 재빌드했다.

## 검증 결과

### YoungOneMediaControl

- MSVC /W4 /WX /Qspectre
- compile 성공
- link 성공
- INF verification 성공
- Inf2Cat signability 성공
- CAT 생성 성공
- 경고 0, 오류 0
- unsigned SYS SHA-256: AE2055850217BB1641479F5CF83AFEF03C1D88B15ECAFEBA7C78164063D28A24

### YoungOneMediaFilter

- MSVC /W4 /WX /Qspectre
- compile 성공
- link 성공
- INF verification 성공
- Inf2Cat signability 성공
- CAT 생성 성공
- 경고 0, 오류 0
- unsigned SYS SHA-256: 2C6581611F7BA99BAE2512923792F32351DF44CF3C5EE4F4477BAE4B8C365714

## 배포 제한

생성된 SYS/CAT/INF는 컴파일 검증용 unsigned 패키지다. 회사 업무 PC에 직접 설치하지 않는다. 다음 단계는 스냅샷 가능한 별도 Windows VM에서 테스트 인증서, test signing, Driver Verifier, fltmc, pnputil을 사용해 detection-first 로드·언로드를 검증하는 것이다.

실제 운영 배포에는 Microsoft Hardware Dev Center attestation/HLK 계열 서명, EV 인증서 기반 제출 절차, HVCI 호환 검증과 조직 변경승인이 필요하다.

## VM 검증용 묶음

- 파일: `youngone-media-control-drivers-test-x64-0.21.0.zip`
- 크기: 10,817 bytes
- SHA-256: `F1BF553FC9C6B7374CF145ADB66AA9A02EB7072C9440762E60956AC0281F1995`
- 구성: Control SYS/INF/CAT, Minifilter SYS/INF/CAT, `TEST_ONLY.txt`
- 용도: 격리된 스냅샷 가능 Windows VM의 로드·탐지 검증 전용
