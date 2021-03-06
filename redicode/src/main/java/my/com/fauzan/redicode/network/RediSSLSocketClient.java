package my.com.fauzan.redicode.network;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
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

import my.com.fauzan.redicode.BuildConfig;
import my.com.fauzan.redicode.RediView;

public class RediSSLSocketClient {

    private static final String TAG = SSLAsyncTask.class.getSimpleName();

    private String dstAddress;
    private int dstPort;

    private int timeout = 30000;
    private byte[] response;
    private boolean success;
    private SSLSocket socket = null;

    private Context context;
    private RediView.OnByteResponseListener onByteResponseListener;
    private String certFileName;
    private SSLAsyncTask sslAsyncTask;
    private static RediSSLSocketClient mInstance;
    private Boolean trustAllCerts = false;

    public static synchronized RediSSLSocketClient getInstance(Context context) {

        if (mInstance == null) {
            mInstance = new RediSSLSocketClient(context);
        }
        return mInstance;
    }

    private RediSSLSocketClient(Context context) {
        this.context = context;
    }

    public void initSSL(String dstAddress, int dstPort, String certFileName) {
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFileName = certFileName;
    }

    public void initSSL(String dstAddress, int dstPort, String certFileName, int timeout) {
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFileName = certFileName;
        this.timeout = timeout;
    }

    public void initSSL(String dstAddress, int dstPort, String certFileName, int timeout, Boolean trustAllCerts) {
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFileName = certFileName;
        this.timeout = timeout;
        this.trustAllCerts = trustAllCerts;
    }

    public void initSSL(String dstAddress, int dstPort, String certFileName, Boolean trustAllCerts) {
        this.dstAddress = dstAddress;
        this.dstPort = dstPort;
        this.certFileName = certFileName;
        this.timeout = timeout;
        this.trustAllCerts = trustAllCerts;
    }


    public static void setOnResponseListener(String request, RediView.OnByteResponseListener onByteResponseListener) {
        mInstance.onByteResponseListener = onByteResponseListener;
        mInstance.sslAsyncTask = (SSLAsyncTask) new SSLAsyncTask().execute(request);
    }

    public static void cancelRequest() {
        if (mInstance.sslAsyncTask != null && mInstance.sslAsyncTask.getStatus() != AsyncTask.Status.FINISHED)
            mInstance.sslAsyncTask.cancel(true);
    }

    private static class SSLAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (NetworkUtil.isNetworkConnected(mInstance.context)) {
                mInstance.onByteResponseListener.onStart();
            } else {
                mInstance.onByteResponseListener.onNetworkFailure();
                SSLAsyncTask.this.cancel(true);
            }
        }

        @Override
        protected String doInBackground(String... strings) {

            String req = strings[0];

            try {
                // SSLConnection CAs from an InputStream
                CertificateFactory cf;
                cf = CertificateFactory.getInstance("X.509");
                InputStream caInput = mInstance.context.getAssets().open(mInstance.certFileName);
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
                final SSLContext context = SslUtils.getSslContextForCertificateFile(mInstance.context, mInstance.certFileName);
                if (mInstance.trustAllCerts) {
                    context.init(null, SslUtils.trustAllCerts, null);
                } else {
                    context.init(null, tmf.getTrustManagers(), null);
                }
                try {

                    SSLSocketFactory sslsocketfactory = context.getSocketFactory();
                    mInstance.socket = (SSLSocket) sslsocketfactory.createSocket();
                    mInstance.socket.connect(new InetSocketAddress(mInstance.dstAddress, mInstance.dstPort), mInstance.timeout);
                    mInstance.socket.setSoTimeout(mInstance.timeout);

                    OutputStream out = mInstance.socket.getOutputStream();
                    out.write(ByteUtil.hexStr2Bytes(req));
                    out.flush();

                    byte[] buffer = new byte[2048];

                    int bytesRead, recvLen = 0;
                    InputStream inputStream = mInstance.socket.getInputStream();

                    /*
                     * notice: inputStream.read() will block if no data return
                     */

                    while ((bytesRead = inputStream.read(buffer, recvLen, buffer.length - recvLen)) != -1) {

                        recvLen += bytesRead;
                        if (recvLen > 2) {
                            int headerLen;
                            headerLen = ((int) buffer[0] * 256);
                            headerLen += (int) buffer[1];
                            if (headerLen + 2 <= recvLen) {
                                mInstance.response = new byte[recvLen];
                                System.arraycopy(buffer, 0, mInstance.response, 0, recvLen);
                                mInstance.success = true;
                                break;
                            }
                        }
                    }

                } catch (SocketTimeoutException se) {

                    String errorMsg = "Timeout error!";
                    mInstance.response = errorMsg.getBytes();
                    mInstance.success = false;

                    se.printStackTrace();

                    return null;

                } catch (IOException e) {
                    String connError = "Error!";
                    String errorMsg = e.getMessage();

                    if (errorMsg != null)
                        mInstance.response = errorMsg.getBytes();
                    else
                        mInstance.response = connError.getBytes();

                    mInstance.success = false;

                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Message reading error IO: " + e.getMessage());
                    }

                } catch (Exception e) {
                    String connError = "Connection Problem";
                    String errorMsg = e.getMessage();

                    if (errorMsg != null)
                        mInstance.response = errorMsg.getBytes();
                    else
                        mInstance.response = connError.getBytes();

                    mInstance.success = false;
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "Message reading error " + e.getMessage());
                    }
                } finally {
                    // close socket
                    if (mInstance.socket != null) {
                        try {
                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "Socket close");
                            }
                            mInstance.socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }

            } catch (CertificateException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            } catch (FileNotFoundException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            } catch (IOException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            } catch (KeyStoreException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            } catch (KeyManagementException e) {
                String errorMsg = e.getMessage();
                mInstance.response = errorMsg.getBytes();
                mInstance.success = false;

                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (mInstance.success)
                mInstance.onByteResponseListener.onSuccess(mInstance.response);
            else
                mInstance.onByteResponseListener.onFailure(mInstance.response);
        }
    }
}
