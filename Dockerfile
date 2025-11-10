FROM eclipse-temurin:21-jre-alpine

ENV TZ=Asia/Seoul \
    JAVA_HOME=/opt/java/openjdk \
    PATH="/opt/java/openjdk/bin:${PATH}"

RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app
COPY build/libs/*.jar /app/app.jar

EXPOSE 8080
# 중요: sh -lc 쓰지 말고 exec-form으로 바로 java 실행
ENTRYPOINT ["java","-Dserver.port=${PORT}","-Dserver.address=0.0.0.0","-jar","/app/app.jar"]
