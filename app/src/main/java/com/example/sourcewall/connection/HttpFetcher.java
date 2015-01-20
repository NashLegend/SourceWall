package com.example.sourcewall.connection;

import com.example.sourcewall.connection.api.UserAPI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
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
import java.net.URL;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpFetcher {

    public static DefaultHttpClient defaultHttpClient;
    private static final int MAX_EXECUTION_COUNT = 2;
    public final static int MAX_ROUTE_CONNECTIONS = 400;
    public final static int MAX_TOTAL_CONNECTIONS = 800;
    public final static int TIMEOUT = 2000;
    public final static int SO_TIMEOUT = 3000;
    public final static int CONNECTION_TIMEOUT = 10000;

    public static ResultObject post(String url, List<NameValuePair> params) throws IOException {
        ResultObject resultObject = new ResultObject();
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        HttpResponse response = getDefaultHttpClient().execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    public static ResultObject get(String url) throws IOException {
        ResultObject resultObject = new ResultObject();
        HttpGet httpGet = new HttpGet(url);
        DefaultHttpClient defaultHttpClient1 = getDefaultHttpClient();
        HttpResponse response = defaultHttpClient1.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, HTTP.UTF_8);
        resultObject.statusCode = statusCode;
        resultObject.result = result;
        return resultObject;
    }

    /**
     * @return 返回一个线程安全的HttpClient。
     */
    public static DefaultHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new DefaultHttpClient();
            defaultHttpClient.setHttpRequestRetryHandler(requestRetryHandler);
            ClientConnectionManager manager = defaultHttpClient.getConnectionManager();
            HttpParams params = defaultHttpClient.getParams();
            //多线程请求
            defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);

            ConnManagerParams.setMaxTotalConnections(params, MAX_TOTAL_CONNECTIONS);
            ConnManagerParams.setTimeout(params, TIMEOUT);//发起链接超时
            ConnPerRouteBean connPerRoute = new ConnPerRouteBean(MAX_ROUTE_CONNECTIONS);
            ConnManagerParams.setMaxConnectionsPerRoute(params, connPerRoute);

            HttpConnectionParams.setStaleCheckingEnabled(params, false);
            HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);//连接到服务器超时
            HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);//服务器连接超时，连接过和
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
     * 重试处理
     */
    private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            if (executionCount >= MAX_EXECUTION_COUNT) {
                System.out.println("Retry Failed executionCount");
                return false;
            }
            System.out.println("Retry executionCount");
            if (exception instanceof NoHttpResponseException) {
                System.out.println("Retry NoHttpResponseException");
                return true;
            }
            if (exception instanceof SSLHandshakeException) {
                System.out.println("Retry Fail SSLHandshakeException");
                return false;
            }
            HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
            boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
            if (!idempotent) {
                System.out.println("Retry !idempotent");
                return true;
            }
            System.out.println("Retry False");
            return false;
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
        } catch (IOException e) {
            errorCatch = true;
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
                    if (!errorCatch) {
                        return true;
                    }
                }
            } catch (IOException e) {

            }
        }
        return false;
    }

}
