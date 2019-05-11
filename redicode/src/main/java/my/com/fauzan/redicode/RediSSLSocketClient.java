package my.com.fauzan.redicode;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class RediSSLSocketClient {

    private final String TAG = SSLAsyncTask.class.getSimpleName();

    private String dstAddress;
    private int dstPort;

    private int timeout = 30000;
    private byte[] response;
    private boolean success;
    private SSLSocket socket = null;

    private Context context;
    private RediView.OnResponseListener onResponseListener;
    private int certFile;

    public RediSSLSocketClient(Context context, String dstAddress, int dstPort, int certFile) {
        this.context = context;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFile = certFile;
    }

    public RediSSLSocketClient(Context context, String dstAddress, int dstPort, int certFile, int timeout) {
        this.context = context;
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFile = certFile;
        this.timeout = timeout;

    }


    public SSLAsyncTask setOnResponseListener(String request, RediView.OnResponseListener onResponseListener){
        this.onResponseListener = onResponseListener;
        if (NetworkUtil.isNetworkConnected(context))
            return (SSLAsyncTask) new SSLAsyncTask().execute(request);
        else
            this.onResponseListener.onNetworkError();

        return null;
    }

    ////////////////////////
    ////// SSL Socket //////
    ////////////////////////
    public class SSLAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {

            String req = strings[0];
            try {
                // SSLConnection CAs from an InputStream
                CertificateFactory cf;
                cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = context.getResources().openRawResource(certFile);
                Certificate ca;
                try {
                    ca = cf.generateCertificate(caInput);

                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "ca = " + ((X509Certificate) ca).getSubjectDN());
                    }
                } finally {
                    caInput.close();
                }

                // Create a KeyStore containing our trusted CAs
                String keyStoreType = KeyStore.getDefaultType();
                KeyStore keyStore = KeyStore.getInstance(keyStoreType);
                keyStore.load(null, null);
                keyStore.setCertificateEntry("ca", ca);

                // Create a TrustManager that trusts the CAs in our KeyStore
                String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                tmf.init(keyStore);

                // Create an SSLContext that uses our TrustManager
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, tmf.getTrustManagers(), null);

                try {

                    SSLSocketFactory sslsocketfactory = context.getSocketFactory();
                    socket = (SSLSocket) sslsocketfactory.createSocket();
                    socket.connect(new InetSocketAddress(dstAddress, dstPort), timeout);
                    socket.setSoTimeout(timeout);

                    OutputStream out = socket.getOutputStream();
                    out.write(ByteUtil.hexStr2Bytes(req));
                    out.flush();

                    byte[] buffer = new byte[2048];

                    int bytesRead, recvLen = 0;
                    InputStream inputStream = socket.getInputStream();

                    /*
                     * notice: inputStream.read() will block if no data return
                     */

                    while ((bytesRead = inputStream.read(buffer, recvLen, buffer.length - recvLen)) != -1) {

                        recvLen += bytesRead;
                        if (recvLen > 2) {
                            int headerLen;
                            headerLen = ((int)buffer[0]  * 256);
                            headerLen += (int)buffer[1];
                            if (headerLen + 2 <= recvLen) {
                                response = new byte[recvLen];
                                System.arraycopy (buffer, 0, response, 0, recvLen);
                                success = true;
                                break;
                            }
                        }
                    }

                } catch(SocketTimeoutException se) {

                    String errorMsg = "Timeout error!";
                    response = errorMsg.getBytes();
                    success = false;

                    se.printStackTrace();

                    return null;

                } catch (IOException e) {
                    String connError = "Error!";
                    String errorMsg = e.getMessage();

                    if(errorMsg != null)
                        response = errorMsg.getBytes();
                    else
                        response = connError.getBytes();

                    success = false;

                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Message reading error IO: " + e.getMessage());
                    }

                } catch (Exception e) {
                    String connError = "Connection Problem";
                    String errorMsg = e.getMessage();

                    if(errorMsg != null)
                        response = errorMsg.getBytes();
                    else
                        response = connError.getBytes();

                    success = false;
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Message reading error " + e.getMessage());
                    }
                } finally {
                    // close socket
                    if (socket != null) {
                        try {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "Socket close");
                            }
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            } catch (CertificateException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            } catch (FileNotFoundException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            } catch (IOException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            } catch (KeyStoreException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            } catch (KeyManagementException e) {
                String errorMsg = e.getMessage();
                response = errorMsg.getBytes();
                success = false;

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            onResponseListener.onStart();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (success) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    onResponseListener.onComplete(new String(response, StandardCharsets.UTF_8));
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    onResponseListener.onError(new String(response, StandardCharsets.UTF_8));
                }
            }
        }
    }
}
