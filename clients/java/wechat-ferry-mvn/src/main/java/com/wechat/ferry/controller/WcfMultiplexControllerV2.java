package com.wechat.ferry.controller;

import com.wechat.ferry.entity.TResponse;
import com.wechat.ferry.entity.vo.request.*;
import com.wechat.ferry.entity.vo.response.*;
import com.wechat.ferry.enums.ResponseCodeEnum;
import com.wechat.ferry.service.WeChatDllService;
import com.wechat.ferry.service.impl.WcfServiceMultiplexerV2;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 控制层-Java封装的微信机器人的HTTP API接口
 *
 * @author shiyeling@gmail.com
 * @date 2024-10-01 15:48
 */
@Slf4j
@RestController
@RequestMapping("/wechat-ferry/{token}")
@Api(tags = "微信消息处理-接口")
@CrossOrigin(originPatterns = "*", allowedHeaders = "*", allowCredentials = "true")
public class WcfMultiplexControllerV2 {

    @Autowired
    private WcfServiceMultiplexerV2 serviceMultiplexer;

    @ApiOperation(value = "查询机器人运行状态", notes = "botStatus")
    @PostMapping(value = "/botStatus")
    public TResponse<Boolean> botRunningStatus(@PathVariable String token) {
        try {
            WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
            return TResponse.ok(ResponseCodeEnum.SUCCESS, weChatDllService != null);
        } catch (Exception e) {
            log.error("查询登录状态出错：{}", e.getMessage(), e);
            return TResponse.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "查询登录状态", notes = "loginStatus")
    @PostMapping(value = "/loginStatus")
    public TResponse<Boolean> loginStatus(@PathVariable String token) {
        try {
            WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
            if (weChatDllService != null) {
                Boolean status = weChatDllService.loginStatus();
                return TResponse.ok(ResponseCodeEnum.SUCCESS, status);
            } else {
                return TResponse.fail("机器人不在线");
            }
        } catch (Exception e) {
            log.error("查询登录状态出错：{}", e.getMessage(), e);
            return TResponse.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "获取登录微信内部识别号UID", notes = "queryLoginWeChatUid")
    @PostMapping(value = "/loginWeChatUid")
    public TResponse<String> queryLoginWeChatUid(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        String weChatUid = weChatDllService.queryLoginWeChatUid();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, weChatUid);
    }


    @ApiOperation(value = "获取登录二维码", notes = "queryLoginQrCode")
    @PostMapping(value = "/loginQrCode")
    public TResponse<WxPpWcfQrCodeResp> queryLoginQrCode(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfQrCodeResp weChatUid = weChatDllService.queryLoginQrCode();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, weChatUid);
    }

