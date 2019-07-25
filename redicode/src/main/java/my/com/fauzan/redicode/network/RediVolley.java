package my.com.fauzan.redicode.network;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONObject;

import my.com.fauzan.redicode.R;
import my.com.fauzan.redicode.RediView;
import my.com.fauzan.redicode.Util;

public class RediVolley {

    private Context context;
    private RequestQueue mRequestQueue;
    private String url;
    private JSONObject jsonObjectReq;
    private int timeout = 30000;
    public final String TAG = RediVolley.class.getSimpleName();

    public RediVolley(Context c, String url) {
        this.context = c;
        this.url = url;
    }

    public RediVolley(Context c, String url, int timeout) {
        this.context = c;
        this.url = url;
        this.timeout = timeout;
    }

    public RediVolley(Context c, String url, JSONObject reqParams) {
        this.context = c;
        this.url = url;
        this.jsonObjectReq = reqParams;
    }

    public RediVolley(Context c, String url, JSONObject reqParams, int timeout) {
        this.context = c;
        this.url = url;
        this.jsonObjectReq = reqParams;
        this.timeout = timeout;
    }



    public void setOnResponseListener(final RediView.OnResponseListener onResponseListener){
        if (Util.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) &&
                Util.hasPermission(context, Manifest.permission.INTERNET)){
            if (NetworkUtil.isNetworkConnected(context)){

                onResponseListener.onStart();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObjectReq, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        onResponseListener.onSuccess(response.toString());
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error.getMessage() != null)
                            onResponseListener.onFailure(error.getMessage());
                        else {
                            if (error instanceof NetworkError) {
                                onResponseListener.onFailure(context.getString(R.string.error_network));
                            } else if (error instanceof ServerError) {
                                onResponseListener.onFailure(context.getString(R.string.error_server));
                            } else if (error instanceof AuthFailureError) {
                                onResponseListener.onFailure(context.getString(R.string.error_auth));
                            } else if (error instanceof ParseError) {
                                onResponseListener.onFailure(context.getString(R.string.error_parse));
                            } else if (error instanceof TimeoutError) {
                                onResponseListener.onFailure(context.getString(R.string.error_timeout));
                            }
                        }
                    }
                });

                addToRequestQueue(jsonObjReq);
            } else {
                onResponseListener.onNetworkFailure();
            }
        } else {
            onResponseListener.onFailure("Missing ACCESS_NETWORK_STATE and INTERNET permissions in Manifest file");
        }
    }


    @SuppressLint("PrivateApi")
    private boolean hasVolleyClasspath() {
        try {
            Class.forName("com.android.volley");
            return true;
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = com.android.volley.toolbox.Volley.newRequestQueue(context.getApplicationContext());
        }

        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req, String tag) {
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);

        // Set API response time to 30s
        req.setRetryPolicy(new DefaultRetryPolicy(
                timeout,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        getRequestQueue().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }




}
