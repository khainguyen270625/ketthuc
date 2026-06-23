# ══════════════════════════════════════════════════════════════
#  Stage 1: BUILD
# ══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Cài iconv để convert encoding
RUN apk add --no-cache libc6-compat

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src

# Convert file application.properties sang UTF-8 thuần trước khi build
# (fix lỗi MalformedInputException do file lưu encoding Windows-1252)
RUN find /app/src/main/resources -name "*.properties" | while read f; do \
      cp "$f" "$f.bak" && \
      iconv -f WINDOWS-1252 -t UTF-8 "$f.bak" > "$f" 2>/dev/null || \
      iconv -f ISO-8859-1  -t UTF-8 "$f.bak" > "$f" 2>/dev/null || \
      cp "$f.bak" "$f"; \
      rm -f "$f.bak"; \
    done

RUN ./mvnw package -DskipTests -B

# ══════════════════════════════════════════════════════════════
#  Stage 2: RUN
# ══════════════════════════════════════════════════════════════
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

COPY --from=builder /app/target/*.jar app.jar

RUN chown spring:spring app.jar
USER spring

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dfile.encoding=UTF-8", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]