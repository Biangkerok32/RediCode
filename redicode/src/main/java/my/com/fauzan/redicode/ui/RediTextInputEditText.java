package my.com.fauzan.redicode.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputEditText;

import my.com.fauzan.redicode.R;

public class RediTextInputEditText extends TextInputEditText {


    public RediTextInputEditText(Context context) {
        super(context);
    }

    public RediTextInputEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RediTextInputEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int valueInPixels = getResources().getDimensionPixelOffset(R.dimen.edit_text_padding);
        setPadding(valueInPixels, valueInPixels, valueInPixels, valueInPixels);

    }
}
