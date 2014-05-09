package info.tongrenlu.android.provider;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpMessage;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class HttpHelper {

    public static final String UTF8 = "utf-8";

    private final HttpClient client;
    private final CookieStore cookieStore;
    private final HttpContext localContext;
    private File cookieFile = null;

    private String referer = null;
    private String useragent = null;

    public HttpHelper() {
        HttpParams httpParams = new BasicHttpParams();
        httpParams.setParameter(ClientPNames.COOKIE_POLICY,
                                CookiePolicy.BROWSER_COMPATIBILITY);

        final SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http",
                                           PlainSocketFactory.getSocketFactory(),
                                           80));
        schemeRegistry.register(new Scheme("https",
                                           SSLSocketFactory.getSocketFactory(),
                                           443));
        ThreadSafeClientConnManager manager = new ThreadSafeClientConnManager(httpParams,
                                                                              schemeRegistry);
        this.client = new DefaultHttpClient(manager, httpParams);

        this.cookieStore = new BasicCookieStore();
        this.localContext = new BasicHttpContext();
        // Bind custom cookie store to the local context
        this.localContext.setAttribute(ClientContext.COOKIE_STORE,
                                       this.cookieStore);
    }

    public HttpResponse get(final String url) throws IOException {
        System.out.println("method: GET");
        System.out.println("   url: " + url);

        final HttpGet httpget = new HttpGet(url);
        this.initHttpHeader(httpget);
        HttpResponse response = null;
        try {
            response = this.client.execute(httpget, this.localContext);
        } catch (final ClientProtocolException e) {
            this.cookieStore.clear();
            throw e;
        }
        return response;
    }

    public String getAsHtml(String url) throws IOException {
        HttpResponse response = this.get(url);
        final HttpEntity entity = response.getEntity();
        // Consume response content
        String result = EntityUtils.toString(entity);
        entity.consumeContent();

        this.setReferer(url);
        return result;
    }

    public JSONObject getAsJson(String url) throws IOException, JSONException {
        HttpResponse response = this.get(url);
        final HttpEntity entity = response.getEntity();
        // Consume response content
        String json = EntityUtils.toString(entity);
        entity.consumeContent();
        return new JSONObject(json);
    }

    public byte[] getByteArray(String url) throws IOException {
        HttpResponse response = this.get(url);
        final HttpEntity entity = response.getEntity();
        // Consume response content
        byte[] data = EntityUtils.toByteArray(entity);
        entity.consumeContent();
        return data;
    }

    public HttpResponse post(String url, List<NameValuePair> nvps) throws IOException {
        System.out.println("method: POST");
        System.out.println("   url: " + url);
        for (final NameValuePair nvp : nvps) {
            System.out.println("form-" + nvp.getName() + " : " + nvp.getValue());
        }
        final HttpPost httppost = new HttpPost(url);
        this.initHttpHeader(httppost);
        HttpResponse response = null;
        try {
            httppost.setEntity(new UrlEncodedFormEntity(nvps, UTF8));
            response = this.client.execute(httppost, this.localContext);

        } catch (final ClientProtocolException e) {
            this.cookieStore.clear();
            throw e;
        }
        return response;
    }

    public String postAsHtml(String url, List<NameValuePair> nvps) throws IOException {
        HttpResponse response = this.post(url, nvps);
        final HttpEntity entity = response.getEntity();
        // Consume response content
        String result = EntityUtils.toString(entity);
        entity.consumeContent();
        this.setReferer(url);
        return result;
    }

    public JSONObject postAsJson(String url, List<NameValuePair> nvps) throws IOException, JSONException {
        HttpResponse response = this.post(url, nvps);
        final HttpEntity entity = response.getEntity();
        // Consume response content
        String json = EntityUtils.toString(entity);
        entity.consumeContent();
        return new JSONObject(json);
    }

    protected void initHttpHeader(final HttpMessage httpMessage) {
        httpMessage.addHeader("Accept",
                              "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        httpMessage.addHeader("Accept-Charset", "UTF-8;");
        httpMessage.addHeader("Accept-Encoding", "gzip, deflate");
        httpMessage.addHeader("Accept-Language", "ja,en-US;q=0.8,en;q=0.6");
        httpMessage.addHeader("Cache-Control", "max-age=0");
        httpMessage.addHeader("Connection", "keep-alive");
        httpMessage.addHeader("Pragma", "no-cache");
        if (StringUtils.isNotBlank(this.getReferer())) {
            httpMessage.addHeader("Referer", this.getReferer());
        }
        if (StringUtils.isNotBlank(this.useragent)) {
            httpMessage.addHeader("User-Agent", this.useragent);
        }
    }

    public void loadCookie(final File cookieFile) {
        this.cookieFile = cookieFile;
        try {
            final List<String> lines = FileUtils.readLines(cookieFile, "utf-8");
            for (final String line : lines) {
                final String[] cookieValue = StringUtils.split(line, ";");
                final String[] nameValue = StringUtils.split(cookieValue[0],
                                                             "=");
                final String name = nameValue[0];
                final String value = nameValue[1];
                final String domain = StringUtils.split(cookieValue[1], "=")[1];
                final String path = StringUtils.split(cookieValue[2], "=")[1];
                final long expires = Long.valueOf(StringUtils.split(cookieValue[3],
                                                                    "=")[1]);
                final int version = Integer.valueOf(StringUtils.split(cookieValue[4],
                                                                      "=")[1]);
                final boolean secure = Boolean.valueOf(StringUtils.split(cookieValue[5],
                                                                         "=")[1]);
                if (expires <= System.currentTimeMillis()) {
                    final BasicClientCookie cookie = new BasicClientCookie(name,
                                                                           value);
                    cookie.setDomain(domain);
                    cookie.setPath(path);
                    cookie.setExpiryDate(new Date(expires));
                    cookie.setVersion(version);
                    cookie.setSecure(secure);
                    this.cookieStore.addCookie(cookie);
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public void saveCookie() {
        if (this.cookieFile != null) {
            final List<Cookie> cookieList = this.cookieStore.getCookies();
            if (CollectionUtils.isEmpty(cookieList)) {
                System.out.println("no cookie to load.");
                return;
            }
            final List<String> lines = new LinkedList<String>();
            for (final Cookie cookie : cookieList) {
                final String line = this.cookieToString(cookie);
                if (StringUtils.isNotBlank(line)) {
                    lines.add(line);
                }
            }
            if (CollectionUtils.isNotEmpty(lines)) {
                try {
                    FileUtils.writeLines(this.cookieFile, "utf-8", lines);
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String cookieToString(final Cookie cookie) {
        if (cookie.getExpiryDate() == null) {
            return null;
        }

        final String name = cookie.getName();
        final String value = cookie.getValue();
        final String domain = cookie.getDomain();
        final String path = cookie.getPath();
        final long expires = cookie.getExpiryDate().getTime();
        final int version = cookie.getVersion();
        final boolean secure = cookie.isSecure();
        return String.format("%s=%s; Domain=%s; Path=%s; Expires=%d; Version=%d; Secure=%s",
                             name,
                             value,
                             domain,
                             path,
                             expires,
                             version,
                             secure);
    }

    public String getUseragent() {
        return this.useragent;
    }

    public void setUseragent(String useragent) {
        this.useragent = useragent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }
}
