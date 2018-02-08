package com.winsun.fruitmix.parser;

import com.winsun.fruitmix.group.data.model.PrivateGroup;
import com.winsun.fruitmix.group.data.model.UserComment;
import com.winsun.fruitmix.user.User;
import com.winsun.fruitmix.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Administrator on 2018/1/19.
 */

public class RemoteGroupParser extends BaseRemoteDataParser implements RemoteDatasParser<PrivateGroup> {

    private Random mRandom;

    public RemoteGroupParser() {

        mRandom = new Random();

    }

    @Override
    public List<PrivateGroup> parse(String json) throws JSONException {

        String root = checkHasWrapper(json);

        List<PrivateGroup> groups = new ArrayList<>();

        JSONArray jsonArray = new JSONArray(root);

        RemoteOneCommentParser remoteOneCommentParser = new RemoteOneCommentParser();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObject = jsonArray.optJSONObject(i);

            String uuid = jsonObject.optString("uuid");
            String name = jsonObject.optString("name");
            String ownerGUID = jsonObject.optString("owner");
            long createTime = jsonObject.optLong("ctime");
            long mTime = jsonObject.optLong("mtime");

            String stationId = jsonObject.optString("stationId");

            JSONArray usersJSONArray = jsonObject.optJSONArray("users");

            List<User> users = new ArrayList<>(usersJSONArray.length());

            for (int j = 0; j < usersJSONArray.length(); j++) {

                JSONObject userObject = usersJSONArray.optJSONObject(j);

                User user = new User();
                user.setAssociatedWeChatGUID(userObject.optString("id"));
                user.setUserName(userObject.optString("nickName"));
                user.setAvatar(userObject.optString("avatarUrl"));

                Util.setUserDefaultAvatar(user, mRandom);

                users.add(user);
            }

            JSONObject lastCommentJson = jsonObject.optJSONObject("tweet");

            PrivateGroup group = new PrivateGroup(uuid, name, ownerGUID, users);

            group.setCreateTime(createTime);
            group.setModifyTime(mTime);

            group.setStationID(stationId);

            JSONObject stations = jsonObject.optJSONObject("station");

            group.setStationOnline(stations.optInt("isOnline") == 1);

            UserComment lastComment = remoteOneCommentParser.parse(lastCommentJson);

            if (lastComment != null) {

                Util.fillUserCommentUser(users, lastComment);

                lastComment.setGroupUUID(group.getUUID());
                lastComment.setStationID(group.getStationID());

            }

            group.setLastComment(lastComment);

            groups.add(group);
        }

        return groups;
    }


}
