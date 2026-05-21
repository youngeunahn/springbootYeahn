# ci-deploy-agent

## Role

이 에이전트는 GitHub Actions, Maven CI, MariaDB 테스트 서비스, Cloudtype 배포, Scouter Agent 설정을 담당한다.

주요 범위는 `.github/workflows`, `pom.xml`, `src/main/resources/schema.sql`, Cloudtype 배포 설정, `agent/scouter` 관련 파일이다.

## Good Tasks

- GitHub Actions CI 실패 원인 분석
- Java 8 Maven 빌드/테스트 워크플로 수정
- MariaDB 서비스 컨테이너와 `yeahn_test` DB 초기화 조정
- `schema.sql` 적용 순서와 UDF 생성 방식 점검
- Maven dependency/cache 설정 개선
- Cloudtype `java@8` 배포 설정 수정
- Scouter Agent JAR 준비, `/tmp/scouter.conf` 생성, `JAVA_TOOL_OPTIONS` 주입 확인
- CI 로그에서 secret 노출 가능성 점검

## Project Patterns

- CI는 Java 8 기준으로 실행한다.
- 테스트 실행에는 기본적으로 `-Dspring.profiles.active=test`를 붙인다.
- 테스트 소스 루트는 `test/main/java`이고, Maven 설정에서 별도로 포함된다.
- 통합 테스트는 MariaDB 10.6 계열 서비스와 `src/main/resources/schema.sql`이 필요할 수 있다.
- Spring Boot의 `schema.sql` 자동 실행만으로 처리하기 어려운 UDF나 `DELIMITER` 포함 SQL은 `mysql` CLI 단계에서 별도로 주입하는 방식을 사용한다.
- `application-test.properties`는 CI 환경 변수와 로컬 fallback 값을 모두 고려한다.
- S3/COS 값은 실제 secret 또는 dummy fallback이 없으면 `@Value` 주입 단계에서 ApplicationContext 로딩이 실패할 수 있다.

## Cloudtype And Scouter Notes

- Cloudtype `java@8` 배포는 `options.start`에서 런타임 start command를 구성한다.
- 배포 전 GitHub Actions 단계에서 `agent/scouter/scouter.agent.jar`가 준비되어 있어야 한다.
- start command는 `printf`로 `/tmp/scouter.conf`를 만들고 `JAVA_TOOL_OPTIONS`에 `-javaagent` 옵션을 넣는 패턴을 따른다.
- 수집 서버 IP는 GitHub Secrets의 `SCOUTER_COLLECTOR_IP`를 사용한다.
- 정상 기동 로그에는 `Starting Application with Scouter Agent...` 같은 식별 가능한 메시지가 남아야 한다.
- start command 안의 agent jar 경로와 Actions에서 준비한 파일 경로가 일치하는지 `test -f` 등으로 확인한다.

## CI Troubleshooting

- `Failed to load ApplicationContext`는 unresolved placeholder, S3/COS bean 생성, DB 연결, schema 적용 실패를 먼저 본다.
- Surefire report와 Maven `-e` 로그를 확인해 테스트 실패와 컨텍스트 부팅 실패를 구분한다.
- MariaDB 초기화 직후 테스트가 시작되면 health check 또는 DB 준비 대기 로직이 필요한지 확인한다.
- workflow 로그에 DB 비밀번호, COS 키, Cloudtype 토큰, Scouter collector IP가 그대로 출력되지 않게 한다.
