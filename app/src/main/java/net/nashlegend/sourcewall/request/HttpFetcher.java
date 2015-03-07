package net.nashlegend.sourcewall.request;

import android.text.TextUtils;

import net.nashlegend.sourcewall.request.api.UserAPI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RequestWrapper;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpFetcher {

    private static DefaultHttpClient defaultHttpClient;
    private static DefaultHttpClient uploadHttpClient;
    private static final int MAX_EXECUTION_COUNT = 2;
    private final static int MAX_ROUTE_CONNECTIONS = 400;
    private final static int MAX_TOTAL_CONNECTIONS = 800;
    private final static int TIMEOUT = 15000;
    private final static int CONNECTION_TIMEOUT = 30000;//网络状况差的时候这个时间可能很长
    private final static int SO_TIMEOUT = 60000;
    private final static int UPLOAD_SO_TIMEOUT = 300000;//300秒的上传时间

    private final static ConcurrentHashMap<String, HttpUriRequest> requestHashMap = new ConcurrentHashMap<>();

    public static boolean abortRequestByTag(String tag) {
        requestHashMap.get(tag);
        return true;
    }

    private static String mapHttpUriRequest(HttpUriRequest request) {
        String key = UUID.randomUUID().toString();
        requestHashMap.put(key, request);
        return key;
    }

    private static void deleteHttpUriRequest(String tag) {
        if (requestHashMap.containsKey(tag)) {
            requestHashMap.remove(tag);
        }
    }

    public static ResultObject post(String url, List<NameValuePair> params) throws Exception {
        return post(url, params, true);
    }

    public static ResultObject post(String url, List<NameValuePair> params, boolean needToken) throws Exception {
        ResultObject resultObject = new ResultObject();
        HttpPost httpPost = new HttpPost(url);
        String token = UserAPI.getToken();
        if (params == null) {
            params = new ArrayList<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.add(new BasicNameValuePair("access_token", token));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        HttpResponse response = getDefaultHttpClient().execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject get(String url) throws Exception {
        ResultObject resultObject = new ResultObject();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = getDefaultHttpClient().execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject get(String url, List<NameValuePair> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new ArrayList<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.add(new BasicNameValuePair("access_token", token));
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (int i = 0; i < params.size(); i++) {
                NameValuePair p = params.get(i);
                paramString.append(p.getName()).append("=").append(p.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return get(url + paramString.toString());
    }

    public static ResultObject get(String url, List<NameValuePair> params) throws Exception {
        return get(url, params, true);
    }

    public static ResultObject put(String url, List<NameValuePair> params, boolean needToken) throws Exception {
        ResultObject resultObject = new ResultObject();
        HttpPut httpPut = new HttpPut(url);
        String token = UserAPI.getToken();
        if (params == null) {
            params = new ArrayList<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.add(new BasicNameValuePair("access_token", token));
        }
        httpPut.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        HttpResponse response = getDefaultHttpClient().execute(httpPut);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject put(String url) throws Exception {
        return put(url, new ArrayList<NameValuePair>(), true);
    }

    public static ResultObject put(String url, List<NameValuePair> params) throws Exception {
        return put(url, params, true);
    }

    public static ResultObject delete(String url) throws Exception {
        ResultObject resultObject = new ResultObject();
        HttpDelete httpDelete = new HttpDelete(url);
        DefaultHttpClient defaultHttpClient1 = getDefaultHttpClient();
        HttpResponse response = defaultHttpClient1.execute(httpDelete);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject delete(String url, List<NameValuePair> params) throws Exception {
        return delete(url, params, true);
    }

    public static ResultObject delete(String url, List<NameValuePair> params, boolean needToken) throws Exception {
        StringBuilder paramString = new StringBuilder("");
        String token = UserAPI.getToken();
        if (params == null) {
            params = new ArrayList<>();
        }
        if (needToken && !TextUtils.isEmpty(token)) {
            params.add(new BasicNameValuePair("access_token", token));
        }
        if (params.size() > 0) {
            paramString.append("?");
            for (int i = 0; i < params.size(); i++) {
                NameValuePair p = params.get(i);
                paramString.append(p.getName()).append("=").append(p.getValue()).append("&");
            }
            paramString.deleteCharAt(paramString.length() - 1);
        }
        return delete(url + paramString.toString());
    }

    /**
     * @return 返回一个线程安全的HttpClient。
     */
    synchronized public static DefaultHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new DefaultHttpClient();

            ClientConnectionManager manager = defaultHttpClient.getConnectionManager();
            HttpParams params = defaultHttpClient.getParams();
            //多线程请求
            defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
            defaultHttpClient.setHttpRequestRetryHandler(requestRetryHandler);
            defaultHttpClient.setRedirectHandler(redirectHandler);

            ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
            ConnManagerParams.setTimeout(params, TIMEOUT);//发起链接超时
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

            HttpConnectionParams.setStaleCheckingEnabled(params, false);
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);//连接服务器超时
            HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);//请求服务器超时
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            //设置Cookie
            BasicClientCookie cookie1 = new BasicClientCookie("_32353_access_token", UserAPI.getToken());
            cookie1.setDomain("guokr.com");
            cookie1.setPath("/");
            BasicClientCookie cookie2 = new BasicClientCookie("_32353_ukey", UserAPI.getUkey());
            cookie2.setDomain("guokr.com");
            cookie2.setPath("/");
            defaultHttpClient.getCookieStore().addCookie(cookie1);
            defaultHttpClient.getCookieStore().addCookie(cookie2);
        }
        return defaultHttpClient;
    }

    /**
     * @return 返回一个线程安全的上传用HttpClient。
     */
    synchronized public static DefaultHttpClient getDefaultUploadHttpClient() {
        if (uploadHttpClient == null) {
            uploadHttpClient = new DefaultHttpClient();
            ClientConnectionManager manager = uploadHttpClient.getConnectionManager();
            HttpParams params = uploadHttpClient.getParams();
            //多线程请求
            uploadHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
            ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
            ConnManagerParams.setTimeout(params, TIMEOUT);//发起链接超时
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

            HttpConnectionParams.setStaleCheckingEnabled(params, false);
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);//连接服务器超时
            HttpConnectionParams.setSoTimeout(params, UPLOAD_SO_TIMEOUT);//请求服务器超时
            HttpConnectionParams.setSocketBufferSize(params, 8192);

            //设置Cookie
            BasicClientCookie cookie1 = new BasicClientCookie("_32353_access_token", UserAPI.getToken());
            cookie1.setDomain("guokr.com");
            cookie1.setPath("/");
            BasicClientCookie cookie2 = new BasicClientCookie("_32353_ukey", UserAPI.getUkey());
            cookie2.setDomain("guokr.com");
            cookie2.setPath("/");
            uploadHttpClient.getCookieStore().addCookie(cookie1);
            uploadHttpClient.getCookieStore().addCookie(cookie2);
        }
        return uploadHttpClient;
    }

    /**
     * 链接跳转处理
     */
    private static DefaultRedirectHandler redirectHandler = new DefaultRedirectHandler() {

        @Override
        public boolean isRedirectRequested(HttpResponse response, HttpContext context) {
            boolean defaultRedirectFlag = super.isRedirectRequested(response, context);
            return defaultRedirectFlag && shouldRedirect(response, context);
        }

        @Override
        public URI getLocationURI(HttpResponse response, HttpContext context) throws ProtocolException {
            return super.getLocationURI(response, context);
        }
    };

    /**
     * 接管302跳转
     *
     * @param defaultURI defaultURI
     * @param response   response
     * @param context    context
     * @return URI
     */
    private static URI getProperRedirectRequest(URI defaultURI, HttpResponse response, HttpContext context) {
        String article_reply_reg = "^http://(www|m).guokr.com/article/\\d+/.+reply\\d+$";//http://www.guokr.com/article/439939/#reply290374
        if (defaultURI.toString().matches(article_reply_reg)) {
            context.setAttribute("http.request", new HttpGet("http://apis.guokr.com/minisite/article_reply/2903740.json"));
            return URI.create("http://apis.guokr.com/minisite/article_reply/2903740.json");
        }
        return defaultURI;
    }

    /**
     * 判断是否执行默认的跳转
     *
     * @param response response
     * @param context  context
     * @return 是否跳转，默认应该是true
     */
    synchronized private static boolean shouldRedirect(HttpResponse response, HttpContext context) {
        boolean flag = false;
        try {
            RequestWrapper wrapper = (RequestWrapper) context.getAttribute("http.request");
            HttpRequest request = wrapper.getOriginal();
            if (request instanceof HttpRequestBase) {
                String url = ((HttpRequestBase) request).getURI().toString();
                String article_reply_reg = "^http://(www|m).guokr.com/article/reply/\\d+/$";//http://www.guokr.com/article/reply/2903740/
                String post_reply_reg = "^http://(www|m).guokr.com/post/reply/\\d+/$";//http://www.guokr.com/post/reply/6148664/，只有通知才会跳到这
                String question_answer_reg = "^http://(www|m).guokr.com/answer/\\d+/redirect/$";//http://www.guokr.com/answer/778164/redirect/
                //问题貌似有点独立，第一次请求会要走一遍sso，所以就不在这里搞了
//                String publish_post_reg = "http://www.guokr.com/post/\\d+/";//
                String publish_post_reg = "http://www.guokr.com/group/\\d+/post/edit/";
                flag = !url.matches(article_reply_reg) && !url.matches(post_reply_reg) && !url.matches(publish_post_reg);
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = true;
        }
        return flag;
    }

    /**
     * 重试处理
     */
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {

        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return executionCount < MAX_EXECUTION_COUNT
                    && !(exception instanceof SSLHandshakeException)
                    && (exception instanceof NoHttpResponseException
                    || !(context.getAttribute(ExecutionContext.HTTP_REQUEST) instanceof HttpEntityEnclosingRequest));
        }
    };

    private static final int IO_BUFFER_SIZE = 8 * 1024;

    public static boolean downloadFile(String urlString, String dest) {
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        boolean errorCatch = false;
        try {
            final URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            File deskFile = new File(dest);
            if (deskFile.exists()) {
                deskFile.delete();
            } else if (!deskFile.getParentFile().exists()) {
                deskFile.getParentFile().mkdirs();
            }
            inputStream = urlConnection.getInputStream();
            fileOutputStream = new FileOutputStream(new File(dest));
            byte[] buff = new byte[IO_BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buff)) != -1) {
                fileOutputStream.write(buff, 0, len);
            }
        } catch (Exception e) {
            errorCatch = true;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                errorCatch = true;
            }
        }
        return !errorCatch;
    }

}
