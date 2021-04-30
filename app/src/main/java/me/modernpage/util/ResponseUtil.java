package me.modernpage.util;

public class ResponseUtil<T> {
    private final int code;
    private final T data;
    private final String msg;

    public ResponseUtil(int code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ResponseUtil{" +
                "code=" + code +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }
}
