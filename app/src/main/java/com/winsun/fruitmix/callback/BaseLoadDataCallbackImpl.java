package com.winsun.fruitmix.callback;

import android.util.Log;

import com.winsun.fruitmix.model.operationResult.OperationResult;

import java.util.List;

/**
 * Created by Administrator on 2017/7/13.
 */

public class BaseLoadDataCallbackImpl<T> extends BaseDataCallbackImpl implements BaseLoadDataCallback<T> {

    public static final String TAG = BaseLoadDataCallbackImpl.class.getSimpleName();

    @Override
    public void onSucceed(List<T> data, OperationResult operationResult) {

    }

}
