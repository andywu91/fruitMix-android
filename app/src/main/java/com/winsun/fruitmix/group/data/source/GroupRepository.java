package com.winsun.fruitmix.group.data.source;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

/**
 * Created by Administrator on 2017/7/20.
 */

public class GroupRepository {

    private static GroupRepository ourInstance;

    private GroupDataSource groupDataSource;

    public static GroupRepository getInstance(GroupDataSource groupDataSource) {
        if (ourInstance == null)
            ourInstance = new GroupRepository(groupDataSource);
        return ourInstance;
    }

    private GroupRepository(GroupDataSource groupDataSource) {
        this.groupDataSource = groupDataSource;
    }

    public void getGroupList(BaseLoadDataCallback<PrivateGroup> callback){

        callback.onSucceed(groupDataSource.getAllGroups(),new OperationSuccess());
    }

}