package com.example.mytypemusic;

import android.annotation.SuppressLint;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytypemusic.databinding.ActivityPlayerBinding;

import java.io.IOException;

public class PlayerActivity extends AppCompatActivity {

    private String songName;
    private String songUrl;
    private int songPos;
    private ActivityPlayerBinding binding;
    private MediaPlayer mediaPlayer;
    private boolean checkState = false;

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
        Handler handler = new Handler();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(
                new AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
        );

        handler.post(() -> {
            try {
                mediaPlayer.setDataSource(songUrl);
                mediaPlayer.prepare();
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_pause));
            } catch (IOException e) {
                e.printStackTrace();
            }
            mediaPlayer.start();
        });

        binding.playBtn.setOnClickListener(v -> {

            if (!checkState && mediaPlayer.isPlaying()) {
                checkState = true;
                mediaPlayer.pause();
                binding.playBtn.setBackground(null);
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_play_button));
            } else {
                checkState = false;
                mediaPlayer.start();
                binding.playBtn.setBackground(null);
                binding.playBtn.setBackground(getDrawable(R.drawable.ic_pause));
            }

        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null)
            mediaPlayer.release();
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