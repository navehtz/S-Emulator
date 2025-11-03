//package util.http;
//
//import okhttp3.Cookie;
//import okhttp3.CookieJar;
//import okhttp3.HttpUrl;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.*;
//
//public class SimpleCookieManager implements CookieJar {
//
//    private final Map<String, Map<String, Cookie>> cookies = new HashMap<>();
//
//    @Override
//    public synchronized void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookiesList) {
//        String host = url.host();
//        Map<String, Cookie> cookiesMap = cookies.computeIfAbsent(host, k -> new HashMap<>());
//        for (Cookie cookie : cookiesList) {
//            cookiesMap.put(cookie.name(), cookie);
//        }
//    }
//
//    @Override
//    public synchronized List<Cookie> loadForRequest(@NotNull HttpUrl url) {
//        Map<String, Cookie> cookiesMap = cookies.get(url.host());
//        return cookiesMap == null ? Collections.emptyList() : new ArrayList<>(cookiesMap.values());
//    }
//
//    public synchronized void clearDomain(String host) {
//        cookies.remove(host);
//    }
//
//    public synchronized void clearAll() {
//        cookies.clear();
//    }
//}
