FROM ubuntu:22.04

# 安装 OpenJDK 21 和 locales
RUN apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends \
        openjdk-21-jdk \
        locales \
        ffmpeg \
    && apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN ffmpeg -version

# 生成 en_US.UTF-8
RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8

WORKDIR /opt/hik-isup

COPY sdk ./sdk
COPY target/hik-isup-0.0.1.jar .

# 创建符号链接（确保 libssl.so 指向 1.1）
RUN cd /opt/hik-isup/sdk/linux && \
    [ -f libssl.so.1.1 ] && ln -sf libssl.so.1.1 libssl.so || (echo "Missing libssl.so.1.1!" && exit 1) && \
    [ -f libcrypto.so.1.1 ] && ln -sf libcrypto.so.1.1 libcrypto.so || (echo "Missing libcrypto.so.1.1!" && exit 1)

# 关键：优先加载 SDK 的 OpenSSL 1.1
ENV LD_LIBRARY_PATH=/opt/hik-isup/sdk/linux

EXPOSE 16233

# 启动（可加 LD_PRELOAD 保险）
CMD ["java", "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-jar", "hik-isup-0.0.1.jar"]