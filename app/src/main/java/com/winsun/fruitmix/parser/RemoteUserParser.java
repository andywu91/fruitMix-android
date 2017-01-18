package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.model.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2016/8/31.
 */
public class RemoteUserParser implements RemoteDataParser<User> {

    @Override
    public List<User> parse(String json) throws JSONException {

        List<User> users = new ArrayList<>();
        JSONArray jsonArray;
        JSONObject itemRaw;

        RemoteUserJSONObjectParser remoteUserJSONObjectParser = new RemoteUserJSONObjectParser();
        jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            itemRaw = jsonArray.getJSONObject(i);

            User user = remoteUserJSONObjectParser.getUser(itemRaw);

            users.add(user);

        }

        return users;
    }
}
