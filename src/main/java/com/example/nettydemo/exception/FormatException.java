package com.example.nettydemo.exception;

/**
 * @description: 消息格式异常
 * @author: zhaoxueke
 * @date 2021/09/03 14:16
 **/
public class FormatException extends RuntimeException {

    public FormatException(String message) {
        super(message);
    }
}
