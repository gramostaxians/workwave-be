FROM eclipse-temurin:21
WORKDIR /workwave-be
COPY mvnw pom.xml ./
COPY .mvn/ .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve
COPY src ./src
CMD ["./mvnw", "spring-boot:run"]