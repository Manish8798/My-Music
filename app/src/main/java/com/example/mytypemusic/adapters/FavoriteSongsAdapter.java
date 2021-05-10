package com.example.mytypemusic.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytypemusic.PlayerActivity;
import com.example.mytypemusic.R;
import com.example.mytypemusic.databinding.HomeRowItemBinding;
import com.example.mytypemusic.model.SongDetails;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class FavoriteSongsAdapter extends FirebaseRecyclerAdapter<SongDetails, FavoriteSongsAdapter.FavoriteSongsViewHolder> {

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public FavoriteSongsAdapter(@NonNull FirebaseRecyclerOptions<SongDetails> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull FavoriteSongsAdapter.FavoriteSongsViewHolder holder, int position, @NonNull SongDetails model) {
        holder.binding.SongName.setText(model.getSongName());
        holder.binding.SongName.setOnClickListener(v -> {
            Intent moveIntent = new Intent(v.getContext(), PlayerActivity.class);
            moveIntent.putExtra("song_name", model.getSongName());
            moveIntent.putExtra("song_url", model.getSongUrl());
            moveIntent.putExtra("song_pos", position);
            v.getContext().startActivity(moveIntent);
        });
        holder.getCurrentSelectedSong(model.getSongName());
    }

    @NonNull
    @Override
    public FavoriteSongsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new FavoriteSongsViewHolder(HomeRowItemBinding.inflate(layoutInflater, parent, false));
    }

    public static class FavoriteSongsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private static final String TAG = FavoriteSongsAdapter.class.getSimpleName();
        HomeRowItemBinding binding;
        String currentSelectedSong;
        FirebaseUser user;

        public FavoriteSongsViewHolder(@NonNull HomeRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            user = FirebaseAuth.getInstance().getCurrentUser();
            this.binding.menuPopupRow.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            showPopMenu(v);
        }

        private void showPopMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.fav_popup_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_remove:
                    deleteFromFavDb(currentSelectedSong);
                    return true;

                default:
                    return false;
            }
        }

        private void deleteFromFavDb(String currentSong) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query songQuery = reference.child("Favorites").child(user.getUid()).orderByChild("songName").equalTo(currentSong);
            songQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        songSnapshot.getRef().removeValue();
                    }
                    Toast.makeText(binding.getRoot().getContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "favorite onCancelled: " + error.getMessage());
                }
            });
        }

        public void getCurrentSelectedSong(String songName) {
            currentSelectedSong = songName;
        }
    }
}
