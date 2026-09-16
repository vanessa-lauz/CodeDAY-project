#imagem do java 25
FROM eclipse-temurin:25-jdk

#define pasta de trabalho dentro do container
WORKDIR /app

#copia a pasta src do projeto(arquivos do proj) pro container
COPY src /app/src

#compíla os arquivos java
RUN javac -d /app/out /app/src/*.java

EXPOSE 10000 #diz a porta usada pelo server
#quando container inicia, executa esse CoManDo e inicia o servidor.
CMD ["java", "-cp", "/app/out", "Servidor"]