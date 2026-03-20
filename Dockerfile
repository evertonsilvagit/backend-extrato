FROM public.ecr.aws/amazoncorretto/amazoncorretto:21

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "/app/app.jar"]