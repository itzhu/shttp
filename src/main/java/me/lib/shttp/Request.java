package me.lib.shttp;

import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import me.lib.shttp.interfaces.IDataParser;

/**
 * Created by linlongxin on 2016/4/27.
 */
public class Request<T> {
    private static final String TAG = "Request";

    /**
     * 是否取消---不一定能取消，在连接已经形成时，请求取消不了
     */
    private boolean isCancel = false;

    //请求方法
    private int method;
    //请求url
    private String url;
    //请求params
    private Map<String, String> params;
    //请求头
    private Map<String, String> headers;

    private SSLContext sslContext;
    private HostnameVerifier hostnameVerifier;

    private String tag;

    private Class<T> clazz;

    /**
     * 数据解析的类
     */
    private IDataParser parser;

    /**
     * 实例化对象的时候获取class对象
     * 使用{link {@link Request#newInstance(Class<T>)}}
     */
    private Request() {
        headers = new HashMap<>();
        params = new HashMap<>();
    }

    /**
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Request<T> newInstance(Class<T> clazz) {
        return new Request().setClazz(clazz);
    }

    /**
     * @param <T>
     * @return
     */
    public static <T> Request<T> newInstance() {
        return new Request().setClazz(String.class);
    }

    /**
     * 设置response<T> T的返回类型
     * // TODO: 2017/5/12 本来想获取T的类型，没有成功
     *
     * @param clazz
     */
    private Request setClazz(Class<T> clazz) {
        this.clazz = clazz;
        return this;
    }

    /**
     * @return T类型 或者null
     */
    public Class<T> getClazz() {
        return clazz;
    }

    /**
     * 检测是否为https
     *
     * @return
     */
    public boolean isHttps() {
        return !TextUtils.isEmpty(url) && url.startsWith("https://");
    }

    public int getMethod() {
        return method;
    }

    public Request setMethod(int method) {
        this.method = method;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Request setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Request setParams(Map<String, String> params) {
        this.params = params;
        return this;
    }

    public Request addParam(String key, String value) {
        params.put(key, value);
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    /**
     * 设置请求头
     * 建议使用{{@link Request#addHeader(String, String)}}
     *
     * @param headers
     * @return
     */
    private Request setHeaders(Map<String, String> headers) {
        if (headers != null) this.headers = headers;
        return this;
    }

    /**
     * 添加请求头信息
     *
     * @param key
     * @param value
     * @return
     */
    public Request addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * ssl
     *
     * @param sslContext
     * @return
     */
    public Request setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
        return this;
    }

    public HostnameVerifier getHostnameVerifier() {
        return hostnameVerifier;
    }

    /**
     * ssl
     *
     * @param hostnameVerifier
     * @return
     */
    public Request setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
        return this;
    }

    public String getTag() {
        return tag;
    }

    /**
     * 标志字段
     *
     * @param tag
     * @return
     */
    public Request setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public IDataParser getParser() {
        return parser;
    }

    /**
     * 数据解析类，将String数据转换为T类型数据，此parser只对当前request有效
     *
     * @param parser
     * @return
     */
    public Request setParser(IDataParser parser) {
        this.parser = parser;
        return this;
    }

    /**
     * 执行这个请求
     * 实际调用 {{link {@link SHttp#doHttp(Request, ICallBack)}}}
     * 只是方便链式调用
     *
     * @param callBack
     */
    public void excute(ICallBack<T> callBack) {
        SHttp.getInstance().doHttp(this, callBack);
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }
}