package com.wechat.ferry.entity.vo.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 请求出参-获取登录二维码地址
 *
 * @author chandler
 * @date 2024/10/01 21:26
 */
@Data
@ApiModel(value = "WxPpWcfQrCodeResp", description = "WCF消息类型登录二维码返回参数结构体")
@AllArgsConstructor
public class WxPpWcfQrCodeResp {

    /**
     * 类型编号
     */
    @ApiModelProperty(value = "二维码地址")
    private String qrCodeUrl;

    /**
     * 类型名称
     */
    @ApiModelProperty(value = "错误消息")
    private String msg;

}
