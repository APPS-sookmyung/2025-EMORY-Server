FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/*.jar app.jar

# Cloud Run 환경변수 PORT를 Spring Boot가 직접 읽게 하기
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
