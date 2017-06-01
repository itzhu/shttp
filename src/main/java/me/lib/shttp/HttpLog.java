package me.lib.shttp;

import android.util.Log;

import me.lib.shttp.interfaces.Method;

/**
 * Created by alien on 2015/8/6.
 */
public class HttpLog {

    private static String DEBUG_TAG = "";
    private static int requestTimes = 0;
    private static boolean isDebug = false;  //是否开启debug模式，默认关闭

    public static void setDebug(boolean debug, String tag) {
        isDebug = debug;
        DEBUG_TAG = tag;
    }

    public static boolean isDebug() {
        return isDebug;
    }

    public static synchronized void Log(String info) {
        if (isDebug) {
            Log.i(DEBUG_TAG, info);
        }
    }

    public static synchronized int requestLog(int method, String info) {
        if (isDebug) {
            if (method == Method.GET) {
                Log.i(DEBUG_TAG, requestTimes + " times GET Request:" + info);
            } else {
                Log.i(DEBUG_TAG, requestTimes + " times POST Request:" + info);
            }
        }
        return requestTimes++;
    }

    public static synchronized void responseLog(String info, int requestNum) {
        if (isDebug) {
            Log.i(DEBUG_TAG, requestNum + " times Response:" + info);
        }
    }

    public static synchronized void requestImageLog(String info) {
        if (isDebug) {
            Log.i(DEBUG_TAG, requestTimes + " times RequestImage:" + info);
            requestTimes++;
        }
    }

    public static synchronized void d(String tag, String message) {
        if (isDebug) {
            Log.d(DEBUG_TAG, tag + "  ->  " + message);
        }
    }

    public static synchronized void e(String tag, String message, Exception e) {
        if (isDebug) {
            Log.e(DEBUG_TAG, tag + "  ->  " + message, e);
        }
    }
}
