FROM eclipse-temurin:11-jdk
LABEL net.weizhukeji.image.authors="yeling.shiweizhukeji.net"
ARG INSTALL_DIR=/opt/wzkj/wechat-ferry-client
RUN echo "Asia/Shanghai" > /etc/timezone
COPY target/wechat-ferry-mvn-*.jar ${INSTALL_DIR}/wechat-ferry-client.jar
#COPY bin ${INSTALL_DIR}/bin
COPY config ${INSTALL_DIR}/config
#COPY license ${INSTALL_DIR}/license
WORKDIR ${INSTALL_DIR}
EXPOSE 9201
#ENTRYPOINT ["/usr/bin/bash", "-c", "./bin/server.sh start dockerized -f"]
ENTRYPOINT ["java","-classpath","wechat-ferry-client.jar:config/","-Djava.net.preferIPv4Stack=true","org.springframework.boot.loader.PropertiesLauncher"]

