FROM eclipse-temurin:17-jre

# Diretório de trabalho
WORKDIR /app

# Copia o artefato construído pelo Gradle (fat jar)
# Ajuste o nome do arquivo se o artefato final mudar
ARG JAR_FILE=build/libs/backend-extrato-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# Porta exposta pelo Spring Boot (configurada em server.port=8083)
EXPOSE 8083

# Variáveis padrão do Java (opcional: reduzir consumo de memória em ambientes pequenos)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Comando de entrada
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
