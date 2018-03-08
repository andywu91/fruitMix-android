package com.winsun.fruitmix.http.request.factory;

import android.util.Base64;
import android.util.Log;

import com.winsun.fruitmix.http.HttpRequest;
import com.winsun.fruitmix.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2018/3/6.
 */

public abstract class BaseCloudHttpRequestForStationAPIFactory extends CloudHttpRequestFactory{


    public static final String TAG = BaseCloudHttpRequestForStationAPIFactory.class.getSimpleName();

    String stationID;

    static final String CALL_STATION_THROUGH_CLOUD_PRE = "/stations/";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_JSON = "/json";

    private static final String CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE = "/pipe";

    BaseCloudHttpRequestForStationAPIFactory(HttpHeader httpHeader, String stationID) {
        super(httpHeader);

        this.stationID = stationID;
    }

    @Override
    public HttpRequest createHttpGetRequest(String httpPath, boolean isGetStream) {

        return createHttpGetRequestThroughPipe(httpPath, isGetStream);
    }

    private HttpRequest createHttpGetRequestThroughPipe(String httpPath, boolean isGetStream) {

        Log.d(TAG, "createHttpGetRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        String queryStr = "";

        if (httpPath.contains("?")) {

            String[] splitResult = httpPath.split("\\?");

            httpPath = splitResult[0];

            queryStr = "&" + splitResult[1];
        }

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        String newHttpPath = getBaseUrlPathBeforePipeOrJson() + (isGetStream ? CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE : CALL_STATION_THROUGH_CLOUD_END_FOR_JSON) + "?resource=" + httpPathEncodeByBase64
                + "&method=GET" + queryStr;

        HttpRequest httpRequest = new HttpRequest(createUrl(newHttpPath), Util.HTTP_GET_METHOD);

        setHeader(httpRequest);

        return httpRequest;

    }

    @Override
    public HttpRequest createHttpPostRequest(String httpPath, String body, boolean isPostStream) {

        return createHttpHasBodyRequest(httpPath, Util.HTTP_POST_METHOD, body, isPostStream);

    }

    @Override
    public HttpRequest createHttpPatchRequest(String httpPath, String body) {
        return createHttpHasBodyRequest(httpPath, Util.HTTP_PATCH_METHOD, body, false);
    }

    @Override
    public HttpRequest createHttpPutRequest(String httpPath, String body) {
        return createHttpHasBodyRequest(httpPath, Util.HTTP_PUT_METHOD, body, false);
    }

    @Override
    public HttpRequest createHttpDeleteRequest(String httpPath, String body) {
        return createHttpHasBodyRequest(httpPath, Util.HTTP_DELETE_METHOD, body, false);
    }

    private HttpRequest createHttpHasBodyRequest(String httpPath, String method, String body, boolean isStream) {

        Log.d(TAG, "createHasBodyRequestThroughPipe: " + getGateway() + ":" + getPort() + httpPath);

        HttpRequest httpRequest = new HttpRequest(createUrl(getBaseUrlPathBeforePipeOrJson() + (isStream ? CALL_STATION_THROUGH_CLOUD_END_FOR_PIPE : CALL_STATION_THROUGH_CLOUD_END_FOR_JSON)),
                Util.HTTP_POST_METHOD);

        setHeader(httpRequest);

        String httpPathEncodeByBase64 = Base64.encodeToString(httpPath.getBytes(), Base64.NO_WRAP);

        try {

            JSONObject jsonObject;

            if (body.isEmpty())
                jsonObject = new JSONObject();
            else
                jsonObject = new JSONObject(body);

            jsonObject.put("resource", httpPathEncodeByBase64);
            jsonObject.put("method", method);

            String jsonObj = jsonObject.toString();

            Log.d(TAG, "createHttpHasBodyRequest: " + jsonObj);

            httpRequest.setBody(jsonObj);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return httpRequest;

    }

    protected abstract String getBaseUrlPathBeforePipeOrJson();


}