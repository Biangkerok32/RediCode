package my.com.fauzan.redicode.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.Nullable;

import com.google.android.material.textfield.TextInputLayout;

import my.com.fauzan.redicode.R;

public class RediTextInputLayout extends TextInputLayout {

    RediTextInputEditText rediTextInputEditText;
    private int mTextSize = 16;
    private int mTextColor = getContext().getResources().getColor(android.R.color.black);
    private String mText = "";

    public RediTextInputLayout(Context context) {
        super(context);
        init(context,null);
    }

    public RediTextInputLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs){

        if (attrs != null){
            final TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.RediTextInputLayout);
            try {
                // Attribute initialization
                mText = typedArray.getString(R.styleable.RediTextInputLayout_android_text);
                mTextSize = (int) typedArray.getDimension(R.styleable.RediTextInputLayout_android_textSize, mTextSize);
                mTextColor = typedArray.getColor(R.styleable.RediTextInputLayout_android_textColor, mTextColor);

                rediTextInputEditText = new RediTextInputEditText(getContext());
                rediTextInputEditText.setTextSize(mTextSize);
                rediTextInputEditText.setTextColor(mTextColor);
                // rediTextInputEditText.setTypeface(2323);

                LayoutParams params = new LayoutParams(
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                );
                params.setMargins(16, 0, 16, 80);
                setLayoutParams(params);
                setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
                setBoxCornerRadii(5, 5, 5, 5);
                addView(rediTextInputEditText);
            } finally {
                typedArray.recycle();
            }

        }
    }
}
