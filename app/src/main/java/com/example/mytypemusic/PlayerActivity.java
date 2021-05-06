package com.example.mytypemusic;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mytypemusic.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {

    private String songName;
    private String songUrl;
    private int songPos;
    private ActivityPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        songName = getIntent().getStringExtra("song_name");
        songUrl = getIntent().getStringExtra("song_url");
        songPos = getIntent().getIntExtra("song_pos", 0);
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