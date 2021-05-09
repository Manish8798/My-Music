package com.example.mytypemusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytypemusic.adapters.FavoriteSongsAdapter;
import com.example.mytypemusic.databinding.FragmentFavoritesBinding;
import com.example.mytypemusic.model.SongDetails;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavoritesFragment extends Fragment {

    private FragmentFavoritesBinding binding;
    FavoriteSongsAdapter favoriteSongsAdapter;
    DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        databaseReference = FirebaseDatabase.getInstance().getReference("Favorites/" + user.getUid());
        binding.recyclerViewFav.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseRecyclerOptions<SongDetails> options = new FirebaseRecyclerOptions.Builder<SongDetails>()
                .setQuery(databaseReference, SongDetails.class)
                .build();
        favoriteSongsAdapter = new FavoriteSongsAdapter(options);
        binding.recyclerViewFav.setAdapter(favoriteSongsAdapter);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        favoriteSongsAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        favoriteSongsAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
