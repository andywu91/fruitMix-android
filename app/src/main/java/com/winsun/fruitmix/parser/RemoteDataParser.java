package com.winsun.fruitmix.parser;

import java.util.List;

/**
 * Created by Administrator on 2016/8/31.
 */
public interface RemoteDataParser<T> {

    List<T> parse(String json);
}
