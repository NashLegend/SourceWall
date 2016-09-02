package net.nashlegend.sourcewall.request.interceptors;

import java.io.IOException;
import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by NashLegend on 16/7/8.
 */

public class RedirectInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        if (response.isRedirect()) {
            String tmpUrl = chain.request().url().toString();
            tmpUrl = tmpUrl.replaceAll("\\?.+", "");
            String article_reply_reg = "^http://(www|m).guokr.com/article/reply/\\d+/$";//http://www.guokr.com/article/reply/2903740/
            String post_reply_reg = "^http://(www|m).guokr.com/post/reply/\\d+/$";//http://www.guokr.com/post/reply/6148664/
            //上面两条，只有通知才会跳到
            String publish_post_reg = "http://www.guokr.com/group/\\d+/post/edit/";//这是发贴的链接跳转
            String reply_post_reg = "http://(www|m).guokr.com/post/\\d+/";//这是回贴的链接跳转http://www.guokr.com/post/754909/
            boolean flag = tmpUrl.matches(article_reply_reg)
                    || tmpUrl.matches(post_reply_reg)
                    || tmpUrl.matches(publish_post_reg)
                    || tmpUrl.matches(reply_post_reg) && "POST".endsWith(request.method());
            if (flag) {
                //匹配上了，要重定向，将code设置成200
                response = response.newBuilder().code(HttpURLConnection.HTTP_OK).build();
            }
        }
        return response;
    }
}