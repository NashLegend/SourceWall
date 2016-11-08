package net.nashlegend.sourcewall.request.interceptors;

import net.nashlegend.sourcewall.request.RequestCallBack;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by NashLegend on 16/6/29.
 * 上传的Interceptor
 */
public class UploadProgressInterceptor implements Interceptor {

    RequestCallBack callBack;

    public UploadProgressInterceptor(RequestCallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(originalRequest.body(),
                callBack);
        Request compressedRequest = originalRequest.newBuilder()
                .method(originalRequest.method(), progressRequestBody)
                .build();
        return chain.proceed(compressedRequest);
    }
}