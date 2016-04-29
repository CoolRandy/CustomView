package com.yalantis.phoenix.sample.priority;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yalantis.phoenix.sample.R;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2016/4/22.
 *
 */
public class PriorityExecutorActivity extends Activity {

    private Button startThread;
    private Button startPause;
    private TextView textView;

    private PauseableThreadPoolExecutor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.priority_layout);

        startThread = (Button)findViewById(R.id.start_thread);
        startPause = (Button)findViewById(R.id.start_pause);
        textView = (TextView)findViewById(R.id.text_thread);

        executor = new PauseableThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new PriorityBlockingQueue<Runnable>());
        startThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i <= 10; i++){
                    final int priority = i;
                    executor.execute(new PriorityRunnable(priority) {
                        @Override
                        public void doSomething() {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    textView.setText(priority + "");
                                }
                            });

                            try {
                                Thread.sleep(1000);
                            }catch (InterruptedException e){
                                e.printStackTrace();
                            }
                        }
                    });
                }
                //线程池完成时关闭调用terminated，也就是在所有任务都已经完成并且所有工作者线程也已经关闭后，terminated可以用来释放Executor在其生命周期里分配的各种资源，此外还可以执行发送通知、记录日志或者手机finalize统计等操作
                executor.shutdown();
            }
        });

        startPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (executor.isPaused()){
                    executor.resume();
                }else {
                    executor.pause();
                }
            }
        });
    }
}
