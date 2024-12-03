package com.wechat.ferry.entity.db;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class Constants {
    public static final String MICRO_MSG_DB = "MicroMsg.db";
    public static final String CONTACT_HEAD_IMG_TABLE = ContactHeadImgUrl.TABLE_NAME;
    public static Map<Integer, Function<ByteString, Object>> sqlTypeMap = new HashMap<>();

    static {
        // 初始化SQL_TYPES 根据类型执行不同的Func
        Constants.sqlTypeMap.put(1, bytes -> {
            return Integer.parseInt(bytes.toString(StandardCharsets.UTF_8));
//            ByteBuffer wrap = ByteBuffer.wrap(bytes.toByteArray());
//            return wrap.getInt(0);
        });
//        sqlTypeMap.put(1, bytes -> ByteBuffer.wrap(bytes).getInt());
        Constants.sqlTypeMap.put(2, bytes -> ByteBuffer.wrap(bytes.toByteArray()).getFloat());
        Constants.sqlTypeMap.put(3, bytes -> new String(bytes.toByteArray(), StandardCharsets.UTF_8));
        Constants.sqlTypeMap.put(4, bytes -> bytes);
        Constants.sqlTypeMap.put(5, bytes -> null);
    }

    public static Function<ByteString, Object> getSqlType(int type) {
        return Constants.sqlTypeMap.get(type);
    }

    public static Object converterSqlVal(int type, ByteString content) {
        // 根据每一列的类型转换
        Function<ByteString, Object> converter = getSqlType(type);
        if (converter != null) {
            return converter.apply(content);
        } else {
            log.warn("未知的SQL类型: {}", type);
            return content.toByteArray();
        }
    }
}
