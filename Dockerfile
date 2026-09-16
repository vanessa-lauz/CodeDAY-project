#imagem do java 25
FROM eclipse-temurin:25-jdk

#define pasta de trabalho dentro do container
WORKDIR /app

#copia a pasta src do projeto(arquivos do proj) pro container
COPY src /app/src

# Copia o frontend para dentro do container
COPY frontend /app/frontend

#compíla os arquivos java
RUN javac -d /app/out /app/src/*.java
#diz a porta usada pelo server
EXPOSE 10000
#quando container inicia, executa esse CoManDo e inicia o servidor.
CMD ["java", "-cp", "/app/out", "Servidor"]