package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.UserInfo;
import net.nashlegend.sourcewall.request.JsonHandler;
import net.nashlegend.sourcewall.request.ResponseObject;

/**
 * Created by NashLegend on 16/7/8.
 */

public class UserInfoParser implements Parser<UserInfo> {
    @Override
    public UserInfo parse(String response, ResponseObject<UserInfo> responseObject)
            throws Exception {
        return UserInfo.fromJson(JsonHandler.getUniversalJsonObject(response, responseObject));
    }
}
