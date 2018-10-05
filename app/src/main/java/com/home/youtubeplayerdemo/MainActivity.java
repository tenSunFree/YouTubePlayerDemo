package com.home.youtubeplayerdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerView;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

public class MainActivity extends YouTubeBaseActivity {

    private final String YOUTUBE_API_KEY = "填入申請的API金鑰";
    private final String YOUTUBE_VIDEO_CODE = "ME_YsfRN9lY";
    private int currentSeekBarProgress, currentDurationMillis, currentTimeMillis;
    private boolean onPlaying = false;

    private YouTubePlayerView youTubePlayerView;
    private LinearLayout youtubPlayLinearLayout;
    private ImageView youtubPlayImageView;
    private IndicatorSeekBar scheduleIndicatorSeekBar;
    private YouTubeAsyncTask youTubeAsyncTask;
    private TextView accumulateTextView, reciprocalTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accumulateTextView = findViewById(R.id.accumulateTextView);
        reciprocalTextView = findViewById(R.id.reciprocalTextView);

        initializationYouTubePlayer();
    }

    /** 初始化YouTubePlayer 相關設定 */
    private void initializationYouTubePlayer() {
        youTubePlayerView = findViewById(R.id.youTubePlayerView);
        youTubePlayerView.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override
            public void onInitializationSuccess(
                    YouTubePlayer.Provider provider, final YouTubePlayer youTubePlayer, boolean b) {
                if (!b) {
                    currentDurationMillis = youTubePlayer.getDurationMillis();
                    youTubePlayer.loadVideo(YOUTUBE_VIDEO_CODE);
                    initializationYoutubPlayLinearLayout(youTubePlayer);
                    initializationScheduleIndicatorSeekBar(youTubePlayer);
                    youTubeAsyncTask = new YouTubeAsyncTask();
                    youTubeAsyncTask.execute();
                    youTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                    youTubePlayer.setPlaybackEventListener(new YouTubePlayer.PlaybackEventListener() {
                        @Override
                        public void onPlaying() {
                            youtubPlayImageView.setImageResource(R.drawable.icon_youtub_pause);
                            currentDurationMillis = youTubePlayer.getDurationMillis();
                            currentTimeMillis = youTubePlayer.getCurrentTimeMillis();
                            onPlaying = true;
                        }

                        @Override
                        public void onPaused() {
                            youtubPlayImageView.setImageResource(R.drawable.icon_youtub_play);
                            onPlaying = false;
                        }

                        @Override
                        public void onStopped() {
                        }

                        @Override
                        public void onBuffering(boolean b) {
                        }

                        @Override
                        public void onSeekTo(int i) {
                        }
                    });
                }
            }

            @Override
            public void onInitializationFailure(
                    YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Toast.makeText(MainActivity.this,
                        youTubeInitializationResult.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializationScheduleIndicatorSeekBar(final YouTubePlayer youTubePlayer) {
        scheduleIndicatorSeekBar = findViewById(R.id.scheduleIndicatorSeekBar);
        scheduleIndicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                currentSeekBarProgress = seekParams.progress;
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                onPlaying = false;
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                youTubePlayer.seekToMillis(youTubePlayer.getDurationMillis() * currentSeekBarProgress / 100);
                if (currentSeekBarProgress == 100) {
                    onPlaying = false;
                    accumulateTextView.setText(formatTime(currentDurationMillis));
                    reciprocalTextView.setText("-00:00");
                }
            }
        });
    }

    /** 初始化 播放/暫停的按鈕 */
    private void initializationYoutubPlayLinearLayout(final YouTubePlayer youTubePlayer) {
        youtubPlayLinearLayout = findViewById(R.id.youtubPlayLinearLayout);
        youtubPlayImageView = findViewById(R.id.youtubPlayImageView);
        youtubPlayLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (youTubePlayer.isPlaying()) {
                    youTubePlayer.pause();
                    youtubPlayImageView.setImageResource(R.drawable.icon_youtub_play);
                } else {
                    youTubePlayer.play();
                    youtubPlayImageView.setImageResource(R.drawable.icon_youtub_pause);
                }
            }
        });
    }

    /** 將毫秒轉成分秒 */
    public static String formatTime(long ms) {

        int ss = 1000;
        int mi = ss * 60;
        int hh = mi * 60;
        int dd = hh * 24;

        long day = ms / dd;
        long hour = (ms - day * dd) / hh;
        long minute = (ms - day * dd - hour * hh) / mi;
        long second = (ms - day * dd - hour * hh - minute * mi) / ss;
        long milliSecond = ms - day * dd - hour * hh - minute * mi - second * ss;

        String strDay = day < 10 ? "0" + day : "" + day;                                            // 天
        String strHour = hour < 10 ? "0" + hour : "" + hour;                                        // 小时
        String strMinute = minute < 10 ? "0" + minute : "" + minute;                                // 分钟
        String strSecond = second < 10 ? "0" + second : "" + second;                                // 秒
        String strMilliSecond = milliSecond < 10 ? "0" + milliSecond : "" + milliSecond;            // 毫秒
        strMilliSecond = milliSecond < 100 ? "0" + strMilliSecond : "" + strMilliSecond;

        return strMinute + ":" + strSecond;
    }

    private class YouTubeAsyncTask extends AsyncTask<String, Integer, String> {

        /**
         * 这个方法会在后台任务开始执行之间调用, 在主线程执行, 用于进行一些界面上的初始化操作, 比如显示一个进度条对话框等
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * 更新UI
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        /**
         * 执行耗时操作
         */
        @Override
        protected String doInBackground(String... params) {
            while (true) {
                if (isCancelled()) {
                    break;
                }
                if (onPlaying) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            accumulateTextView.setText(formatTime(currentTimeMillis));
                            reciprocalTextView.setText("-" + formatTime(currentDurationMillis - currentTimeMillis));
                        }
                    });
                    currentSeekBarProgress = (int) (((float) currentTimeMillis / currentDurationMillis) * 100);
                    scheduleIndicatorSeekBar.setProgress(currentSeekBarProgress);

                    if (currentTimeMillis <= currentDurationMillis) {
                        currentTimeMillis = currentTimeMillis + 50;
                    }
                }
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return "finished";
        }

        /**
         * doInBackground结束后执行本方法, result是doInBackground方法返回的数据
         */
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        onPlaying = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        onPlaying = false;
        youTubeAsyncTask.cancel(true);
    }
}
