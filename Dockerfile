# 1) jdk 설치
FROM azul/zulu-openjdk:17 as build

# 2) 디렉토리 이동
WORKDIR /app

# 3) gradle 복사
COPY backend/gradlew backend/gradle* ./ 
COPY backend/gradle ./gradle

# 4) 프로젝트 파일 복사
COPY backend/. .

# 5) 권한 설정
RUN chmod +x ./gradlew

# 6) gradle bootJar 실행
RUN ./gradlew clean bootJar -x test --no-daemon

# 7) Zulu JRE 17 런타임
FROM azul/zulu-openjdk:17-jre

# 8) 디렉토리 이동
WORKDIR /app

# 9) 타임존 변경
ENV TZ=Asia/Seoul

# 10) 빌드 산출물 복사
COPY --from=build /app/build/libs/*.jar /app/app.jar

# 11) 이건 뭔 설정?
EXPOSE 8080

# 12) 애플리케이션 실행
ENTRYPOINT ["java","-Duser.timezone=Asia/Seoul","-jar","/app/app.jar"]