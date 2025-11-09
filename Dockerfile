FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Gradle에서 app.jar 출력( build/libs/app.jar )이 전제
COPY build/libs/app.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-lc","echo PORT=$PORT && exec java \
  -Dserver.port=${PORT:-8080} \
  -Dserver.address=0.0.0.0 \
  -jar app.jar"]
