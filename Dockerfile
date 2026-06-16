FROM eclipse-temurin:21
WORKDIR /workwave-be
COPY mvnw pom.xml ./
COPY .mvn/ .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:resolve
COPY src ./src

ENV USER_CONTRACTS_DIR=/opt/app/contracts

VOLUME /opt/app/contracts

CMD ["./mvnw", "spring-boot:run"]