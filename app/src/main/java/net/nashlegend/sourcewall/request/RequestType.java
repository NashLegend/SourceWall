package net.nashlegend.sourcewall.request;

/**
 * Created by NashLegend on 2016/10/28.
 */

public class RequestType {
    public static final int PLAIN = 0;
    public static final int UPLOAD = 1;
    /**
     * 理论上说下载返回结果没法用Parser，如果需要Parser，那么parse的是downloadFilePath
     * 所以建议使用DirectlyStringParser,因为在onNext中返回了downloadFilePath
     */
    public static final int DOWNLOAD = 2;
}
