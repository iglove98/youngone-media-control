# Server deployment starter

1. `.env.example`을 `.env`로 복사하고 모든 placeholder를 서로 다른 실제 비밀값으로 교체한다.
2. `secrets/server-keystore.p12`에 서버 DNS 이름이 포함된 조직 CA 인증서를 둔다.
3. Agent PC가 해당 조직 CA를 신뢰하는지 확인한다.
4. `docker compose config`로 필수 변수와 구성을 검증한다.
5. `docker compose up -d --build`로 시험 서버를 기동한다.
6. `https://서버주소:8443/actuator/health`가 `UP`인지 확인한다.

`.env`, `secrets/`, 실제 키와 인증서는 소스·ZIP·메신저에 포함하지 않는다. DB 네트워크는 내부 전용이며 외부에 3306을 공개하지 않는다.
