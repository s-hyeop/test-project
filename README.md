# 환경 변수 관리 가이드

## 개요
이 프로젝트는 환경별(DB, Redis 등) 설정값을 **소스 코드와 분리**하여 관리

---

## 📂 관리 파일

### 무시되는 실제 파일
- `/gradle.properties`  
- `/src/main/resources/application.properties`

### 제공되는 예제 파일
- `/gradle.properties.example`  
- `/src/main/resources/application.properties.example`

---

## 🛠️ 사용 방법

### 1. 예제 파일 복사
최초 프로젝트 클론 후 다음 명령을 실행하세요.

```bash
cp gradle.properties.example gradle.properties
cp src/main/resources/application.properties.example src/main/resources/application.properties
```

Windows PowerShell:
```powershell
copy gradle.properties.example gradle.properties
copy src\main\resources\application.properties.example src\main\resources\application.properties
```

---

### 2. `gradle.properties` 설정
Gradle 빌드 및 실행 시 사용되는 공통 프로퍼티를 정의합니다.

```properties
# gradle.properties
DB_HOST=localhost
DB_PORT=3306
DB_SCHEMA=mydb
DB_USER=myuser
DB_PASS=mypassword
```

---

### 3. `application.properties` 설정

```properties
# application.properties
spring.application.name=test-project

spring.datasource.url=jdbc:mariadb://${DB_HOST}:${DB_PORT}/${DB_SCHEMA}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASS}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
spring.data.redis.password=${REDIS_PASS}
spring.data.redis.timeout=2000

spring.jooq.sql-dialect=mariadb
logging.level.org.jooq.tools.LoggerListener=INFO
```

---

### 4. 실행
```bash
./gradlew bootRun
```
