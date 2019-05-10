package my.com.fauzan.redicode;

public class View {

    public interface OnResponseListener{
        void onStart();
        void onComplete(String result);
        void onError(String error);
        void onNetworkError();
    }
}
