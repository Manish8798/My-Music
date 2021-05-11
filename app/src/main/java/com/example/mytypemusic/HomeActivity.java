package com.example.mytypemusic;

import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mytypemusic.databinding.ActivityHomeBinding;
import com.example.mytypemusic.fragments.FavoritesFragment;
import com.example.mytypemusic.fragments.HomeFragment;
import com.example.mytypemusic.model.SongDetails;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private static final int RC_UPLOAD = 1;
    private static final String TAG = "HomeActivity";
    private ActivityHomeBinding binding;
    private boolean checkPermission = false;
    private Uri uri;
    private String songName, songUrl;
    private FirebaseUser user;
    final Fragment homeFragment = new HomeFragment();
    final Fragment favFragment = new FavoritesFragment();
    final FragmentManager fm = getSupportFragmentManager();
    Fragment activeFragment = homeFragment;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        fm.beginTransaction().add(R.id.home_container, favFragment, "2").hide(favFragment).commit();
        fm.beginTransaction().add(R.id.home_container, homeFragment, "1").commit();
        Objects.requireNonNull(getSupportActionBar()).setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.toolbar_color)));
        binding.bottomNav.setBackground(null);
        binding.bottomNav.getMenu().getItem(1).setEnabled(false);
        binding.progressSeekBar.setVisibility(View.VISIBLE);
        handler = new Handler();

        binding.fab.setOnClickListener(v -> {
            if (validatePermission()) {
                uploadSong();
            }
        });
        binding.bottomNav.setOnNavigationItemSelectedListener(navListener);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (signInAccount != null) {
            Toast.makeText(this, "Welcome " + signInAccount.getDisplayName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        user = FirebaseAuth.getInstance().getCurrentUser();
        validatePermission();
    }

    private void uploadSong() {
        Intent intentUpload = new Intent();
        intentUpload.setType("audio/*");
        intentUpload.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intentUpload, RC_UPLOAD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == RC_UPLOAD && resultCode == RESULT_OK) {
            assert data != null;
            uri = data.getData();
            Cursor cursor = getApplicationContext().getContentResolver().query(uri, null, null, null, null);
            int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            cursor.moveToFirst();
            songName = cursor.getString(index);
            cursor.close();
            uploadToFirebaseStorage();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadToFirebaseStorage() {

        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                .child("Songs")
                .child(user.getUid())
                .child(uri.getLastPathSegment());

//        binding.progressSeekBar.setVisibility(View.VISIBLE);

        storageReference.putFile(uri).addOnSuccessListener(taskSnapshot -> {

            Handler handler = new Handler();
            handler.postDelayed(() -> binding.progressSeekBar.setProgress(0), 1000);
            Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
            while (!uriTask.isComplete());
            Uri urlSong = uriTask.getResult();
            assert urlSong != null;
            songUrl = urlSong.toString();
            uploadDetailsToFirebase();

        }).addOnFailureListener(e -> {
            binding.progressSeekBar.setProgress(0);
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "uploadToFirebaseStorage: " + e.getMessage());
//            binding.progressSeekBar.setVisibility(View.GONE);

        }).addOnProgressListener(snapshot -> {
            double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
            int currentProgress = (int) progress;
            binding.progressSeekBar.post(() -> binding.progressSeekBar.setProgress(currentProgress));
        });

    }

    private void uploadDetailsToFirebase() {

        SongDetails songDetails = new SongDetails(songName, songUrl);
        FirebaseDatabase.getInstance().getReference("Songs/" + user.getUid()).push().setValue(songDetails)
                .addOnCompleteListener(task -> Toast.makeText(HomeActivity.this, "Song Uploaded", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(HomeActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show());

    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener = item -> {
        switch (item.getItemId()) {
            case R.id.home:
                handler.post(() -> {
                    fm.beginTransaction().hide(activeFragment).show(homeFragment).commit();
                    activeFragment = homeFragment;
                });
                return true;

            case R.id.favorite:
                handler.post(() -> {
                    fm.beginTransaction().hide(activeFragment).show(favFragment).commit();
                    activeFragment = favFragment;
                });
                return true;
        }
        return false;
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Sign out", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.user_profile:
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivity(profileIntent);
                return true;

            default:
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean validatePermission() {
        Dexter.withContext(HomeActivity.this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        checkPermission = true;
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        checkPermission = false;
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                }).check();

        return checkPermission;
    }
}