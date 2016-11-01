package net.nashlegend.sourcewall.request.interceptors;

import net.nashlegend.sourcewall.request.RequestObject;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by NashLegend on 2016/10/31.
 */

public class SupplierInterceptor implements Interceptor {
    RequestObject requestObject;

    public SupplierInterceptor(RequestObject requestObject) {
        this.requestObject = requestObject;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder newBuilder = chain.request().newBuilder();
        if (requestObject.cacheControl != null) {
            newBuilder.cacheControl(requestObject.cacheControl);
        }
        if (requestObject.headers != null) {
            newBuilder.headers(requestObject.headers.build());
        }
        if (requestObject.requestBody != null) {
            newBuilder.method(requestObject.method.value(), requestObject.requestBody);
        }
        return chain.proceed(newBuilder.build());
    }
}
