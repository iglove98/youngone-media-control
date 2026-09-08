# YoungOneMediaFilter

Removable volume 전용 파일시스템 minifilter의 detection-first 골격이다.

현재 구현:
- FltRegisterFilter/FltStartFiltering
- create/read/write/set-information/section synchronization callback 등록
- FILE_REMOVABLE_MEDIA 특성이 있는 disk file-system volume만 attach
- 모든 I/O는 통과하며 차단하지 않음

차단 활성화 전 필수:
- Filter Manager communication port와 SYSTEM 전용 ACL
- Control KMDF ABI v2 정책의 kernel rule snapshot 전달
- Volume GUID와 USB PnP/VID/PID/SerialHash 상관관계
- 시스템/부팅/페이지파일/덤프/HID 제외
- Driver Verifier, HLK, HVCI, VM snapshot rollback 테스트
- Microsoft 적합 서명 및 운영 코드서명
