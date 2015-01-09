package com.example.sourcewall.connection;

import com.example.sourcewall.connection.api.UserAPI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by NashLegend on 2014/9/15 0015
 */
public class HttpFetcher {

    public static DefaultHttpClient defaultHttpClient;

    public static String post(String url, List<NameValuePair> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
        HttpResponse response = getDefaultHttpClient().execute(httpPost);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, HTTP.UTF_8);
    }

    public static String get(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        DefaultHttpClient defaultHttpClient1 = getDefaultHttpClient();
        HttpResponse response = defaultHttpClient1.execute(httpGet);
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, HTTP.UTF_8);
    }

    /**
     * @return 返回一个线程安全的HttpClient。
     */
    public static DefaultHttpClient getDefaultHttpClient() {
        if (defaultHttpClient == null) {
            defaultHttpClient = new DefaultHttpClient();
            ClientConnectionManager manager = defaultHttpClient.getConnectionManager();
            HttpParams params = defaultHttpClient.getParams();
            defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(params, manager.getSchemeRegistry()), params);
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
