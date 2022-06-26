package com.example.campsitesearcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

public class RequestHandler {
    private Handler handler;
    private ExecutorService executorService;

    public void stopAllRequests() {
        executorService.shutdown();
        handler.removeCallbacksAndMessages(null);
    }

    private class Request implements Runnable
    {
        private Runnable r;
        private long delay;
        private boolean isForever;
        public Request(Runnable r, long delay, boolean isForever)
        {
            this.r = r;
            this.delay = delay;
            this.isForever = isForever;
        }

        @Override
        public void run() {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    r.run();
                    if (isForever)
                    {
                        handler.postDelayed(Request.this, delay);
                    }
                }
            });
        }
    }

    public RequestHandler()
    {
        handler = new Handler();
        executorService = Executors.newSingleThreadExecutor();
    }

    public void handleRequest(long delay, boolean goesForever, Runnable task)
    {
        Request request = new Request(task, delay, goesForever);
        handler.post(request);
    }
}
