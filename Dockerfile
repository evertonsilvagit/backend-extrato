FROM public.ecr.aws/docker/library/amazoncorretto:21-alpine-jdk

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]