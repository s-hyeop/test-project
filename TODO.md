# Spring Boot TODO

## 기술 및 버전 (2025-08-15 기준)
- zulu openJDK 17 (LTS)
- Spring Boot 3.5.4 (LTS)
- MariaDB 11.8.3 (LTS)
- jOOQ 3.19.25
- Node 20.19.4 (LTS)

## 버전 선정의 주요 사유

### Spring Boot
- 3.5.x (LTS, Long Term Support), 그 중 최신 버전

### MariaDB
- 11.8.x (LTS, Long Term Support), 그 중 최신 버전

### jOOQ
- LTS 버전 없음  
- 버전별 최소 요구 버전:
  - Open Source 3.21.x → JDK 21
  - Open Source 3.20.x → JDK 21
  - Open Source 3.19.x → JDK 17, MariaDB 10.7  
- 참고:
  - [JDK Support Matrix](https://www.jooq.org/download/support-matrix-jdk)
  - [jOOQ Support Matrix](https://www.jooq.org/download/support-matrix)

---

# 스프링 부트 설계 흐름

## 데이터 이동 흐름

```plaintext
DTO Controller (var DTO) {
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
  string insert(pk, pojo) return pk
  boolean update(pk, pojo) return execute > 0
  boolean delete(pk) return execute > 0
}
```

## REST API 참고 사항
 - ISO-8601 시간 형식 등 REST 원칙 준수 (추가 조사 및 보완 예정)