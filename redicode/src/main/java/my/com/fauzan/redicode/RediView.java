package my.com.fauzan.redicode;

public class RediView {

    public interface OnResponseListener{
        void onStart();
        void onComplete(String result);
        void onError(String error);
        void onNetworkError();
    }

    public interface OnByteResponseListener{
        void onStart();
        void onComplete(byte[] result);
        void onError(byte[] error);
        void onNetworkError();
    }
}
