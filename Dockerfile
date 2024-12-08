# Estágio de build com Ubuntu e Maven manual
FROM ubuntu:22.04 AS build

# Atualize o repositório e instale OpenJDK e Maven
RUN apt-get update && \
    apt-get install -y openjdk-21-jdk wget tar && \
    wget https://downloads.apache.org/maven/maven-3/3.9.8/binaries/apache-maven-3.9.8-bin.tar.gz && \
    tar -xzf apache-maven-3.9.8-bin.tar.gz -C /opt && \
    ln -s /opt/apache-maven-3.9.8/bin/mvn /usr/bin/mvn

# Defina o diretório de trabalho no contêiner
WORKDIR /app

# Copie os arquivos de origem do projeto para o diretório de trabalho
COPY pom.xml .
COPY src ./src

# Realize o build do projeto e crie o arquivo .jar
RUN mvn clean package -DskipTests && ls -l target

# Estágio de execução com OpenJDK
FROM openjdk:21-jdk

# Defina o diretório de trabalho no contêiner
WORKDIR /app

# Copie o arquivo .env e o arquivo .jar criado no estágio de build para o diretório de trabalho
COPY .env .
COPY --from=build /app/target/borai-0.1.16-jar-with-dependencies.jar borai.jar

# Cria a pasta 'uploads' ao lado do .jar
RUN mkdir /app/uploads

# Exponha a porta 7373
EXPOSE 7373

# Comando para rodar a aplicação
CMD ["java", "-jar", "borai.jar"]