package com.wechat.ferry.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wechat.ferry.config.WeChatFerryProperties;
import com.wechat.ferry.entity.RemoteWcfBotLocator;
import com.wechat.ferry.entity.TResponse;
import com.wechat.ferry.exception.BizException;
import com.wechat.ferry.service.WeChatDllService;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 业务实现层-对接原本DLL的接口
 *
 * @author chandler
 * @date 2024-10-01 15:58
 */
@Slf4j
@Service
public class WcfServiceMultiplexerV2 {

    @Resource
    private WeChatFerryProperties weChatFerryProperties;

    private final Map<String, RemoteWcfBotLocator> registeredBots = new HashMap<>();

    private final Map<String, WeChatDllService> wcfRobotClients = new HashMap<>();

    public WcfServiceMultiplexerV2() {
        // 初始化 WebClient，可以设置基础URL或默认头等
//        WebClient.Builder webClientBuilder = WebClient.builder().

    }

    /**
     * 根据token获取机器人的dll调用服务
     *
     * @param token
     * @return
     */
    public WeChatDllService getService(String token) {
        if (registeredBots.containsKey(token)) {
            RemoteWcfBotLocator locator = registeredBots.get(token);
            String botUri = locator.getUri();
            log.info("Accessing bot : {} with token {} ", botUri, token);
            if (!wcfRobotClients.containsKey(botUri)) {
                createRobotClient(locator);
            } else {
                if (wcfRobotClients.get(locator.getUri()).isConnectionStale()) {
                    createRobotClient(locator);
                }
            }
            return wcfRobotClients.get(locator.getUri());
        } else {
            throw new BizException("机器人不在线");
        }
    }

    private void createRobotClient(RemoteWcfBotLocator locator) {
        try {
            WeChatDllServiceImpl weChatDllService = new WeChatDllServiceImpl(locator.getHost(), locator.getWcfCmdPort());
            weChatDllService.setWeChatFerryProperties(weChatFerryProperties);
            wcfRobotClients.put(locator.getUri(), weChatDllService);
        } catch (Exception e) {
            log.error("无法创建到机器人{}:{}的链接，{}", locator.getHost(), locator.getWcfCmdPort(), e.getMessage(), e);
            throw e;
        }
    }

    private void retireRobotClient(RemoteWcfBotLocator locator) {
        WeChatDllService weChatDllService = wcfRobotClients.get(locator.getUri());
        if (weChatDllService != null) {
            weChatDllService.retire();
        }
        wcfRobotClients.remove(locator.getUri());
    }

    public boolean isBotOnline(String token) {
        return registeredBots.containsKey(token);
    }

    public void registerBotLocation(String token, String host, int cmdPort, int helperPort) {
        if (isBotOnline(token)) {
            RemoteWcfBotLocator locator = registeredBots.get(token);
            log.info("Token {} is currently registered at {}", token, locator.getUri());
            retireRobotClient(registeredBots.get(token));
        }
        this.registeredBots.put(token, new RemoteWcfBotLocator(host, cmdPort, helperPort));
    }

    public void deregisterBotLocation(String token) {
        if (isBotOnline(token)) {
            retireRobotClient(registeredBots.get(token));
            this.registeredBots.remove(token);
        } else {
            log.warn("No bot registered with token {} ", token);
        }
    }

    public String uploadToBot(String token) {
        if (registeredBots.containsKey(token)) {

        } else {
            throw new RuntimeException("Bot is not online");
        }
        return null;
    }

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * 使用 InputStream 直接流式上传
     */
    public TResponse<String> uploadStream(String token, MultipartFile file) throws IOException {

        RemoteWcfBotLocator locator = registeredBots.get(token);
        OkHttpClient okHttpClient = buildHttpClient();
        log.info("Uploading file {} size:{}", file.getOriginalFilename(), file.getSize());
        // 创建请求体
        RequestBody requestBody = new MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file",
                URLEncoder.encode(Objects.requireNonNull(file.getOriginalFilename()), StandardCharsets.UTF_8),
                createRequestBody(file))
            .build();

        // 构建请求
        Request request = new Request.Builder()
            .url(locator.getUploadUrl())
            .post(requestBody)
            .build();

        // 执行请求并返回结果
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("上传失败: " + response.code() + " - " + response.message());
            }
            return objectMapper.readValue(response.body().string(), new TypeReference<>() {
            });
        }
    }

    public ResponseEntity<InputStream> getLoginQrCodeImage(String botToken) {
        RemoteWcfBotLocator locator = registeredBots.get(botToken);

        OkHttpClient okHttpClient = buildHttpClient();
        Request request = new Request.Builder()
            .url(locator.getQrCodeImageDownloadUrl())
            .get()
            .build();

        try {
            Response response = okHttpClient.newCall(request).execute();

            if (!response.isSuccessful()) {
                response.close();
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
            }

            // 获取内容类型
            String contentType = response.header("Content-Type");
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/bmp";
            }

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.valueOf(contentType));

            // 尝试从URL提取文件名
            String filename = "qrcode_" + botToken + ".bmp";
            headers.setContentDispositionFormData("attachment", filename);
            return new ResponseEntity<>(response.body().byteStream(), headers, HttpStatus.OK);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 创建 RequestBody 用于 MultipartFile
     */
    private RequestBody createRequestBody(MultipartFile file) {
        return new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.parse(file.getContentType());
            }

            @Override
            public void writeTo(okio.BufferedSink sink) throws IOException {
                sink.write(file.getBytes());
            }

            @Override
            public long contentLength() throws IOException {
                return file.getSize();
            }
        };
    }

    public OkHttpClient buildHttpClient() {
        return new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)    // 连接超时
            .writeTimeout(10, TimeUnit.SECONDS)      // 写入超时（上传大文件需要更长时间）
            .readTimeout(10, TimeUnit.SECONDS)       // 读取超时
            .callTimeout(25, TimeUnit.SECONDS)      // 整个调用超时
            .build();
    }

    public WebClient buildWebClientToBot(String token) {
        RemoteWcfBotLocator locator = registeredBots.get(token);
        return buildWebClientToBot(locator);
    }

    public WebClient buildWebClientToBot(RemoteWcfBotLocator locator) {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000) // 连接超时30秒
            .responseTimeout(Duration.ofSeconds(15)) // 响应超时30秒
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30)) // 读取超时
            );

        return WebClient.builder()
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(10 * 1024 * 1024)) // 10MB内存缓冲
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();

    }

    /**
     * 上传字节数组
     */
//    private Mono<String> uploadBytes(String token, byte[] data, String targetUrl, String filename) {
//        MultipartBodyBuilder builder = new MultipartBodyBuilder();
//        builder.part("file", data)
//            .filename(filename)
//            .contentType(MediaType.APPLICATION_OCTET_STREAM);
//
//        return webClient.post()
//            .uri(targetUrl)
//            .contentType(MediaType.MULTIPART_FORM_DATA)
//            .body(BodyInserters.fromMultipartData(builder.build()))
//            .retrieve()
//            .bodyToMono(String.class);
//    }
}
