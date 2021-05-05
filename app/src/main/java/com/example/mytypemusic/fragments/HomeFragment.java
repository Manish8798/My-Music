package com.example.mytypemusic.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mytypemusic.adapters.HomeAdapter;
import com.example.mytypemusic.databinding.FragmentHomeBinding;
import com.example.mytypemusic.model.SongDetails;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    HomeAdapter homeAdapter;
    DatabaseReference databaseReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        databaseReference = FirebaseDatabase.getInstance().getReference("Songs");
        binding.recyclerViewHome.setLayoutManager(new LinearLayoutManager(getContext()));
        FirebaseRecyclerOptions<SongDetails> options = new FirebaseRecyclerOptions.Builder<SongDetails>()
                .setQuery(databaseReference, SongDetails.class)
                .build();

        homeAdapter = new HomeAdapter(options);
        binding.recyclerViewHome.setAdapter(homeAdapter);

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        homeAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        homeAdapter.stopListening();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
