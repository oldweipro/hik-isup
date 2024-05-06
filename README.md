# 海康ISUP

### 项目

最大程度保留SDK原始代码，SDK代码统一放到`com.oldwei.hikisup.sdk`包中。在SDK功能基础之上封装其接口以供外部调用。

### 端口放行

重要的是要放行7660/tcp和7660/udp

### 打包Docker

```shell
# 删除镜像
docker rmi oldweipro/hik-isup:latest

# 构建镜像
docker build -t oldweipro/hik-isup:latest .

# 导出镜像
docker save -o hik-isup.tar oldweipro/hik-isup:latest

# 停止容器
docker stop hik-isup

# 删除容器
docker rm hik-isup

# 删除镜像
docker rmi oldweipro/hik-isup:latest

# 加载镜像
docker load -i hik-isup.tar

# 构建容器
docker run -p 16233:16233 -p 7660:7660 -p 7665:7665 -p 7500:7500 -d --network=host --restart=always --name hik-isup oldweipro/hik-isup:latest

# 容器日志
docker logs -f --tail=300 hik-isup
```

### CentOS

CentOS 防火墙通常指的是 firewalld 服务，它是 CentOS 7 及更高版本的默认防火墙管理工具。以下是一些常用的 firewalld 命令：

启动防火墙：

```shell
sudo systemctl start firewalld
```

停止防火墙：

```shell
sudo systemctl stop firewalld
```

查看防火墙状态：

```shell
sudo systemctl status firewalld
```

设置防火墙开机自启：

```shell
sudo systemctl enable firewalld
```

禁用防火墙开机自启：

```shell
sudo systemctl disable firewalld
```

添加规则允许特定端口（例如，允许 TCP 80 端口）：

```shell
sudo firewall-cmd --zone=public --add-port=80/tcp --permanent
```

删除规则关闭特定端口（例如，关闭 TCP 8080 端口）：

```shell
sudo firewall-cmd --zone=public --remove-port=8080/tcp --permanent
```

重新载入防火墙以应用更改：

```shell
sudo firewall-cmd --reload
```

查看所有当前规则：

```shell
sudo firewall-cmd --list-all
```

请根据实际需要使用适当的命令。注意，--permanent 标志用于使更改永久生效，不加 --permanent 标志则只对当前会话生效。
