package me.lib.shttp.interfaces;

/**
 * Created by itzhu on 2017/5/11.
 * desc 网络数据的解析
 */
public interface IDataParser {
    <T> T parserData(String data, Class<T> clazz) throws Exception;
}
