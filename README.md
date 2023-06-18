# Crypto Wallet Demo

## Overview

간단한 암호화폐 지갑 API demo입니다.

## Getting Started

### 환경 변수 설정

.bashrc, .zshrc와 같은 설정파일에 환경변수를 설정해주세요.

```shell
## Database
export POSTGRES_URL=jdbc:postgresql://db:5432/${YOUR_POSTGRES_DB}
export POSTGRES_USERNAME=${YOUR_POSTGRES_USERNAME}
export POSTGRES_PASSWORD=${YOUR_POSTGRES_PASSWORD}

## Web3 Provider
export WEB3_PROVIDER_URL=export WEB3_PROVIDER_URL=https://eth-goerli.g.alchemy.com/v2/${YOUR_CLIENT_ID}
```

```shell
$ source .zshrc
```

프로젝트 상위의 docker-compose.yaml 파일이 있는 위치에서 다음 명령어를 실행해주세요.

```shell
$ docker-compose build
$ docker-compose up
```

정상적으로 실행되었다면 아래와 같은 로그가 출력됩니다.

```shell
[+] Running 2/0
 ✔ Container wallet-db-1   Created                                                                                                                                                                                                                                                                                     0.0s
 ✔ Container wallet-app-1  Created                                                                                                                                                                                                                                                                                     0.0s
Attaching to wallet-app-1, wallet-db-1
wallet-db-1   |
wallet-db-1   | PostgreSQL Database directory appears to contain a database; Skipping initialization
wallet-db-1   |
wallet-db-1   | 2023-06-18 14:04:01.424 UTC [1] LOG:  starting PostgreSQL 15.3 (Debian 15.3-1.pgdg110+1) on aarch64-unknown-linux-gnu, compiled by gcc (Debian 10.2.1-6) 10.2.1 20210110, 64-bit
wallet-db-1   | 2023-06-18 14:04:01.424 UTC [1] LOG:  listening on IPv4 address "0.0.0.0", port 5432
wallet-db-1   | 2023-06-18 14:04:01.424 UTC [1] LOG:  listening on IPv6 address "::", port 5432
wallet-db-1   | 2023-06-18 14:04:01.428 UTC [1] LOG:  listening on Unix socket "/var/run/postgresql/.s.PGSQL.5432"
wallet-db-1   | 2023-06-18 14:04:01.440 UTC [30] LOG:  database system was shut down at 2023-06-18 14:03:59 UTC
wallet-db-1   | 2023-06-18 14:04:01.455 UTC [1] LOG:  database system is ready to accept connections
wallet-app-1  |
wallet-app-1  |   .   ____          _            __ _ _
wallet-app-1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
wallet-app-1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
wallet-app-1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
wallet-app-1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
wallet-app-1  |  =========|_|==============|___/=/_/_/_/
wallet-app-1  |  :: Spring Boot ::                (v3.1.0)
wallet-app-1  |
wallet-app-1  | 2023-06-18T14:04:01.799Z  INFO 1 --- [           main] com.example.wallet.WalletApplication     : Starting WalletApplication v0.0.1-SNAPSHOT using Java 17.0.2 with PID 1 (/src/wallet.jar started by root in /src)
wallet-app-1  | 2023-06-18T14:04:01.801Z  INFO 1 --- [           main] com.example.wallet.WalletApplication     : No active profile set, falling back to 1 default profile: "default"
wallet-app-1  | 2023-06-18T14:04:02.248Z  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
wallet-app-1  | 2023-06-18T14:04:02.290Z  INFO 1 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 37 ms. Found 4 JPA repository interfaces.
wallet-app-1  | 2023-06-18T14:04:02.623Z  INFO 1 --- [           main] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat initialized with port(s): 8080 (http)
```

## Test

### Test Scenario

#### 1. 지갑 생성

- [o] 정상 케이스 -> address와 privateKey 반환 (사용자가 별도로 저장하게 하기 위함)
- [o] 결제 비밀번호 (password)를 입력하지 않을 경우 -> 400 에러 발생

#### 2. 지갑 ETH 잔액 조회

- [o] 정상 케이스 -> Balance 반환
- [o] 입금 또는 출금 트랜잭션의 blockConfirmations가 12보다 작을 때 -> Balance가 업데이트되지 않음
- [o] queryParam network_name을 누락하여 호출할 경우 -> 기본 네트워크인 ethereum_goerli의 Balance 반환
- [x] 서버 다운타임동안 실제 잔고에 변경이 생긴 경우 -> 마지막으로 체크한 블록 넘버부터 다시 계싼하여 Balance 반환 (미구현)

#### 3. 출금

- [o] 정상 케이스 -> transaction hash 반환, 이후 event를 조회해보면 pending 상태로 저장된 이벤트 확인 가능
- [o] 파라미터 누락 -> 400 에러 반환
- [o] 잔액 부족 -> 400 에러 반환
- [x] 트래픽이나 가스비 문제 등으로 tx 실패시 설정한 횟수만큼 재시도 (미구현)

#### 4. 입출금 이벤트 조회

- 예상 결과값
  1. [x] 입금 Pending
  2. [o] 입금 Mined
     - [ ] block confirmations 필드값 : 0,1,2, ... 11
  3. [o] 입금 Confirmed
  4. [o] 출금 Pending
  5. [o] 출금 Mined
     - [ ] block confirmations 필드값 : 0,1,2, ... 11
  6. [o] 출금 Confirmed
     - block confirmation 필드값: 12
- [o] 최근 이벤트부터 반환
- [o] 페이지 size 적용 -> 정상 동작
  - [o] size > 100일 경우 한 페이지에100개까지 반환
  - [o] default값은 10
- [o] starting_after 적용 -> 이전 id 값을 갖는 이벤트들이 페이지네이션되어 반환됨
- [o] ending_before 적용 -> 이전 id 값을 갖는 이벤트들이 페이지네이션되어 반환됨
- [o] starting_after와 ending_before을 둘 다 사용할 경우 starting_after만 적용
