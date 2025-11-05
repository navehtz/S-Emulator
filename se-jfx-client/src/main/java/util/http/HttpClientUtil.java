package util.http;

import okhttp3.*;
import okhttp3.JavaNetCookieJar;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class HttpClientUtil {

    //private final static SimpleCookieManager COOKIE_MANAGER = new SimpleCookieManager();
    private final static CookieManager COOKIE_MANAGER = new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    public final static OkHttpClient HTTP_CLIENT =
            new OkHttpClient.Builder()
                    .cookieJar(new JavaNetCookieJar(COOKIE_MANAGER))
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .build();


    public static void runAsync(String finalUrl, Callback callback) {
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static void runAsync(Request request, Callback callback) {
        HTTP_CLIENT.newCall(request).enqueue(callback);
    }

    public static CookieManager getCookieManager() {
        return COOKIE_MANAGER;
    }

//    public static String runSyncGetBody(String finalUrl) throws IOException {
//        Request request = new Request.Builder()
//                .url(finalUrl)
//                .build();
//        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
//            if (!response.isSuccessful()) throw new IOException("HTTP " + response.code() + " " + response.message());
//            return response.body() != null ? response.body().string() : "";
//        }
//    }
//
//    public static void clearCookiesForHost(String host) {
//        COOKIE_MANAGER.clearDomain(host);
//    }
//
//    public static void clearAllCookies() {
//        COOKIE_MANAGER.clearAll();
//    }
//
//    public static void shutdown() {
//        HTTP_CLIENT.dispatcher().executorService().shutdown();
//        HTTP_CLIENT.connectionPool().evictAll();
//    }
}
