# 海康ISUP

### 项目

最大程度保留SDK原始代码，SDK代码统一放到`com.oldwei.isup.sdk`包中。在SDK功能基础之上封装其接口以供外部调用。

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

基于对代码的分析，我发现了导致RTMP推流终端和管道流异常的几个关键问题和优化点：

🔍 问题分析

1. 管道流异常的主要原因

StreamHandler.java 中的问题：

- 缓冲区大小不足 (第42行): PipedInputStream(outputStream, 4096 * 5) 只有20KB缓冲区
- 缺乏背压机制: 当数据写入速度超过处理速度时，管道会堵塞
- 异常处理不完整 (第66-72行): 只对"Pipe closed"做了特殊处理，其他IO异常未分类处理

2. 连接断开检测不及时

StreamHandler.java 第119-127行：
while (running && (packet = grabber.grabPacket()) != null) {
count++;
recorder.recordPacket(packet);
completableFutureString.complete("true");//运行到这说明推流成功了
}

- 问题: 只在grabPacket()返回null时退出循环，网络异常不会立即触发重连

3. 资源释放顺序问题

StreamHandler.java 第134-160行：

- 问题: 先关闭grabber/recorder，再关闭流，可能导致数据丢失
- 风险: thread.interrupt() 可能中断正在进行的IO操作

🚀 优化建议

1. 优化管道流处理

// 建议改进的processStream方法
public void processStream(byte[] data) {
if (data == null || data.length == 0) {
log.debug("收到空数据包，忽略处理");
return;
}

      if (!running || outputStream == null) {
          log.debug("推流已停止，忽略数据包");
          return;
      }

      try {
          // 添加写入超时机制
          outputStream.write(data);
          outputStream.flush(); // 强制刷新，避免数据堆积
      } catch (IOException e) {
          String errorMsg = e.getMessage();
          if (errorMsg != null && errorMsg.contains("Pipe closed")) {
              log.warn("管道已关闭，停止推流");
              running = false;
          } else if (errorMsg != null && errorMsg.contains("Broken pipe")) {
              log.error("管道破裂，可能是消费者线程异常退出");
              running = false;
          } else {
              log.error("写入管道异常: {}", errorMsg, e);
              // 可以考虑添加重试机制
          }
      }

}

2. 增加连接健康检查

// 在推流循环中添加连接检测
private void startProcessing() {
thread = new Thread(() -> {
try {
// ... 初始化代码 ...

              // 添加连接监控
              long lastPacketTime = System.currentTimeMillis();
              long connectionTimeout = 30000; // 30秒无数据认为连接超时

              while (running) {
                  AVPacket packet = grabber.grabPacket();

                  if (packet == null) {
                      // 检查是否超时
                      if (System.currentTimeMillis() - lastPacketTime > connectionTimeout) {
                          log.error("连接超时，无数据超过{}毫秒", connectionTimeout);
                          break;
                      }
                      Thread.sleep(100); // 短暂等待
                      continue;
                  }

                  lastPacketTime = System.currentTimeMillis();

                  try {
                      recorder.recordPacket(packet);
                      if (count++ == 0) {
                          completableFutureString.complete("true"); // 第一次成功推流
                      }
                  } catch (Exception e) {
                      log.error("推流失败: {}", e.getMessage());
                      // 检查是否是网络错误
                      if (isNetworkError(e)) {
                          log.error("检测到网络错误，尝试重连...");
                          break; // 退出当前循环，外层会重新初始化
                      }
                      throw e;
                  }
              }
          } catch (Exception e) {
              completableFutureString.complete("false");
              log.error("推流线程异常: {}", e.getMessage(), e);
          } finally {
              // ... 清理代码 ...
          }
      });

}

3. 优化资源管理

// 改进的stopProcessing方法
public void stopProcessing() {
log.info("开始停止推流处理...");
running = false;

      // 1. 先关闭数据源，停止新数据写入
      try {
          if (outputStream != null) {
              outputStream.close();
          }
      } catch (IOException e) {
          log.warn("关闭输出流异常: {}", e.getMessage());
      }

      // 2. 等待处理线程自然结束（最多等待3秒）
      if (thread != null && thread.isAlive()) {
          try {
              thread.join(3000);
              if (thread.isAlive()) {
                  log.warn("处理线程未正常结束，强制中断");
                  thread.interrupt();
                  thread.join(1000); // 再等待1秒
              }
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
          }
      }

      // 3. 最后释放FFmpeg资源
      closeFFmpegResources();

      log.info("推流处理已完全停止");

}

4. 增加重连机制

// 添加重连逻辑
public class StreamHandler {
private static final int MAX_RETRY_COUNT = 3;
private static final long RETRY_DELAY_MS = 5000;
private int retryCount = 0;

