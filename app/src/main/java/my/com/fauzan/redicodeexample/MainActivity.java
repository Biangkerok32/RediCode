package my.com.fauzan.redicodeexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import my.com.fauzan.redicode.RediLocation;
import my.com.fauzan.redicode.RediVolley;
import my.com.fauzan.redicode.RediSSLSocketClient;
import my.com.fauzan.redicode.RediView;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        location();
    }

    private void volley(){
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

    }

    private void ssl(){
        // SSL Socket with default timeout - 30000;
        // Cert file must be in Raw Folder
        RediSSLSocketClient.getInstance(this).initSSL(BuildConfig.Address, BuildConfig.Port, R.raw.certificate);
        RediSSLSocketClient.setOnResponseListener(BuildConfig.Request, new RediView.OnByteResponseListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(byte[] result) {
                Log.e(TAG, "onSuccess: " + new String(result) );
            }

            @Override
            public void onFailure(byte[] error) {
                Log.e(TAG, "onFailure: " + new String(error) );
            }

            @Override
            public void onNetworkFailure() {
                Log.e(TAG, "onNetworkFailure: ");
            }
        });

        // Stop SSL Request
//        if (sslTest !=null && sslTest.getStatus() != AsyncTask.Status.FINISHED)
//            sslTest.cancel(true);
    }

    private void location (){
        final RediLocation rediLocation = new RediLocation(this, 1);
        rediLocation.startLocation(new RediView.OnLocationListener() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(double latitude, double longitude) {
                Log.e(TAG, "onSuccess: "+latitude);
                Log.e(TAG, "onSuccess: "+longitude);

                rediLocation.stopLocationUpdates();

            }


            @Override
            public void onFailure(String error) {

            }

            @Override
            public void onPermissionFailure() {
                Log.e(TAG, "onNoUserPermission: ");
            }
        });

    }
}
