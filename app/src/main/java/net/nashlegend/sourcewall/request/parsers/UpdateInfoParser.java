package net.nashlegend.sourcewall.request.parsers;

import net.nashlegend.sourcewall.model.UpdateInfo;
import net.nashlegend.sourcewall.request.ResponseObject;

import org.json.JSONObject;

/**
 * Created by NashLegend on 2015/10/26 0026.
 */
public class UpdateInfoParser implements Parser<UpdateInfo> {
    @Override
    public UpdateInfo parse(String str, ResponseObject<UpdateInfo> responseObject)
            throws Exception {
        UpdateInfo info = UpdateInfo.fromJson(new JSONObject(str));
        responseObject.ok = true;
        return info;
    }
}
