package com.winsun.fruitmix.logged.in.user;

import java.util.Collection;

/**
 * Created by Administrator on 2017/7/4.
 */

public interface LoggedInUserDataSource {

    boolean insertLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean deleteLoggedInUsers(Collection<LoggedInUser> loggedInUsers);

    boolean clear();

    Collection<LoggedInUser> getAllLoggedInUsers();

    LoggedInUser getCurrentLoggedInUser();

    void setCurrentLoggedInUser(LoggedInUser loggedInUser);

}