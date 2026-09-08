# Removable Volume-PnP 식별 매핑

## 목표

Minifilter가 관찰하는 `\\Device\\HarddiskVolumeN`과 정책이 사용하는 USB 장치의 PnP DeviceInstanceId, VID, PID, SerialHash를 연결한다. 매핑이 확정되지 않은 이벤트는 절대로 실제 차단에 사용하지 않는다.

## 커널 처리

- Removable volume에만 Minifilter instance를 연결한다.
- instance 생성 시 `FltGetVolumeName` 결과를 NonPaged instance context에 한 번 저장한다.
- 매 I/O마다 이름을 재조회하거나 메모리를 할당하지 않는다.
- 탐지 ABI v2는 기존 필드와 UTF-16 `VolumeName[64]`를 포함하며 x64 native 크기는 168 bytes다.
- Agent가 없어도 timeout 0의 비대기 전송 후 즉시 원래 I/O를 계속한다.

## Agent 매핑

1. `QueryDosDevice`로 이동식 논리 드라이브와 커널 volume 이름을 연결한다.
2. `Win32_LogicalDiskToPartition`으로 논리 드라이브에서 파티션을 찾는다.
3. `Win32_DiskDriveToDiskPartition`으로 파티션에서 실제 디스크를 찾는다.
4. 디스크의 PNPDeviceID와 SerialNumber를 기존 정규화기에 전달한다.
5. USBSTOR ID에 VID/PID가 없으면 동일 SerialHash를 갖는 USB VID/PID PnP 노드를 보조 매칭한다.
6. 결과를 10초 캐시해 반복 파일 I/O가 WMI 조회를 유발하지 않게 한다.

## 중복·오인 방지 규칙

- volume 이름 비교는 대소문자를 구분하지 않는다.
- 장치 Serial은 원문을 서버에 보내지 않고 정규화 후 SHA-256만 사용한다.
- SerialHash 일치가 없으면 USB 부모 노드의 VID/PID를 임의 결합하지 않는다.
- 매핑 실패 이벤트는 `UNRESOLVED_VOLUME:<volume>`로 기록하고 DetectOnly로 처리한다.
- VDI 리디렉션·MTP·복합 USB처럼 논리 디스크 연계가 없는 장치는 기존 WMI 인벤토리 탐지를 유지한다.
- 실제 차단 활성화 전 동일 volume의 PnP 매핑 안정성, 재연결, 드라이브 문자 변경, 다중 파티션, 동일 모델·무시리얼 장치를 VM에서 시험한다.

## 검증 결과

- Agent Release 빌드 경고 0, 오류 0
- 단위 테스트 27/27 성공
- ABI v2 managed native 크기 168 bytes 테스트 추가
- EWDK 28000.2526 Minifilter 빌드·링크·INF signability·CAT 생성 성공
- 실제 장치 연결 검증은 미서명 드라이버를 격리 VM에 설치한 뒤 수행 예정
