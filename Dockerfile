FROM registry.digicade.com.br/base/jre:11.15.v1
USER root

RUN yum install openssl -y

USER digicade

WORKDIR /home/digicade

COPY target/*.jar integracao-rd-station.jar
COPY install_cert.sh $PRERUN_DIR/

CMD [ "-jar","integracao-rd-station.jar"  ]

EXPOSE 8080