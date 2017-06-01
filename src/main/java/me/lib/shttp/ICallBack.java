package me.lib.shttp;

/**
 * Created by itzhu on 2017/5/11.
 * desc 结果回调
 */
interface ICallBack<T> {

    void onStart();

    void onNext(T t);

    void onCompleted();

    void onError(Exception e);
}
