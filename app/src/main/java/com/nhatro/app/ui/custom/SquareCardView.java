package com.nhatro.app.ui.custom;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.card.MaterialCardView;

public class SquareCardView extends MaterialCardView {

    public SquareCardView(@NonNull Context context) {
        super(context);
    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareCardView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Enforce 1:1 aspect ratio by using widthMeasureSpec for both width and height
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
