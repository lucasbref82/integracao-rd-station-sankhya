FROM registry.digicade.com.br/base/jre:11.15.v1
USER root

RUN yum install openssl -y
COPY install_cert.sh $PRERUN_DIR/

RUN chmod +x -R $PRERUN_DIR/*

USER digicade

WORKDIR /home/digicade

COPY target/*.jar integracao-rd-station.jar


CMD [ "-jar","integracao-rd-station.jar"  ]

EXPOSE 8080