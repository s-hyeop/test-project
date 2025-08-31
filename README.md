# 환경 변수 관리 가이드

## 개요
이 프로젝트는 환경별(DB, Redis 등) 설정값을 **소스 코드와 분리**하여 관리

---

## 관리 파일

### 무시되는 실제 파일
- `/gradle.properties`
- `/src/main/resources/application.properties`

### 제공되는 예제 파일
- `/gradle.properties.example`
- `/src/main/resources/application.properties.example`

---

## 사용 방법

### 1. 예제 파일 사용 방법
 - .example 확장자 제거
```text
/gradle.properties.example
/src/main/resources/application.properties.example
```

---

### 2. `gradle.properties` 설정 (jooq 생성시 필요)
Gradle 빌드 및 실행 시 사용되는 공통 프로퍼티를 정의합니다.

```properties
# gradle.properties
DB_HOST=localhost
DB_PORT=3306
DB_SCHEMA={mydb}
DB_USER={myuser}
DB_PASS={mypassword}
```

---

### 3. `application.properties` 설정

```properties
spring.application.name=test-project
spring.datasource.url=jdbc:mariadb://{DB_HOST}:{DB_PORT}/{DB_SCHEMA}
spring.datasource.username={DB_USER}
spring.datasource.password={DB_PASS}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
spring.data.redis.host={REDIS_HOST}
spring.data.redis.port={REDIS_PORT}
spring.data.redis.password={REDIS_PASS}
spring.data.redis.timeout=2000
spring.jooq.sql-dialect=mariadb
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${GMAIL_USERNAME}
spring.mail.password=${GMAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
logging.level.org.jooq.tools.LoggerListener=INFO

# 비밀키
app.jwt-secret={secret-key}

# 쿠키이름
app.jwt-refresh-token-cookie-name=refreshToken

# AT 만료(분), 15분
app.jwt-access-expiration-minutes=15

# RT 만료(분), 30일
app.jwt-refresh-expiration-minutes=43200

# AT 재발급 가능한 임계 시점, 3분
app.jwt-access-token-reissue-threshold-minutes=3

# redis 회원가입 인증 코드 TTL, 5분
app.redis-ttl-signup=300

# redis 비밀번호 찾기 인증 코드 TTL, 5분
app.redis-ttl-reset-password=600

# rate limit 10초 동안 5회 허용
app.redis-ratelimit-max-requests=5
app.redis-ratelimit-window-seconds=10
```

---

# 백엔드 가이드

## 버전 정보 (8.15 기준)

### JDK
-zulu openJDK 17, LTS, 요구 조건

### Spring Boot
- 3.5.4, LTS, 최신 버전

### MariaDB
- 11.8.3, LTS, 최신 버전

### jOOQ
- 3.9.25, 사용 가능한 최신 버전
- 버전별 최소 요구 버전:
  - Open Source 3.21.x → JDK 21
  - Open Source 3.20.x → JDK 21
  - Open Source 3.19.x → JDK 17, MariaDB 10.7
- 참고:
  - [JDK Support Matrix](https://www.jooq.org/download/support-matrix-jdk)
  - [jOOQ Support Matrix](https://www.jooq.org/download/support-matrix)

---


## 데이터 이동 흐름

```plaintext
ResponseEntity<DTO> Controller (var DTO) {
  1. DTO 유효성 검사
}

DTO Service (var DTO) {
  1. DTO를 pojo로 변환
  2. Repository 로 요청
  3. 반환된 pojo DTO로 변환
  * 트랜잭션 처리
}

Repository {
  pojo select(...params)
  string|int insert(pk, pojo) return pk
  int update(pk, pojo) return execute
  int delete(pk) return execute
}
```
