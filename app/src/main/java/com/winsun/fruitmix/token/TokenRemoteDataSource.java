package com.winsun.fruitmix.token;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.http.BaseRemoteDataSourceImpl;
import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.http.HttpRequestFactory;
import com.winsun.fruitmix.http.IHttpUtil;
import com.winsun.fruitmix.parser.RemoteTokenParser;

/**
 * Created by Administrator on 2017/7/13.
 */

public class TokenRemoteDataSource extends BaseRemoteDataSourceImpl {

    public TokenRemoteDataSource(IHttpUtil iHttpUtil, HttpRequestFactory httpRequestFactory) {
        super(iHttpUtil, httpRequestFactory);
    }

    public void getToken(LoadTokenParam loadTokenParam, BaseLoadDataCallback<String> callback) {

        HttpRequest httpRequest = httpRequestFactory.createHttpGetTokenRequest(loadTokenParam);

        wrapper.loadCall(httpRequest, callback, new RemoteTokenParser());

    }

}