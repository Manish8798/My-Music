package com.example.mytypemusic.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytypemusic.PlayerActivity;
import com.example.mytypemusic.databinding.HomeRowItemBinding;
import com.example.mytypemusic.model.SongDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class HomeAdapter extends FirebaseRecyclerAdapter<SongDetails, HomeAdapter.songViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public HomeAdapter(@NonNull FirebaseRecyclerOptions<SongDetails> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull songViewHolder holder, int position, @NonNull SongDetails model) {

        holder.binding.SongName.setText(model.getSongName());
        holder.binding.SongName.setOnClickListener(v -> {
            Intent moveIntent = new Intent(v.getContext(), PlayerActivity.class);
            moveIntent.putExtra("song_name", model.getSongName());
            moveIntent.putExtra("song_url", model.getSongUrl());
            moveIntent.putExtra("song_pos", position);
            v.getContext().startActivity(moveIntent);
        });

    }

    @NonNull
    @Override
    public songViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new songViewHolder(HomeRowItemBinding.inflate(layoutInflater, parent, false));
    }

    static class songViewHolder extends RecyclerView.ViewHolder {

        HomeRowItemBinding binding;

        public songViewHolder(@NonNull HomeRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
