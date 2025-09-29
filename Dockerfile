# Java 21 런타임 (클래스 버전 65)
FROM eclipse-temurin:21-jre-alpine

# 타임존 설정
ENV TZ=Asia/Seoul
RUN apk add --no-cache tzdata && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 앱 디렉터리
WORKDIR /app

# 빌드 산출물 복사 (bootJar 결과물)
COPY build/libs/*.jar app.jar

# Cloud Run은 8080을 사용 (문서화 목적)
EXPOSE 8080

# 추가 JVM 옵션을 외부에서 주입할 수 있도록
ENV JAVA_OPTS=""

# $PORT 주입 + PID 1 신호 처리 위해 exec 사용
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
