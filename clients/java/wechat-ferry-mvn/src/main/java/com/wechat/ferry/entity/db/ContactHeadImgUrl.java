package com.wechat.ferry.entity.db;

import com.wechat.ferry.entity.proto.Wcf;
import com.wechat.ferry.entity.vo.response.WxPpWcfDatabaseFieldResp;
import com.wechat.ferry.entity.vo.response.WxPpWcfDatabaseRowResp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * 对应MicronMsg.db这个库中的下面这个库表结构数据
 * <pre>
 * CREATE TABLE ContactHeadImgUrl(
 *     usrName TEXT PRIMARY KEY, -- 联系人的
 *     smallHeadImgUrl TEXT,
 *     bigHeadImgUrl TEXT,
 *     headImgMd5 TEXT,
 *     reverse0 INT,
 *     reverse1 TEXT
 * )
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class ContactHeadImgUrl {
    public static final String TABLE_NAME = "ContactHeadImgUrl";
    String usrName;
    String smallHeadImgUrl;
    String bigHeadImgUrl;
    String headImgMd5;
    int reverse0;
    String reverse1;

    public ContactHeadImgUrl(Wcf.DbRow dbRow) {
        for (Wcf.DbField dbField : dbRow.getFieldsList()) {
            WxPpWcfDatabaseFieldResp fieldVo = new WxPpWcfDatabaseFieldResp();
            fieldVo.setType(String.valueOf(dbField.getType()));
            fieldVo.setColumn(dbField.getColumn());
            Object value = Constants.converterSqlVal(dbField.getType(), dbField.getContent());
            switch (dbField.getColumn()) {
                case "usrName":
                    this.usrName = (String) value;
                    break;
                case "smallHeadImgUrl":
                    this.smallHeadImgUrl = (String) value;
                    break;
                case "bigHeadImgUrl":
                    this.bigHeadImgUrl = (String) value;
                    break;
                case "headImgMd5":
                    this.headImgMd5 = (String) value;
                    break;
                case "reverse1":
                    this.reverse1 = (String) value;
                    break;
                case "reverse0":
                    this.reverse0 = (Integer) value;
                    break;
                default:
                    log.warn("位置的联系人库表字段:{}", dbField.getColumn());
            }
        }
    }

    public boolean checkFields() {
        boolean b = !StringUtils.isEmpty(usrName) && !StringUtils.isEmpty(smallHeadImgUrl);
        if (!b) {
            log.info("{} not valid", this);
        }
        return b;

    }

}
