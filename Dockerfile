FROM registry.digicade.com.br/base/jre:11.15.v1
USER digicade

WORKDIR /home/digicade

COPY target/*.jar integracao-rd-station.jar

CMD [ "-jar","integracao-rd-station.jar"  ]

EXPOSE 8080