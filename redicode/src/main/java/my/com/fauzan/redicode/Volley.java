package my.com.fauzan.redicode;

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

public class Volley {

    private Context context;
    private RequestQueue mRequestQueue;
    private String url;
    private JSONObject jsonObjectReq;
    private int timeout = 30000;

    public Volley(Context c, String url) {
        this.context = c;
        this.url = url;
    }

    public Volley(Context c, String url, int timeout) {
        this.context = c;
        this.url = url;
        this.timeout = timeout;
    }

    public Volley(Context c, String url, JSONObject reqParams) {
        this.context = c;
        this.url = url;
        this.jsonObjectReq = reqParams;
    }

    public Volley(Context c, String url, JSONObject reqParams, int timeout) {
        this.context = c;
        this.url = url;
        this.jsonObjectReq = reqParams;
        this.timeout = timeout;
    }



    public void execute(final OnExecute onExecute){
        if (Util.hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) &&
                Util.hasPermission(context, Manifest.permission.INTERNET)){
            if (NetworkUtil.isNetworkConnected(context)){

                onExecute.onStart();

                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST,
                        url, jsonObjectReq, new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        onExecute.onComplete(response.toString());
                    }
                }, new com.android.volley.Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (error.getMessage() != null)
                            onExecute.onError(error.getMessage());
                        else {
                            if (error instanceof NetworkError) {
                                onExecute.onError(context.getString(R.string.error_network));
                            } else if (error instanceof ServerError) {
                                onExecute.onError(context.getString(R.string.error_server));
                            } else if (error instanceof AuthFailureError) {
                                onExecute.onError(context.getString(R.string.error_auth));
                            } else if (error instanceof ParseError) {
                                onExecute.onError(context.getString(R.string.error_parse));
                            } else if (error instanceof TimeoutError) {
                                onExecute.onError(context.getString(R.string.error_timeout));
                            }
                        }
                    }
                });

                addToRequestQueue(jsonObjReq);
            } else {
                onExecute.onNetworkError();
            }
        } else {
            onExecute.onError("Missing ACCESS_NETWORK_STATE and INTERNET permissions in Manifest file");
        }
    }

    public interface OnExecute{
        void onStart();
        void onComplete(String result);
        void onError(String error);
        void onNetworkError();
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
        req.setTag(TextUtils.isEmpty(tag) ? context.getClass().getSimpleName() : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(context.getClass().getSimpleName());

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
