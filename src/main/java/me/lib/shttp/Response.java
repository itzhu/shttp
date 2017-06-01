package me.lib.shttp;

import java.util.List;
import java.util.Map;

/**
 * Created by itzhu on 2017/5/11.
 * desc
 */

public class Response<T> {

    private String url;
    private int responseCode;
    private T data;
    private Map<String, List<String>> headers;
    private Exception exception;
    private String tag;

    public Response(String url) {
        responseCode = -1;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, List<String>> headers) {
        this.headers = headers;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getTag() {
        return tag;
    }

    public Response setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public void setError(int responseCode, Exception e) {
        this.responseCode = responseCode;
        this.exception = e;
    }

}