    @ApiOperation(value = "获取登录微信信息", notes = "queryLoginWeChatInfo")
    @PostMapping(value = "/loginWeChatInfo")
    public TResponse<WxPpWcfLoginInfoResp> queryLoginWeChatInfo(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfLoginInfoResp resp = weChatDllService.queryLoginWeChatInfo();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "获取消息类型", notes = "queryMsgTypeList")
    @PostMapping(value = "/list/msgType")
    public TResponse<List<WxPpWcfMsgTypeResp>> queryMsgTypeList(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<WxPpWcfMsgTypeResp> list = weChatDllService.queryMsgTypeList();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取联系人", notes = "queryContactsList")
    @PostMapping(value = "/list/contacts")
    public TResponse<List<WxPpWcfContactsResp>> queryContactsList(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<WxPpWcfContactsResp> list = weChatDllService.queryContactsList();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取可查询数据库", notes = "queryDatabaseSql")
    @PostMapping(value = "/list/dbSql")
    public TResponse<List<WxPpWcfDatabaseRowResp>> queryDatabaseSql(@PathVariable String token,
                                                                    @Validated @RequestBody WxPpWcfDatabaseSqlReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<WxPpWcfDatabaseRowResp> list = weChatDllService.execDbQuerySql(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取数据库所有表名称", notes = "queryDatabaseAllTableName")
    @PostMapping(value = "/list/dbTableName")
    public TResponse<List<String>> queryDatabaseAllTableName(@PathVariable String token) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<String> list = weChatDllService.queryDbTableNameList();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取指定数据库中的表", notes = "queryDatabaseTable")
    @PostMapping(value = "/list/dbTable")
    public TResponse<Map<String, String>> queryDatabaseTable(@PathVariable String token,
                                                             @Validated @RequestBody WxPpWcfDatabaseTableReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<WxPpWcfDatabaseTableResp> wxPpWcfDatabaseTableResps = weChatDllService.queryDbTableList(request);
        Map<String, String> list = wxPpWcfDatabaseTableResps.stream().collect(Collectors.toMap(
            WxPpWcfDatabaseTableResp::getSql, WxPpWcfDatabaseTableResp::getTableName
        ));
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    // @ApiOperation(value = "获取语音消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/voiceMsg")
    // public TResponse<Object> queryVoiceMsg() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }

    @ApiOperation(value = "查询群成员", notes = "queryGroupMember")
    @PostMapping(value = "/list/groupMember")
    public TResponse<List<WxPpWcfGroupMemberResp>> queryGroupMember(@PathVariable String token,
                                                                    @Validated @RequestBody WxPpWcfGroupMemberReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        List<WxPpWcfGroupMemberResp> list = weChatDllService.queryGroupMemberList(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "发送消息汇总入口", notes = "sendMsgMaster")
    @PostMapping(value = "/send/msgMaster")
    public TResponse<WxPpWcfSendTextMsgResp> sendMsgMaster(@Validated @RequestBody String jsonString) {

        return TResponse.ok(ResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "发送文本消息（可 @）", notes = "sendTextMsg")
    @PostMapping(value = "/send/textMsg")
    public TResponse<WxPpWcfSendTextMsgResp> sendTextMsg(@PathVariable String token,
                                                         @Validated @RequestBody WxPpWcfSendTextMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendTextMsgResp resp = weChatDllService.sendTextMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送图片消息", notes = "sendImageMsg")
    @PostMapping(value = "/send/imageMsg")
    public TResponse<WxPpWcfSendImageMsgResp> sendImageMsg(@PathVariable String token,
                                                           @Validated @RequestBody WxPpWcfSendImageMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendImageMsgResp resp = weChatDllService.sendImageMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送文件消息", notes = "sendFileMsg")
    @PostMapping(value = "/send/fileMsg")
    public TResponse<WxPpWcfSendFileMsgResp> sendFileMsg(@PathVariable String token,
                                                         @Validated @RequestBody WxPpWcfSendFileMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendFileMsgResp resp = weChatDllService.sendFileMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送XML消息", notes = "sendXmlMsg")
    @PostMapping(value = "/send/xmlMsg")
    public TResponse<WxPpWcfSendXmlMsgResp> sendXmlMsg(@PathVariable String token,
                                                       @Validated @RequestBody WxPpWcfSendXmlMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendXmlMsgResp resp = weChatDllService.sendXmlMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送表情消息", notes = "sendEmojiMsg")
    @PostMapping(value = "/send/emojiMsg")
    public TResponse<WxPpWcfSendEmojiMsgResp> sendEmojiMsg(@PathVariable String token,
                                                           @Validated @RequestBody WxPpWcfSendEmojiMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendEmojiMsgResp resp = weChatDllService.sendEmojiMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送富文本消息", notes = "sendRichTextMsg")
    @PostMapping(value = "/send/richTextMsg")
    public TResponse<WxPpWcfSendRichTextMsgResp> sendRichTextMsg(@PathVariable String token,
                                                                 @Validated @RequestBody WxPpWcfSendRichTextMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendRichTextMsgResp resp = weChatDllService.sendRichTextMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "拍一拍群友", notes = "patOnePat")
    @PostMapping(value = "/patOnePat")
    public TResponse<WxPpWcfSendPatOnePatMsgResp> patOnePat(@PathVariable String token,
                                                            @Validated @RequestBody WxPpWcfPatOnePatMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(token);
        WxPpWcfSendPatOnePatMsgResp resp = weChatDllService.patOnePat(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    // @ApiOperation(value = "撤回消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "转发消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "开启接收消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "关闭接收消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //

    // @ApiOperation(value = "通过好友申请", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<WxPpSendPatOnePatMsgResp> friendApply(@Validated @RequestBody WxPpPatOnePatMsgReq request) {
    // // WxPpSendPatOnePatMsgResp resp = weChatDllService.patOnePat(request);
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    // }

    // @ApiOperation(value = "获取朋友圈消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "下载图片、视频、文件", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "解密图片", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "添加群成员", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "删除群成员", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }
    //
    // @ApiOperation(value = "邀请群成员", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/msgType")
    // public TResponse<Object> queryMsgTypeList() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }

    private final String tempDir = System.getProperty("java.io.tmpdir");

    /**
     * 向机器人管理服务传输文件，以便后续执行文件消息发送，
     * 目前这个版本的服务暂时不管理机器人的元数据信息，因此需要调用者自行拼接具体的机器人的文件发送目录，并通过path 参数传入
     *
     * @param multipartFile 文件
     * @return 写入结果对象
     */
    @PostMapping("/upload-file")
    public TResponse<String> saveFile(
        @PathVariable String token,
        @RequestPart("file") MultipartFile multipartFile) {
        if (!serviceMultiplexer.isBotOnline(token)) {
            return TResponse.fail("机器人不在线");
        }
        try {
//            String fileName = multipartFile.getOriginalFilename();
//            Path fileStoreDir = Path.of(tempDir, token);
//            log.info("Uploading file:{} to bot {}", fileName, fileStoreDir);
//            if (!Files.exists(fileStoreDir)) {
//                fileStoreDir = Files.createTempDirectory(Path.of(tempDir), token);
//            }
//            int seq = 1;
//            Path targetPath = Path.of(fileStoreDir.toString(), fileName);
//            while (Files.exists(targetPath)) {
//                // 如果目录中已经有同名文件，给他一个序号，不断尝试
//                log.info("Found existing file: {}", targetPath.toFile().getAbsolutePath());
//                targetPath = Path.of(fileStoreDir.toUri().getPath(),
//                    String.format("%s%d.%s", FilenameUtils.getBaseName(fileName), seq, FilenameUtils.getExtension(fileName)));
//                seq++;
//            }
//            File file = targetPath.toFile();
//            IOUtils.copy(multipartFile.getInputStream(), FileUtils.openOutputStream(file));
//            log.info("File written to {}", file.getAbsolutePath());

            TResponse<String> uploadResult = serviceMultiplexer.uploadStream(token, multipartFile);
//            Mono<String> stringMono = serviceMultiplexer.uploadStream(token, multipartFile.getInputStream(), multipartFile.getOriginalFilename());
//            // 方式2: 使用默认值
//            String response = stringMono.block(Duration.ofSeconds(15));
//            String response = file.getAbsolutePath();
            return uploadResult.getCode().equalsIgnoreCase("200") ?
                TResponse.ok(ResponseCodeEnum.SUCCESS, uploadResult.getData())
                : TResponse.fail("上传到远程Bot失败");
        } catch (Exception e) {
            log.error("Uploading file[{}] to bot with token {} has error", multipartFile.getOriginalFilename(), token, e);
            return TResponse.fail("文件上传失败：" + e.getMessage());
        }
    }


    @ApiOperation("更新机器人所在的端口")
    @PutMapping("/address")
    public TResponse<Boolean> updateBotPort(@PathVariable String token,
                                            @RequestParam(required = false) String ip,
                                            @RequestParam Integer wcfCmdPort,
                                            @RequestParam Integer helperPort,
                                            HttpServletRequest request) {
        try {
            String ipAddress = request.getHeader("X-Forwarded-For");

            if (isInvalidIp(ipAddress)) {
                ipAddress = request.getHeader("Proxy-Client-IP");
            }
            if (isInvalidIp(ipAddress)) {
                ipAddress = request.getHeader("WL-Proxy-Client-IP");
            }
            if (isInvalidIp(ipAddress)) {
                ipAddress = request.getHeader("HTTP_CLIENT_IP");
            }
            if (isInvalidIp(ipAddress)) {
                ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
            }
            if (isInvalidIp(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }

            // 处理多个IP的情况（如X-Forwarded-For可能包含代理链）
            if (ipAddress != null && ipAddress.contains(",")) {
                ipAddress = ipAddress.split(",")[0].trim();
            }

            log.info("Updating bot {} to address {}:{}/{}", token, ipAddress, wcfCmdPort, helperPort);
            serviceMultiplexer.registerBotLocation(token, ipAddress, wcfCmdPort, helperPort);
            return TResponse.ok(ResponseCodeEnum.SUCCESS, true);
        } catch (Exception e) {
            log.error("更新出错{}", e.getMessage(), e);
            return TResponse.fail("机器人注册出错：" + e.getMessage());
        }
    }

    @ApiOperation("更新机器人所在的端口")
    @DeleteMapping("/address")
    public TResponse<Boolean> updateBotPort(@PathVariable String token) {
        log.info("Updating bot {} to offline", token);
        serviceMultiplexer.deregisterBotLocation(token);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, true);
    }


    private boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip);
    }
}
