package com.yalantis.phoenix.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


/**
 * Created by admin on 2016/4/20.
 *
 */
public class TestActivity extends Activity {

    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_main);
        button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                button.offsetTopAndBottom(-10);//正值，上移；负值，下移
                button.offsetLeftAndRight(10);//正值，右移；负值，左移
            }
        });
    }
}
