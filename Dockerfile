# ---- Build stage ----
FROM eclipse-temurin:17-jdk-jammy AS build
WORKDIR /app

# Gradle wrapper & 설정 파일 먼저 복사 → 캐시 최적화
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 권한 주고 의존성 다운
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# 소스 복사 후 빌드
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

# ---- Run stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java","-Dserver.port=${PORT}","-Dspring.profiles.active=prod","-jar","/app/app.jar"]