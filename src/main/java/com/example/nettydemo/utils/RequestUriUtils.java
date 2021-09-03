package com.example.nettydemo.utils;

import io.netty.handler.codec.http.HttpHeaders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zhaoxueke
 * @date 2021/09/02 14:12
 **/
public class RequestUriUtils {

    /**
     * 将路径参数转换成Map对象，如果路径参数出现重复参数名，将以最后的参数值为准
     * @param uri 传入的携带参数的路径
     * @return
     */
    public static Map<String, String> getParams(String uri) {
        Map<String, String> params = new HashMap<>(10);

        int idx = uri.indexOf("?");
        if (idx != -1) {
            String[] paramsArr = uri.substring(idx + 1).split("&");

            for (String param : paramsArr) {
                idx = param.indexOf("=");
                params.put(param.substring(0, idx), param.substring(idx + 1));
            }
        }

        return params;
    }

    public static Map<String, String> getHeader(HttpHeaders headers){
        Map<String, String> headerMap = new HashMap<>();
        headers.forEach(entry -> headerMap.put(entry.getKey(), entry.getValue()));
        return headerMap;
    }

    /**
     * 获取URI中参数以外部分路径
     * @param uri
     * @return
     */
    public static String getBasePath(String uri) {
        if (uri == null || uri.isEmpty())
            return null;

        int idx = uri.indexOf("?");
        if (idx == -1)
            return uri;

        return uri.substring(0, idx);
    }
}
