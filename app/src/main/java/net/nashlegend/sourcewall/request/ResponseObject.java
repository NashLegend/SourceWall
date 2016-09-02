package net.nashlegend.sourcewall.request;

import android.support.annotation.NonNull;

/**
 * Created by NashLegend on 2014/9/23 0023
 * 网络请求返回的数据，默认为错误结果
 */
public class ResponseObject<T> {
    public RequestObject<T> requestObject;
    public boolean ok = false;
    public int statusCode = -1;//http code，200，201，302，304，404，500等
    public Throwable throwable;
    public ResponseError error = ResponseError.UNKNOWN;//请求错误，断网，服务器错误等等
    public String error_message = "";//其他错误的error_message，如NPE
    public int code = ResponseCode.CODE_NONE;//json中返回的code
    public String message = "";//json返回值中的message
    public T result;//网络请求返回的结果(已经parse过的)
    public String body = "";//网络返回的结果，String
    public boolean isCancelled = false;//是否已经被取消，在ok==false的时候可以判断是否是由取消请求引起的
    public boolean isCached = false;//是否响应是通过缓存读取的

    /**
     * 从一个ResponseObject中复制一部分，类型可能不一样，但是其他参数一样，用于ResponseObject类型转换
     *
     * @param object
     * @return
     */
    public void copyPartFrom(@NonNull ResponseObject object) {
        requestObject = new RequestObject<>();
        if (object.requestObject != null) {
            requestObject.copyPartFrom(object.requestObject);
        }
        ok = object.ok;
        error = object.error;
        statusCode = object.statusCode;
        error = object.error;
        error_message = object.error_message;
        code = object.code;
        message = object.message;
    }

    @Override
    public String toString() {
        return result == null ? "" : result.toString();
    }

    @SuppressWarnings("StringBufferReplaceableByString")
    public String dump() {
        StringBuilder err = new StringBuilder();
        err.append("ok").append(":").append(ok).append("\n");
        err.append("statusCode").append(":").append(statusCode).append("\n");
        err.append("error").append(":").append(error).append("\n");
        err.append("error_message").append(":").append(error_message).append("\n");
        err.append("code").append(":").append(code).append("\n");
        err.append("message").append(":").append(message).append("\n");
        err.append("result").append(":").append(result).append("\n");
        if (!Utils.isEmpty(body)) {
            err.append("\n").append("body:  ").append(body).append("\n");
        }
        if (requestObject != null) {
            err.append("requestObject:\n").append(requestObject.dump());
        }
        return err.toString();
    }

}
