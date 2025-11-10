FROM eclipse-temurin:21-jre

ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

# Cloud Build에서 --build-arg JAR_FILE 로 정확 파일을 넘겨줄 예정
ARG JAR_FILE=build/libs/*-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080

ENTRYPOINT ["sh","-c","\
  echo PORT=$PORT && \
  java -version && \
  ls -l /app && \
  test -f /app/app.jar || (echo '❌ app.jar not found' && exit 1); \
  exec java \
    -XX:MaxRAMPercentage=75 \
    -XX:InitialRAMPercentage=20 \
    -XX:+ExitOnOutOfMemoryError \
    -Dserver.port=${PORT:-8080} \
    -Dserver.address=0.0.0.0 \
    -jar /app/app.jar \
"]
