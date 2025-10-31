package com.oldwei.isup.util;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

/**
 * WebFlux HTTP 请求工具类
 * 使用 WebClient 发送 HTTP 请求
 */
public class WebFluxHttpUtil {

    private static final WebClient webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    /**
     * POST 请求 - 发送 byte[] 数据
     * 适配服务端方案1: application/octet-stream
     *
     * @param url          请求地址
     * @param data         byte[] 数据
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T postBytes(String url, byte[] data, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * POST 请求 - 发送 byte[] 数据（异步）
     *
     * @param url          请求地址
     * @param data         byte[] 数据
     * @param responseType 响应类型
     * @return Mono<T>
     */
    public static <T> Mono<T> postBytesAsync(String url, byte[] data, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * POST 请求 - 发送 byte[] 数据（使用 Flux 流式传输，适合大文件）
     *
     * @param url          请求地址
     * @param data         byte[] 数据
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T postBytesStream(String url, byte[] data, Class<T> responseType) {
        DataBuffer dataBuffer = new DefaultDataBufferFactory().wrap(data);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Mono.just(dataBuffer), DataBuffer.class)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * POST 请求 - 发送 byte[] 数据，带自定义 Header
     *
     * @param url          请求地址
     * @param data         byte[] 数据
     * @param headers      自定义请求头
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T postBytesWithHeaders(String url, byte[] data, Map<String, String> headers, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                .bodyValue(data)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * POST 请求 - 发送 byte[] 数据，带超时
     *
     * @param url            请求地址
     * @param data           byte[] 数据
     * @param responseType   响应类型
     * @param timeoutSeconds 超时时间（秒）
     * @return T
     */
    public static <T> T postBytesWithTimeout(String url, byte[] data, Class<T> responseType, long timeoutSeconds) {
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .bodyValue(data)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();
    }

    /**
     * GET 请求 - 返回单个对象
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return Mono<T>
     */
    public static <T> T get(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .block();  // 阻塞获取结果
    }

    /**
     * GET 请求 - 异步返回
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return Mono<T>
     */
    public static <T> Mono<T> getAsync(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * GET 请求 - 带请求参数
     *
     * @param url          请求地址
     * @param params       请求参数
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T getWithParams(String url, Map<String, String> params, Class<T> responseType) {
        return webClient.get()
                .uri(uriBuilder -> {
                    uriBuilder.path(url);
                    params.forEach(uriBuilder::queryParam);
                    return uriBuilder.build();
                })
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * GET 请求 - 返回列表
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return Flux<T>
     */
    public static <T> Flux<T> getList(String url, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToFlux(responseType);
    }

    /**
     * POST 请求 - JSON 请求体
     *
     * @param url          请求地址
     * @param requestBody  请求体
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T post(String url, Object requestBody, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * POST 请求 - 异步
     *
     * @param url          请求地址
     * @param requestBody  请求体
     * @param responseType 响应类型
     * @return Mono<T>
     */
    public static <T> Mono<T> postAsync(String url, Object requestBody, Class<T> responseType) {
        return webClient.post()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType);
    }

    /**
     * PUT 请求
     *
     * @param url          请求地址
     * @param requestBody  请求体
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T put(String url, Object requestBody, Class<T> responseType) {
        return webClient.put()
                .uri(url)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * DELETE 请求
     *
     * @param url          请求地址
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T delete(String url, Class<T> responseType) {
        return webClient.delete()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * 带自定义 Header 的请求
     *
     * @param url          请求地址
     * @param headers      请求头
     * @param responseType 响应类型
     * @return T
     */
    public static <T> T getWithHeaders(String url, Map<String, String> headers, Class<T> responseType) {
        return webClient.get()
                .uri(url)
                .headers(httpHeaders -> headers.forEach(httpHeaders::add))
                .retrieve()
                .bodyToMono(responseType)
                .block();
    }

    /**
     * 带超时的请求
     *
     * @param url            请求地址
     * @param responseType   响应类型
     * @param timeoutSeconds 超时时间（秒）
     * @return T
     */
    public static <T> T getWithTimeout(String url, Class<T> responseType, long timeoutSeconds) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .block();
    }

    /**
     * 返回原始字符串
     *
     * @param url 请求地址
     * @return String
     */
    public static String getString(String url) {
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}

// ========== 使用示例 ==========

class Example {

    // 定义响应实体类
    static class User {
        private Long id;
        private String name;
        private String email;

        // getters and setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static void main(String[] args) {

        // 1. 简单的 GET 请求
        String deviceList = WebFluxHttpUtil.get("http://192.168.2.38:16233/mediaStream/deviceList", String.class);
        System.out.println(deviceList);

        // 2. 带参数的 GET 请求
        Map<String, String> params = Map.of("page", "1", "size", "10");
        User result = WebFluxHttpUtil.getWithParams("https://api.example.com/users", params, User.class);

        // 3. POST 请求
        User newUser = new User();
        newUser.setName("张三");
        newUser.setEmail("zhangsan@example.com");
        User created = WebFluxHttpUtil.post("https://api.example.com/users", newUser, User.class);

        // 4. 异步 GET 请求
        WebFluxHttpUtil.getAsync("https://api.example.com/users/1", User.class)
                .subscribe(u -> System.out.println("异步获取: " + u.getName()));

        // 5. 带自定义 Header
        Map<String, String> headers = Map.of("Authorization", "Bearer token123");
        User authUser = WebFluxHttpUtil.getWithHeaders("https://api.example.com/users/me", headers, User.class);

        // 6. 带超时
        User timeoutUser = WebFluxHttpUtil.getWithTimeout("https://api.example.com/users/1", User.class, 5);

        // 7. 获取字符串响应
        String json = WebFluxHttpUtil.getString("https://api.example.com/status");
        System.out.println(json);

        // 8. 获取列表数据
        WebFluxHttpUtil.getList("https://api.example.com/users", User.class)
                .subscribe(u -> System.out.println(u.getName()));
    }
}