package me.lib.shttp;

import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Created by itzhu on 2017/5/11.
 * desc
 */
public class Util {

    private static final String TAG = "Util";

    public static void showError(Exception e) {
        String message = e.getMessage();
        if (!TextUtils.isEmpty(message)) {
            if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_DATA_NULL))) {
                HttpLog.e(TAG, "data数据为空", e);
            } else if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_ECEPTION))) {
                HttpLog.e(TAG, "exception异常", e);
            } else if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_JSONPARSER))) {
                HttpLog.e(TAG, "数据解析错误", e);
            } else if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_OTHER))) {
                HttpLog.e(TAG, "其他错误", e);
            } else if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_REQUEST_CAECEL))) {
                HttpLog.e(TAG, "请求取消", e);
            } else if (message.startsWith(String.valueOf(SHttp.CODE_ERROR_RESPONSECODE))) {
                HttpLog.e(TAG, "返回code不为200", e);
            }
        } else {
            HttpLog.e(TAG, "未知错误", e);
        }
    }


    /**
     * get时URL后面添加参数
     * @param url
     * @param params
     * @return
     */
    public static String createUrl(String url, Map<String, String> params) {
        StringBuffer stringBuffer = new StringBuffer();
        if (params != null && params.size() > 0) {
            stringBuffer.append(url);
            stringBuffer.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                stringBuffer.append(entry.getKey() + "=" + entry.getValue());
            }
            return stringBuffer.toString();
        } else {
            return url;
        }
    }

    /**
     * URL 转码
     */
    public static String getURLEncoderString(String str) throws UnsupportedEncodingException {
        String result = null;
        result = java.net.URLEncoder.encode(str, "utf-8");
        return result;
    }
}
