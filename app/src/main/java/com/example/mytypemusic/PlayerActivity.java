package com.example.mytypemusic;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytypemusic.databinding.ActivityPlayerBinding;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    private static final String TAG = "PlayerActivity";
    private String songName;
    private String songUrl;
    private int songPos;
    private ActivityPlayerBinding binding;
    private MediaPlayer mediaPlayer;
    private boolean playingAudio = false;
    Runnable runnable;
    Handler handler;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (getIntent() != null) {
            songName = getIntent().getStringExtra("song_name");
            songUrl = getIntent().getStringExtra("song_url");
            songPos = getIntent().getIntExtra("song_pos", 0);
        }
        binding.songName.setText(songName);
        Log.d(TAG, "onCreate: " + songUrl);
        handler = new Handler();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        try {
            mediaPlayer.setDataSource(songUrl);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnPreparedListener(mp -> {
            binding.playBtn.setBackground(getDrawable(R.drawable.ic_pause));
            binding.seekbarPlayer.setMax(mp.getDuration());
            binding.seekBarEndTime.setText(milliSecToTimer(mp.getDuration()));
            mediaPlayer.start();
            playingAudio = true;
            updateSeekbar();
        });

        mediaPlayer.setOnBufferingUpdateListener((mp, percent) -> {
            double ratio = percent / 100.0;
            int bufferingLevel = (int) (mp.getDuration() * ratio);
            binding.seekbarPlayer.setSecondaryProgress(bufferingLevel);
        });

        binding.playBtn.setOnClickListener(v -> {

            if (playingAudio) {
                mediaPlayer.pause();
                playingAudio = false;
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_play_button));
            } else {
                mediaPlayer.start();
                playingAudio = true;
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_pause));
            }
            if (!playingAudio && mediaPlayer.getCurrentPosition() == mediaPlayer.getDuration()) {
                playingAudio = true;
                mediaPlayer.start();
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_pause));
            }

        });

        binding.seekbarPlayer.setOnSeekBarChangeListener(seekBarChangeListener);

    }

    private SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaPlayer.seekTo(progress);
                seekBar.setProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    private void updateSeekbar() {
        try {
            if (mediaPlayer != null) {
                long currentTime = mediaPlayer.getCurrentPosition();
                int currentPos = (int) currentTime;
                binding.seekBarStartTime.setText(milliSecToTimer(currentTime));
                binding.seekbarPlayer.setProgress(currentPos);
                runnable = this::updateSeekbar;
                handler.postDelayed(runnable, 500);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "updateSeekbar: " + e.getMessage());
        }

    }

    private String milliSecToTimer(long milliseconds) {
        String timerString = "";
        String secondString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            timerString = hours + ":";
        }
        if (seconds < 10) {
            secondString = "0" + seconds;
        } else {
            secondString = "" + seconds;
        }

        timerString = timerString + minutes + ":" + secondString;
        return timerString;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        clearMediaPlayer();
        return true;
    }

    private void clearMediaPlayer() {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    /*
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            songName = intent.getStringExtra("song_name");
            songUrl = intent.getStringExtra("song_url");
        }
    }; */

}