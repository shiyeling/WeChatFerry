#!/usr/bin/bash
export deploy_target=aiserver.weizhukeji.net
export https_proxy=http://127.0.0.1:10809
mvn clean package -DskipTests
docker build -t wechat-ferry-client .
docker save -o /d/docker/images/wechat-ferry-client.img wechat-ferry-client:latest
scp /d/docker/images/wechat-ferry-client.img root@${deploy_target}:/home/docker/images
ssh root@${deploy_target} "docker load -i /home/docker/images/wechat-ferry-client.img"
ssh root@${deploy_target} "docker compose -f /home/wcf/docker-compose.yml down wechat_ferry_proxy"
ssh root@${deploy_target} "docker compose -f /home/wcf/docker-compose.yml up -d wechat_ferry_proxy"

