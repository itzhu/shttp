package me.lib.shttp;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Created by itzhu on 2017/5/11.
 * desc 执行request请求
 */
class RequestDispatcher {
    public static final String TAG = "RequestDispatcher";


    private boolean isEmptyNetQueue = true;
    private LinkedBlockingDeque<Pair<Request, ICallBack>> mNetQueue;
    private static RequestDispatcher instance;
    private Handler mHandler;
    private ExecutorService mThreadPool; //线程池

    public static RequestDispatcher getInstance() {
        if (instance == null) {
            synchronized (RequestDispatcher.class) {
                if (instance == null) {
                    instance = new RequestDispatcher();
                }
            }
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private RequestDispatcher() {
        mNetQueue = new LinkedBlockingDeque<>();
        mThreadPool = Executors.newFixedThreadPool(SHttp.Config.getInstance().getMaxthread());
        // TODO: 2017/5/11  不是很明白这个handler，不知道会不会造成什么问题
        mHandler = new Handler(Looper.getMainLooper());
    }

//    public void executeRunnable(Runnable runnable) {
//        mThreadPool.execute(runnable);
//    }

    public <T> void addRequest(Request<T> request, ICallBack<T> callback) {
        mNetQueue.push(new Pair<Request, ICallBack>(request, callback));
        if (isEmptyNetQueue) {
            startDealNetRequest();
        }
    }

    /**
     * 网络请求轮询
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void startDealNetRequest() {
        while (!mNetQueue.isEmpty()) {
            Pair<Request, ICallBack> pair = mNetQueue.poll();
            final Request request = pair.first;
            final ICallBack callback = pair.second;

            if (callback != null) {
                //请求开始执行
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStart();
                    }
                });
            }
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {

                    final Response response = SHttp.getInstance().doAsync(request);

                    if (callback != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                HttpLog.d(TAG, "responseCode--" + response.getResponseCode());
                                if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                    HttpLog.d(TAG, "next--");
                                    callback.onNext(callback instanceof DataCallBack ? response.getData() : response);
                                    HttpLog.d(TAG, "onCompleted--");
                                    callback.onCompleted();
                                } else {
                                    HttpLog.d(TAG, "onerr--");
                                    callback.onError(response.getException());
                                }
                            }
                        });
                    }
                }
            });
            isEmptyNetQueue = false;
        }
        isEmptyNetQueue = true;
    }

    public void cancelAllNetRequest() {
        mNetQueue.clear();
    }

    public boolean cancelRequest(String url) {
        for (Pair<Request, ICallBack> pair : mNetQueue) {
            if (pair.first.getUrl().equals(url)) {
                mNetQueue.remove(pair);
                return true;
            }
        }
        return false;
    }

    public boolean cancelRequestByTag(String tag) {
        for (Pair<Request, ICallBack> pair : mNetQueue) {
            if (!TextUtils.isEmpty(pair.first.getTag()) && tag.equals(pair.first.getTag())) {
                mNetQueue.remove(pair);
                return true;
            }
        }
        return false;
    }
}
