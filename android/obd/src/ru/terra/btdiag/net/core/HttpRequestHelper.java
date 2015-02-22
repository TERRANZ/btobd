package ru.terra.btdiag.net.core;

import android.content.Context;
import com.google.gson.Gson;
import com.google.inject.Inject;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import roboguice.inject.ContextSingleton;
import ru.terra.btdiag.core.Logger;
import ru.terra.btdiag.core.SettingsService;
import ru.terra.btdiag.core.constants.Constants;
import ru.terra.btdiag.core.constants.URLConstants;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

//Синхронные вызовы rest сервисов, вызывать только внутри асинхронной таски
@ContextSingleton
public class HttpRequestHelper {
    private HttpClient hc;

    private String baseAddress = URLConstants.SERVER_URL;

    @Inject
    private SettingsService settingsService;
    private Context context;

    @Inject
    public HttpRequestHelper(Context context) {
        this.context = context;
        hc = new DefaultHttpClient();
        hc.getParams().setParameter("http.protocol.content-charset", "UTF-8");
    }

    public JsonResponce runSimpleJsonRequest(String uri) throws IOException, UnableToLoginException {
        HttpGet httpGet = new HttpGet(baseAddress + uri);
        return runRequest(httpGet);
    }

    private JsonResponce runRequest(HttpUriRequest httpRequest) throws UnableToLoginException, IOException {
        httpRequest.setHeader("Cookie", "JSESSIONID=" + settingsService.getSetting(Constants.CONFIG_SESSION, ""));
        for (Header h : httpRequest.getAllHeaders()) {
            Logger.i(context, "runRequest", "header: " + h.getName() + " = " + h.getValue());
        }
        Logger.i(context, "runRequest", "header: " + httpRequest.getHeaders("Cookie"));
        HttpResponse response = null;
        try {
            response = hc.execute(httpRequest);
        } catch (ConnectException e) {
            Logger.w(context, "HttpRequestHelper", "Connect exception " + e.getMessage());
            return null;
        } catch (IllegalStateException e) {
            Logger.w(context, "HttpRequestHelper", "IllegalStateException " + e.getMessage());
            return null;
        }
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        // Logger.i("HttpRequestHelper", "Received status code " +
        // statusCode);
        JsonResponce ret = new JsonResponce();
        ret.code = statusCode;
        if (HttpStatus.SC_OK == statusCode) {
            ret.json = response.getEntity().getContent();
        }
        return ret;
    }

    public JsonResponce runJsonRequest(String uri, NameValuePair... params) throws IOException, UnableToLoginException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        uri += "?";
        for (int i = 0; i < params.length; ++i) {
            uri += params[i].getName() + "=" + params[i].getValue() + "&";
        }
        uri = uri.substring(0, uri.length() - 1);
        HttpGet request = new HttpGet(baseAddress + uri);

        request.addHeader("Content-Type", "application/json");
        try {
            return runRequest(request);
        } catch (Exception e) {
            Logger.w(context, "HttpRequestHelper", "Failed to form request content" + e.getMessage());
            return new JsonResponce(null);
        }
    }

    public JsonResponce runJsonPUTRequest(String uri, String json) throws IOException, UnableToLoginException {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
//        uri += "?";
//        for (int i = 0; i < params.length; ++i) {
//            uri += params[i].getName() + "=" + params[i].getValue() + "&";
//        }
//        uri = uri.substring(0, uri.length() - 1);
        HttpPut request = new HttpPut(baseAddress + uri);
        StringEntity entity = new StringEntity(json);

        request.setEntity(entity);

        request.addHeader("Content-Type", "application/json");
        try {
            return runRequest(request);
        } catch (Exception e) {
            Logger.w(context, "HttpRequestHelper", "Failed to form request content" + e.getMessage());
            return new JsonResponce(null);
        }
    }


    public <T> T getForObject(String url, Class<T> targetClass, NameValuePair... params) throws IOException, UnableToLoginException {
        JsonResponce ret = runJsonRequest(url, params);
        if (ret == null)
            return null;
        if (ret.code == HttpStatus.SC_FORBIDDEN) {
            throw new UnableToLoginException();
        }
        try {
            return new Gson().fromJson(new InputStreamReader(ret.json), targetClass);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public <T> T putForObject(String url, Class<T> targetClass, String json) throws IOException, UnableToLoginException {
        JsonResponce ret = runJsonPUTRequest(url, json);
        if (ret == null)
            return null;
        if (ret.code == HttpStatus.SC_FORBIDDEN) {
            throw new UnableToLoginException();
        }
        try {
            return new Gson().fromJson(new InputStreamReader(ret.json), targetClass);
        } catch (NullPointerException e) {
            return null;
        }
    }
}