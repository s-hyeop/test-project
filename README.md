# 프로젝트 설정 가이드

## 1. jOOQ 코드 생성 준비
1. `gradle.properties copy` 파일을 `gradle.properties` 로 이름 변경합니다.
2. `gradle.properties` 파일을 열어 DB 접속 정보와 필요한 환경 변수를 채워 넣습니다.
  ```properties
    jooqDBHost=your_db_host
    jooqDBPort=your_db_port
    jooqDBSchema=your_db_schema
    jooqDBUsername=your_db_username
    jooqDBPassword=your_db_password
  ```

## 2. Spring Boot 데이터베이스 연결 설정
1. `/src/main/resources/` 경로에서 `application-database.properties copy` 파일을 `application-database.properties` 로 이름 변경합니다.
2. `application-database.properties` 파일을 열어 실제 DB 연결 정보를 채워 넣습니다.
  ```properties
    DB_URL=jdbc:mariadb://localhost:3308/your_database
    DB_USERNAME=your_db_user
    DB_PASSWORD=your_db_password
  ```
