FROM ubuntu:22.04

ENV LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/opt/hik-isup/sdk/linux
RUN apt-get update && \
    apt-get install -y openjdk-17-jdk && \
    apt-get clean;

# 设置工作目录
WORKDIR /opt/hik-isup

COPY sdk ./sdk
COPY target/hik-isup-0.0.1.jar .

# 暴露端口
EXPOSE 16233

# 容器启动时执行的命令
CMD ["java", "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-jar", "hik-isup-0.0.1.jar"]
