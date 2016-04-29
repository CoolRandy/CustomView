package com.yalantis.phoenix.sample.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.yalantis.phoenix.sample.R;

/**
 * Created by admin on 2016/4/29.
 *
 */
public class CircleDotLoadingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.circle_dot_load_layout);
        final CircleDotLoadingView circleDotLoadingView = (CircleDotLoadingView)findViewById(R.id.circle_dot_loading);
//        circleDotLoadingView.executeTask();
        Button start = (Button)findViewById(R.id.start_animation);
        Button stop = (Button)findViewById(R.id.stop_animation);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleDotLoadingView.executeTask();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                circleDotLoadingView.stopTask();
            }
        });

    }
}
