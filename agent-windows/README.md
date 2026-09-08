# YoungOne Windows Agent

- `src/YoungOne.MediaControl.Agent`: .NET 10 Windows Service
- `driver/YoungOneMediaControl`: KMDF C/C++ 제어 드라이버 골격
- `tests`: Agent 단위 테스트
- `installer`: WiX/MSI 후속 구현 위치

현재 서비스는 설치 ID·장치 증거, DPAPI 자격증명, 등록/Heartbeat/정책 조회, Ed25519 검증, DPAPI 정책 캐시, 재시도 백오프 골격을 포함한다. 드라이버는 빌드 가능한 통신 골격을 목표로 하며 실제 장치 차단은 아직 구현하지 않았다. 개발 PC에 .NET SDK가 없으므로 이 호스트에서는 빌드되지 않았다.

명령: `dotnet restore YoungOne.MediaControl.Agent.slnx`, `dotnet test`, `dotnet publish src/YoungOne.MediaControl.Agent -c Release -r win-x64`.
