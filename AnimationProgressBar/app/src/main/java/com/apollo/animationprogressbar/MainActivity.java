package com.apollo.animationprogressbar;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.apollo.animationprogressba.AnimationProgressBar;

public class MainActivity extends Activity {

    private AnimationProgressBar bar;
    private Button btn_start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        bar = (AnimationProgressBar) findViewById(R.id.pb_frame);
//        bar.setDrawableIds(new int[]{R.drawable.i00,
//                R.drawable.i01,
//                R.drawable.i02,
//                R.drawable.i03,
//                R.drawable.i04,
//                R.drawable.i05,
//                R.drawable.i06});
//        btn_start = (Button) findViewById(R.id.btn_start);
//
//
//        //使用属性动画来实现进度变化
//        final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 10000);
//        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator valueAnimator) {
//                bar.setProgress(Integer.parseInt(valueAnimator.getAnimatedValue().toString()));
//                if (bar.getProgress() >= bar.getMax()) {//进度最大后停止
//                    bar.setAnimRun(false);
//                }
//            }
//        });
//        valueAnimator.setDuration(60000);
//        btn_start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                bar.setAnimRun(true);
//                valueAnimator.start();
//            }
//        });
//
    }
}
