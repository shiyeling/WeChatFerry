package com.wechat.ferry.controller;

import com.wechat.ferry.entity.RemoteWcfBotLocator;
import com.wechat.ferry.entity.TResponse;
import com.wechat.ferry.entity.vo.request.*;
import com.wechat.ferry.entity.vo.response.*;
import com.wechat.ferry.enums.ResponseCodeEnum;
import com.wechat.ferry.service.WeChatDllService;
import com.wechat.ferry.service.impl.WcfServiceMultiplexer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 控制层-Java封装的微信机器人的HTTP API接口
 *
 * @author shiyeling@gmail.com
 * @date 2024-10-01 15:48
 */
@Slf4j
@RestController
@RequestMapping("/wechat-ferry")
@Api(tags = "微信消息处理-接口")
public class WcfMultiplexController {

    @Autowired
    private WcfServiceMultiplexer serviceMultiplexer;

    @ApiOperation(value = "查询登录状态", notes = "loginStatus")
    @PostMapping(value = "/loginStatus")
    public TResponse<Object> loginStatus(@RequestParam String host, @RequestParam int wcfCmdPort) {
        try {
            WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
            Boolean status = weChatDllService.loginStatus();
            return TResponse.ok(ResponseCodeEnum.SUCCESS, status);
        } catch (Exception e) {
            log.error("查询登录状态出错：{}", e.getMessage(), e);
            return TResponse.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "获取登录微信内部识别号UID", notes = "queryLoginWeChatUid")
    @PostMapping(value = "/loginWeChatUid")
    public TResponse<Object> queryLoginWeChatUid(@RequestParam String host, @RequestParam int wcfCmdPort) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        String weChatUid = weChatDllService.queryLoginWeChatUid();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, weChatUid);
    }

    @ApiOperation(value = "获取登录微信信息", notes = "queryLoginWeChatInfo")
    @PostMapping(value = "/loginWeChatInfo")
    public TResponse<WxPpWcfLoginInfoResp> queryLoginWeChatInfo(@RequestParam String host, @RequestParam int wcfCmdPort) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfLoginInfoResp resp = weChatDllService.queryLoginWeChatInfo();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "获取消息类型", notes = "queryMsgTypeList")
    @PostMapping(value = "/list/msgType")
    public TResponse<List<WxPpWcfMsgTypeResp>> queryMsgTypeList(@RequestParam String host, @RequestParam int wcfCmdPort) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        List<WxPpWcfMsgTypeResp> list = weChatDllService.queryMsgTypeList();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取联系人", notes = "queryContactsList")
    @PostMapping(value = "/list/contacts")
    public TResponse<List<WxPpWcfContactsResp>> queryContactsList(@RequestParam String host, @RequestParam int wcfCmdPort) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        List<WxPpWcfContactsResp> list = weChatDllService.queryContactsList();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取可查询数据库", notes = "queryDatabaseSql")
    @PostMapping(value = "/list/dbSql")
    public TResponse<List<WxPpWcfDatabaseRowResp>> queryDatabaseSql(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                                    @Validated @RequestBody WxPpWcfDatabaseSqlReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        List<WxPpWcfDatabaseRowResp> list = weChatDllService.queryDatabaseSql(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取数据库所有表名称", notes = "queryDatabaseAllTableName")
    @PostMapping(value = "/list/dbTableName")
    public TResponse<List<String>> queryDatabaseAllTableName(@RequestParam String host, @RequestParam int wcfCmdPort) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        List<String> list = weChatDllService.queryDatabaseAllTableName();
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "获取指定数据库中的表", notes = "queryDatabaseTable")
    @PostMapping(value = "/list/dbTable")
    public TResponse<Map<String, String>> queryDatabaseTable(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                             @Validated @RequestBody WxPpWcfDatabaseTableReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        Map<String, String> list = weChatDllService.queryDatabaseTable(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    // @ApiOperation(value = "获取语音消息", notes = "queryMsgTypeList")
    // @PostMapping(value = "/list/voiceMsg")
    // public TResponse<Object> queryVoiceMsg() {
    // return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    // }

    @ApiOperation(value = "查询群成员", notes = "queryGroupMember")
    @PostMapping(value = "/list/groupMember")
    public TResponse<List<WxPpWcfGroupMemberResp>> queryGroupMember(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                                    @Validated @RequestBody WxPpWcfGroupMemberReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        List<WxPpWcfGroupMemberResp> list = weChatDllService.queryGroupMember(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, list);
    }

    @ApiOperation(value = "发送消息汇总入口", notes = "sendMsgMaster")
    @PostMapping(value = "/send/msgMaster")
    public TResponse<WxPpWcfSendTextMsgResp> sendMsgMaster(@Validated @RequestBody String jsonString) {

        return TResponse.ok(ResponseCodeEnum.SUCCESS);
    }

    @ApiOperation(value = "发送文本消息（可 @）", notes = "sendTextMsg")
    @PostMapping(value = "/send/textMsg")
    public TResponse<WxPpWcfSendTextMsgResp> sendTextMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                         @Validated @RequestBody WxPpWcfSendTextMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendTextMsgResp resp = weChatDllService.sendTextMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送图片消息", notes = "sendImageMsg")
    @PostMapping(value = "/send/imageMsg")
    public TResponse<WxPpWcfSendImageMsgResp> sendImageMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                           @Validated @RequestBody WxPpWcfSendImageMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendImageMsgResp resp = weChatDllService.sendImageMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送文件消息", notes = "sendFileMsg")
    @PostMapping(value = "/send/fileMsg")
    public TResponse<WxPpWcfSendFileMsgResp> sendFileMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                         @Validated @RequestBody WxPpWcfSendFileMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendFileMsgResp resp = weChatDllService.sendFileMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送XML消息", notes = "sendXmlMsg")
    @PostMapping(value = "/send/xmlMsg")
    public TResponse<WxPpWcfSendXmlMsgResp> sendXmlMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                       @Validated @RequestBody WxPpWcfSendXmlMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendXmlMsgResp resp = weChatDllService.sendXmlMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送表情消息", notes = "sendEmojiMsg")
    @PostMapping(value = "/send/emojiMsg")
    public TResponse<WxPpWcfSendEmojiMsgResp> sendEmojiMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                           @Validated @RequestBody WxPpWcfSendEmojiMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendEmojiMsgResp resp = weChatDllService.sendEmojiMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "发送富文本消息", notes = "sendRichTextMsg")
    @PostMapping(value = "/send/richTextMsg")
    public TResponse<WxPpWcfSendRichTextMsgResp> sendRichTextMsg(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                                 @Validated @RequestBody WxPpWcfSendRichTextMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
        WxPpWcfSendRichTextMsgResp resp = weChatDllService.sendRichTextMsg(request);
        return TResponse.ok(ResponseCodeEnum.SUCCESS, resp);
    }

    @ApiOperation(value = "拍一拍群友", notes = "patOnePat")
    @PostMapping(value = "/patOnePat")
    public TResponse<WxPpWcfSendPatOnePatMsgResp> patOnePat(@RequestParam String host, @RequestParam int wcfCmdPort,
                                                            @Validated @RequestBody WxPpWcfPatOnePatMsgReq request) {
        WeChatDllService weChatDllService = serviceMultiplexer.getService(new RemoteWcfBotLocator(host, wcfCmdPort));
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
}
