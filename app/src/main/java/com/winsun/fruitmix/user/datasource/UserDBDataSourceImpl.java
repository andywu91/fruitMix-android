package com.winsun.fruitmix.user.datasource;

import com.winsun.fruitmix.callback.BaseLoadDataCallback;
import com.winsun.fruitmix.db.DBUtils;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.model.operationResult.OperationResult;
import com.winsun.fruitmix.model.operationResult.OperationSQLException;
import com.winsun.fruitmix.model.operationResult.OperationSuccess;

import java.util.Collection;
import java.util.List;

/**
 * Created by Administrator on 2017/7/10.
 */

public class UserDBDataSourceImpl implements UserDBDataSource {

    private DBUtils dbUtils;

    public UserDBDataSourceImpl(DBUtils dbUtils) {
        this.dbUtils = dbUtils;
    }

    @Override
    public void getUsers(BaseLoadDataCallback<User> callback) {

        List<User> users = dbUtils.getAllRemoteUser();

        callback.onSucceed(users, new OperationSuccess());
    }

    @Override
    public OperationResult insertUser(Collection<User> users) {

        long result = dbUtils.insertRemoteUsers(users);

        if (result > 0)
            return new OperationSuccess();
        else
            return new OperationSQLException();
    }

    @Override
    public boolean clearUsers() {
        return dbUtils.deleteAllRemoteUser() > 0;
    }
}
