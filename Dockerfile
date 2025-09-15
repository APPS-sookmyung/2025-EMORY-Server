FROM eclipse-temurin:17-jre-alpine

ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

WORKDIR /app

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS=""
CMD ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
