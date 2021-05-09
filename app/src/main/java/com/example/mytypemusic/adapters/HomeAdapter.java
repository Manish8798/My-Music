package com.example.mytypemusic.adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

        holder.getCurrentSong(model.getSongName(), model.getSongUrl());

    }

    @NonNull
    @Override
    public songViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        return new songViewHolder(HomeRowItemBinding.inflate(layoutInflater, parent, false));
    }

    static class songViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {

        private static final String TAG = "HomeAdapter";
        HomeRowItemBinding binding;
        FirebaseUser user;
        String currentSong;
        String currentSongUrl;
        String currentDeletedSong = "";

        public songViewHolder(@NonNull HomeRowItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.menuPopupRow.setOnClickListener(this);
            user = FirebaseAuth.getInstance().getCurrentUser();
        }

        @Override
        public void onClick(View v) {
            showPopupMenu(v);
        }

        private void showPopupMenu(View view) {
            PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
            popupMenu.inflate(R.menu.popup_menu);
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_delete:
                    deleteFromDatabase(currentSong);
                    deleteFromStorage(currentSong);
                    deleteFromLikedDb(currentSong);
                    return true;

                case R.id.action_fav:
                    uploadLikedSong(currentSong, currentSongUrl);
                    return true;

                default:
                    return false;
            }
        }

        private void deleteFromLikedDb(String currentSong) {
            Log.d(TAG, "deleteFromLikedDb: " + currentSongUrl);
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query songQuery = reference.child("Favorites").child(user.getUid()).orderByChild("songName").equalTo(currentSong);
            songQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        songSnapshot.getRef().removeValue();
                    }
                    Toast.makeText(binding.getRoot().getContext(), "Song will be removed from favorites too", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "favorite onCancelled: " + error.getMessage());
                }
            });
        }

        private void uploadLikedSong(String currentSong, String currentSongUrl) {
            SongDetails likedSongDetails = new SongDetails(currentSong, currentSongUrl);
            FirebaseDatabase.getInstance().getReference("Favorites/" + user.getUid()).push().setValue(likedSongDetails)
                    .addOnCompleteListener(task -> Toast.makeText(binding.getRoot().getContext(), "Uploaded to favorites", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(binding.getRoot().getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        private void deleteFromStorage(String currentSong) {

            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                    .child("Songs")
                    .child(user.getUid())
                    .child("raw:/storage/emulated/0/Download")
                    .child(currentSong);

            storageReference.delete()
                    .addOnSuccessListener(unused -> Log.d(TAG, "delete from storage onSuccess: " + currentSong))
                    .addOnFailureListener(e -> Log.d(TAG, "Storage delete onFailure: " + e.getMessage()));

        }

        private void deleteFromDatabase(String currentSong) {
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query songQuery = reference.child("Songs").child(user.getUid()).orderByChild("songName").equalTo(currentSong);
            songQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot songSnapshot : snapshot.getChildren()) {
                        songSnapshot.getRef().removeValue();
                    }
                    Toast.makeText(binding.getRoot().getContext(), "Song deleted", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.d(TAG, "onCancelled: " + error.getMessage());
                }
            });
        }

        public void getCurrentSong(String songName, String songUrl) {
            currentSong = songName;
            currentSongUrl = songUrl;
        }
    }

}
