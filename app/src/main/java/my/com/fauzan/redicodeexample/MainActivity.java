package my.com.fauzan.redicodeexample;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import my.com.fauzan.redicode.NetworkUtil;
import my.com.fauzan.redicode.SSLSocketClient;
import my.com.fauzan.redicode.Volley;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Restful API - default timeout 30000
        Volley volley = new Volley(this, "https://reqres.in/api/products/3");

        // Restful API - with request object
        //  Volley volley = new Volley(this, "https://reqres.in/api/products/3", reqObject);


        // Restful API - custom timeout
        // Volley volley = new Volley(this, "https://reqres.in/api/products/3", reqObject, 45000 );

        volley.execute(new Volley.OnExecute() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(String result) {
                Log.d(TAG, "onComplete: "+result);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: " + error );

            }

            @Override
            public void onNetworkError() {
                Log.e(TAG, "onNetworkError: " + getString(R.string.error_network));
            }
        });

        // Stop volley request
        volley.cancelPendingRequests(this);

        // SSL Socket with default timeout - 30000;
        // Cert file must be in Raw Folder
        SSLSocketClient sslSocketClient = new SSLSocketClient(this,"100.120.280.123",1111, 1312);
        SSLSocketClient.SSLOperation sslOperation = sslSocketClient.execute("", new SSLSocketClient.OnExecute() {
            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(byte[] response) {
                Log.e(TAG, "onComplete: "+ new String(response));

            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "onError: "+error );
            }

            @Override
            public void onNetworkError() {
                Log.e(TAG, "onNetworkError: " + getString(R.string.error_network));
            }
        });

        // Stop SSL Request
        if (sslOperation !=null && sslOperation.getStatus() != AsyncTask.Status.FINISHED)
            sslOperation.cancel(true);
    }
}
