package my.com.fauzan.redicodeexample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import my.com.fauzan.redicode.RediVolley;
import my.com.fauzan.redicode.RediSSLSocketClient;
import my.com.fauzan.redicode.RediView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restful API - default timeout 30000
        RediVolley rediVolley = new RediVolley(this, "https://reqres.in/api/products/3");

        // Restful API - with request object
        //  RediVolley rediVolley = new RediVolley(this, "https://reqres.in/api/products/3", reqObject);


        // Restful API - custom timeout
        // RediVolley rediVolley = new RediVolley(this, "https://reqres.in/api/products/3", reqObject, 45000 );

        rediVolley.setOnResponseListener(new RediView.OnResponseListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(String result) {
                Log.d(TAG, "onSuccess: "+result);
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "onFailure: " + error );
            }

            @Override
            public void onNetworkFailure() {
                Log.e(TAG, "onNetworkFailure: " + getString(R.string.error_network));
            }
        });


        // Stop rediVolley request
        rediVolley.cancelPendingRequests(this);

        // SSL Socket with default timeout - 30000;
        // Cert file must be in Raw Folder
        RediSSLSocketClient rediSslSocketClient = new RediSSLSocketClient(this,"100.120.280.123",1111, 1312);
        RediSSLSocketClient.SSLAsyncTask sslAsyncTask = rediSslSocketClient.setOnResponseListener("12312319081290389120832190830", new RediView.OnByteResponseListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(byte[] result) {

            }

            @Override
            public void onFailure(byte[] error) {

            }

            @Override
            public void onNetworkFailure() {

            }
    });

        // Stop SSL Request
        if (sslAsyncTask !=null && sslAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
            sslAsyncTask.cancel(true);
    }
}