      private void startProcessing() {
          while (retryCount < MAX_RETRY_COUNT && running) {
              try {
                  // ... 初始化代码 ...

                  // 重置重试计数
                  retryCount = 0;

                  // ... 主处理循环 ...

              } catch (Exception e) {
                  retryCount++;
                  log.error("推流异常 (尝试 {}/{}): {}", retryCount, MAX_RETRY_COUNT, e.getMessage());

                  if (retryCount < MAX_RETRY_COUNT) {
                      log.info("等待 {} 毫秒后重试...", RETRY_DELAY_MS);
                      try {
                          Thread.sleep(RETRY_DELAY_MS);
                      } catch (InterruptedException ie) {
                          Thread.currentThread().interrupt();
                          break;
                      }
                  } else {
                      log.error("达到最大重试次数，停止推流");
                      completableFutureString.complete("false");
                      break;
                  }
              } finally {
                  closeFFmpegResources();
              }
          }
      }

}

5. 配置优化建议

// 优化管道缓冲区大小
private static final int PIPE_BUFFER_SIZE = 1024 * 1024; // 1MB 缓冲区
private static final int GRABBER_BUFFER_SIZE = 4096 * 1024; // 4MB

// 在构造函数中
inputStream = new PipedInputStream(outputStream, PIPE_BUFFER_SIZE);
grabber = new FFmpegFrameGrabber(inputStream, GRABBER_BUFFER_SIZE);

// 优化编码参数
recorder.setVideoOption("crf", "28"); // 提高CRF值降低码率
recorder.setVideoBitrate(1500000); // 降低到1.5Mbps
recorder.setGopSize((int)(frameRate * 1.5)); // 减小GOP大小

📊 监控建议

1. 添加推流状态监控
2. 记录关键性能指标（帧率、码率、延迟）
3. 设置告警机制（连续丢帧、连接超时）
4. 实现优雅降级（网络差时自动降低画质）

这些优化应该能显著减少RTMP推流终端和管道流异常的发生频率。建议优先实施管道流优化和连接健康检查。

```
先帝创业未半而中道崩殂，
今天下三分，
益州疲弊，
此诚危急存亡之秋也。
然侍卫之臣不懈于内，
忠志之士忘身于外者，
盖追先帝之殊遇，
欲报之于陛下也。
诚宜开张圣听，
以光先帝遗德，
恢弘志士之气，
不宜妄自菲薄，
引喻失义，
以塞忠谏之路也。
宫中府中，
俱为一体，
陟罚臧否，
不宜异同。
若有作奸犯科及为忠善者，
宜付有司论其刑赏，
以昭陛下平明之理，
不宜偏私，
使内外异法也。
侍中、
侍郎郭攸之、
费祎、
董允等，
此皆良实，
志虑忠纯，
是以先帝简拔以遗陛下。
愚以为宫中之事，
事无大小，
悉以咨之，
然后施行，
必能裨补阙漏，
有所广益。
将军向宠，
性行淑均，
晓畅军事，
试用于昔日，
先帝称之曰能，
是以众议举宠为督。
愚以为营中之事，
悉以咨之，
必能使行阵和睦，
优劣得所。
亲贤臣，
远小人，
此先汉所以兴隆也；
亲小人，
远贤臣，
此后汉所以倾颓也。
先帝在时，
每与臣论此事，
未尝不叹息痛恨于桓、
灵也。
侍中、
尚书、
长史、
参军，
此悉贞良死节之臣，
愿陛下亲之信之，
则汉室之隆，
可计日而待也。
臣本布衣，
躬耕于南阳，
苟全性命于乱世，
不求闻达于诸侯。
先帝不以臣卑鄙，
猥自枉屈，
三顾臣于草庐之中，
咨臣以当世之事，
由是感激，
遂许先帝以驱驰。
后值倾覆，
受任于败军之际，
奉命于危难之间，
尔来二十有一年矣。
先帝知臣谨慎，
故临崩寄臣以大事也。
受命以来，
夙夜忧叹，
恐托付不效，
以伤先帝之明，
故五月渡泸，
深入不毛。
今南方已定，
兵甲已足，
当奖率三军，
北定中原，
庶竭驽钝，
攘除奸凶，
兴复汉室，
还于旧都。
此臣所以报先帝而忠陛下之职分也。
至于斟酌损益，
进尽忠言，
则攸之、
祎、
允之任也。
愿陛下托臣以讨贼兴复之效，
不效，
则治臣之罪，
以告先帝之灵。
若无兴德之言，
则责攸之、
祎、
允等之慢，
以彰其咎；
陛下亦宜自谋，
以咨诹善道，
察纳雅言。
深追先帝遗诏，
臣不胜受恩感激。
今当远离，
临表涕零，
不知所言。

```