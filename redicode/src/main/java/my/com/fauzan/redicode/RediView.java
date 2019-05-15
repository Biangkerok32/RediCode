package my.com.fauzan.redicode;

public class RediView {

    public interface OnResponseListener{
        void onStart();
        void onSuccess(String result);
        void onFailure(String error);
        void onNetworkFailure();
    }

    public interface OnByteResponseListener{
        void onStart();
        void onSuccess(byte[] result);
        void onFailure(byte[] error);
        void onNetworkFailure();
    }
}
