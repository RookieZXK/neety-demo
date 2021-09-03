package com.example.nettydemo.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zhaoxueke
 * @date 2021/09/02 14:24
 **/
public class JsonUtil {
    private static final Logger log = LogManager.getLogger(JsonUtil.class);

    public static final String DATEFORMAT_UTC = "yyyy-MM-dd'T'HH:mm:ss'+08:00'";
    static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setDateFormat(new SimpleDateFormat(DATEFORMAT_UTC));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    /**
     * JSON转Map对象
     *
     * @param json 字符串
     * @return map对象
     */
    public static Map<String, Object> jsonToMap(String json) {
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("jsonToMap is error.json is:{}, \n error is:{}", json, e.getMessage());
            return null;
        }
        return map;
    }

    /**
     * 对象转JSON字符串
     *
     * @param object
     * @return
     */
    public static String ObjectToJson(Object object) {
        if (object == null) {
            return null;
        }
        if (object.getClass() == String.class) {
            return object.toString();
        }
        String json = null;
        try {
            json = mapper.writeValueAsString(object);
        } catch (Exception e) {
            log.error("ObjectToJson is error.error is:{}", e.getMessage());
            return null;
        }
        return json;
    }

    /**
     * JSON转List
     *
     * @param json
     * @return
     */
    public static List<Map<String, Object>> jsonToList(String json) {
        List<Map<String, Object>> list = null;
        try {
            list = mapper.readValue(json, List.class);
        } catch (Exception e) {
            log.error("jsonToList is error.json is:{}, \n error is:{}", json, e.getMessage());
            return null;
        }
        return list;
    }

    /**
     * json转实体对象
     *
     * @param content   json对象
     * @param classType 实体类
     * @param <T>
     * @return
     */
    public static <T> T jsonToEntity(String content, Class<T> classType) {
        if (!jsonCheck(content)) {
            return null;
        }
        if (classType == String.class) {
            return (T) content;
        }
        try {
            return mapper.readValue(content, classType);
        } catch (Exception e) {
            log.error("jsonToEntity is error.json is:{}, \n error msg : {}", content, e.getMessage());
            return null;
        }
    }

    /**
     * json转实体对象
     *
     * @param content      json
     * @param valueTypeRef 实体类
     * @param <T>
     * @return
     */
    public static <T> T jsonToEntity(String content, TypeReference<T> valueTypeRef) {
        if (!jsonCheck(content)) {
            return null;
        }
        try {
            return (T) mapper.readValue(content, valueTypeRef);
        } catch (Exception e) {
            log.error("jsonToEntity is error.json is:{}, \n error msg : {}", content, e.getMessage());
            return null;
        }
    }

    private static boolean jsonCheck(String content) {
        if (content == null || content.equals("")) {
            return false;
        }
        return true;
    }
}
