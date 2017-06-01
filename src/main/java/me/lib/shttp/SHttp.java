package me.lib.shttp;

import android.text.TextUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import me.lib.shttp.interfaces.IDataParser;
import me.lib.shttp.interfaces.Method;

/**
 * Created by itzhu on 2017/5/11.
 * desc
 */
public class SHttp extends Http {

    /**
     * 请求被取消
     */
    public static final int CODE_ERROR_REQUEST_CAECEL = -101;
    /**
     * data数据为空
     */
    public static final int CODE_ERROR_DATA_NULL = -102;
    /**
     * json解析错误
     */
    public static final int CODE_ERROR_JSONPARSER = -103;
    /**
     * 未知错误
     */
    public static final int CODE_ERROR_OTHER = -104;

    /**
     * 已知exception
     */
    public static final int CODE_ERROR_ECEPTION = -105;

    /**
     * responseCode!=200时的错误
     */
    public static final int CODE_ERROR_RESPONSECODE = -106;


    private static final String TAG = "SHttp";

    private static final SHttp ourInstance = new SHttp();

    public static SHttp getInstance() {
        return ourInstance;
    }

    private SHttp() {
    }

    /**
     * 不要在ui线程调用此方法
     *
     * @param request
     * @param <T>
     * @return Response<T>
     */
    public synchronized <T> Response<T> doAsync(Request<T> request) {
        Response<T> response = new Response(request.getUrl());

        //获取jsonparser和clazz
        IDataParser parser = request.getParser();
        Class clazz = request.getClazz();
        if (clazz != null && parser == null) {
            parser = SHttp.Config.getInstance().getParser();
        }

        if (!TextUtils.isEmpty(request.getTag())) {
            response.setTag(request.getTag());
        }

        //检查是否取消
        if (request.isCancel()) {
            response.setError(CODE_ERROR_REQUEST_CAECEL, new Exception(String.valueOf(CODE_ERROR_REQUEST_CAECEL)));
            return response;
        }

        try {
            String url = request.getUrl();
            if (request.getMethod() == Method.GET) {
                url = Util.createUrl(url, request.getParams());
            }
            int respondCode;
            HttpLog.d(TAG, "start");
            HttpURLConnection urlConnection;
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            //配置HttpURLConnection
            urlConnection = configURLConnection(urlConnection, request);

            //检查是否取消
            // TODO: 2017/5/12 不知道这里取消服务器端是否仍然执行????
            if (request.isCancel()) {
                response.setError(CODE_ERROR_REQUEST_CAECEL, new Exception(String.valueOf(CODE_ERROR_REQUEST_CAECEL)));
                return response;
            }

            //开始连接
            urlConnection.connect();

            //post提交数据
            if (request.getMethod() == Method.POST) {
                OutputStream ops = urlConnection.getOutputStream();
                String params = getPostParamBody(request.getParams());
                HttpLog.d(TAG, "post----params---" + params);
                ops.write(params.getBytes());
                ops.flush();
                ops.close();
            }

            HttpLog.d(TAG, "url->" + url);
            InputStream in = urlConnection.getInputStream();
            respondCode = urlConnection.getResponseCode();

            response.setResponseCode(respondCode);
            response.setHeaders(urlConnection.getHeaderFields());

            // 重定向
            if (respondCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                String location = urlConnection.getHeaderField("Location");
                request.setUrl(location);
                HttpLog.d(TAG, "302重定向Location : " + location);

                response = doAsync(request);

            } else if (respondCode != HttpURLConnection.HTTP_OK) {

                //请求失败，请求返回code不等于200
                in = urlConnection.getErrorStream();
                String info = readInputStream(in);
                in.close();
                HttpLog.d(TAG, "respond err   " + respondCode + "---" + info);
                response.setError(respondCode, new Exception(CODE_ERROR_RESPONSECODE + " responsecode!=200->info->" + info));

            } else {
                //获取网络数据成功
                String result = readInputStream(in);
                in.close();
                HttpLog.d(TAG, "respond  success->" + respondCode + "---" + result);

                //如果clazz和parser都不为空，且clazz不为基础数据类型，也不为String.class ,则开始数据解析
                // TODO: 2017/5/12 这里有可能会出错，建议只填写String.class或者自定义类型的数据
                if (parser != null && clazz != null && !clazz.isPrimitive() && !clazz.isAssignableFrom(String.class)) {
                    T data = parser.parserData(result, request.getClazz());
                    if (data != null) {
                        response.setData(data);
                    } else {
                        response.setData(null);
                        response.setError(CODE_ERROR_JSONPARSER, new Exception(String.valueOf(CODE_ERROR_JSONPARSER)));
                    }
                } else {
                    response.setData((T) result);
                }
            }
        } catch (Exception e) {
            response.setError(CODE_ERROR_ECEPTION, e);
            HttpLog.e(TAG, "error->", e);
        }
        return response;
    }

    /**
     * 执行网络请求，在线程池里面执行
     *
     * @param request
     * @param callBack
     * @param <T>
     */
    public static <T> void doHttp(Request<T> request, ICallBack<T> callBack) {
        RequestDispatcher.getInstance().addRequest(request, callBack);
    }

    /**
     * http参数配置
     */
    public static class Config {

        private static int TIME_CONNECTION_OUT = 10 * 1000;//读取超时时间
        private static int TIME_READ_OUT = 10 * 1000;//读取响应时间
        private static boolean urlIsRedirect = true;//url重定向
        private static int MAXTHREAD = 3;//线程池数量

        private static Config config = new Config();

        private Config() {
        }

        public static Config getInstance() {
            return config;
        }

        private int connectionTimeOut = 10 * 1000;//读取超时时间
        private int readTimeOut = 10 * 1000;//读取响应时间
        private boolean isUrlRedirect = true;//url重定向
        private int maxthread = MAXTHREAD;

        private IDataParser parser;
        private Map<String, String> headers;

        public Config setHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public int getConnectionTimeOut() {
            return connectionTimeOut;
        }

        /**
         * 连接超时时间
         *
         * @param connectionTimeOut
         * @return
         */
        public Config setConnectionTimeOut(int connectionTimeOut) {
            this.connectionTimeOut = connectionTimeOut < 0 ? TIME_CONNECTION_OUT : connectionTimeOut;
            return this;
        }

        public int getReadTimeOut() {
            return readTimeOut;
        }

        /**
         * 读取超时时间
         *
         * @param readTimeOut
         * @return
         */
        public Config setReadTimeOut(int readTimeOut) {
            this.readTimeOut = readTimeOut < 0 ? TIME_READ_OUT : readTimeOut;
            return this;
        }

        public boolean isUrlRedirect() {
            return isUrlRedirect;
        }

        /**
         * url重定向
         *
         * @param urlRedirect
         * @return
         */
        public Config setUrlRedirect(boolean urlRedirect) {
            isUrlRedirect = urlRedirect;
            return this;
        }

        public IDataParser getParser() {
            return parser;
        }

        /**
         * 设置通用的数据解析类
         *
         * @param parser
         * @return
         */
        public Config setParser(IDataParser parser) {
            this.parser = parser;
            return this;
        }

        public int getMaxthread() {
            return maxthread;
        }

        public Config setMaxthread(int maxthread) {
            this.maxthread = maxthread;
            return this;
        }
    }
}
