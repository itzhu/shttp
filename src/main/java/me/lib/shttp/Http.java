package me.lib.shttp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

import me.lib.shttp.interfaces.Method;
import me.lib.shttp.ssl.SSLUtil;

/**
 * Created by itzhu on 2017/5/11.
 * desc
 */
class Http {

    /**
     * @param urlConnection
     * @param request
     * @return
     * @throws IOException
     */
    protected HttpURLConnection configURLConnection(HttpURLConnection urlConnection, Request request) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {

        if (request.isHttps()) {
            if (request.getSslContext() != null) {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(request.getSslContext().getSocketFactory());
            } else {
                SSLContext sslContext = SSLUtil.createSimpleSSLContext();
                if (sslContext != null) {
                    ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslContext.getSocketFactory());
                }
            }
        }

        urlConnection.setDoInput(true);
        urlConnection.setConnectTimeout(SHttp.Config.getInstance().getConnectionTimeOut());
        urlConnection.setReadTimeout(SHttp.Config.getInstance().getReadTimeOut());
        urlConnection.setInstanceFollowRedirects(SHttp.Config.getInstance().isUrlRedirect());  //重定向默认是true

        // 设置字符集
        urlConnection.setRequestProperty("Charset", "UTF-8");
        // 设置文件类型
        urlConnection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");

        //请求方法设置
        if (request.getMethod() == Method.GET) {
            urlConnection.setRequestMethod("GET");
        } else if (request.getMethod() == Method.POST) {
            //关于setDoOutput(true)
            // 网上查到的解释是，设置true，表示你发送的请求，会把body的内容发送至server端，即POST和PUT才需要使用。GET完全可以不用设置。
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);//不使用缓存

            urlConnection.setRequestMethod("POST");
        }

        //设置请求头
        addHeader(urlConnection, request);

        return urlConnection;
    }

    /**
     * 设置请求头
     *
     * @param urlConnection
     * @param request
     */
    protected void addHeader(URLConnection urlConnection, Request request) {
        //设置通用的请求头
        Map<String, String> commonHeaders = SHttp.Config.getInstance().getHeaders();
        if (commonHeaders != null) {
            for (Map.Entry<String, String> entry : commonHeaders.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        //设置request请求头
        Map<String, String> headers = request.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 设置post数据
     *
     * @param params
     * @return
     */
    protected String getPostParamBody(Map<String, String> params) {
        if (params != null) {
            StringBuilder paramStrBuilder = new StringBuilder();
            synchronized (this) {
                for (Map.Entry<String, String> map : params.entrySet()) {
                    try {
                        paramStrBuilder = paramStrBuilder.append("&").append(URLEncoder.encode(map.getKey(), "UTF-8")).append("=")
                                .append(URLEncoder.encode(map.getValue(), "UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                    }
                }
                paramStrBuilder.deleteCharAt(0);
            }
            return paramStrBuilder.toString();
        } else {
            return "";
        }
    }

    /**
     * 读取输入流信息，转化成String
     *
     * @param in
     * @return
     * @throws IOException
     */
    protected String readInputStream(InputStream in) throws IOException {
        String result = "";
        String line;
        if (in != null) {
            BufferedReader bin = new BufferedReader(new InputStreamReader(in));
            while ((line = bin.readLine()) != null) {
                result += line;
            }
        }
        return result;
    }

}
