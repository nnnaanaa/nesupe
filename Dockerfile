# ローカルサーバ用イメージ（GitHub Pages には影響しません）
FROM gradle:8-jdk21 AS build
WORKDIR /src
COPY server/ ./
RUN gradle --no-daemon jar

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/build/libs/nesupe-server.jar /app/nesupe-server.jar
COPY index.html /app/www/index.html
COPY pages/ /app/www/pages/
ENV PORT=8080 WWW_DIR=/app/www DATA_DIR=/data
VOLUME /data
EXPOSE 8080
CMD ["java", "-jar", "/app/nesupe-server.jar"]
