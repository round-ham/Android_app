package vn.edu.fpt.musicplayer;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import vn.edu.fpt.musicplayer.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements SongAdapter.OnItemClickerListerner {
    private ActivityMainBinding binding;
    private RecyclerView.Adapter adapter;
    private List<Song> songList;

    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
        if(isGranted){
            loadSong();
        } else {
            Toast.makeText(this, "Permission denied to read storage", Toast.LENGTH_SHORT).show();
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        EdgeToEdgeHelper.enable(this);

        binding.recyclerViewSongs.setLayoutManager(new LinearLayoutManager(this));


        checkPermissonAndLoadSongs();
    }

    private void checkPermissonAndLoadSongs() {
        String permission;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            permission= Manifest.permission.READ_MEDIA_AUDIO;
        } else {
            permission= Manifest.permission.READ_EXTERNAL_STORAGE;
        }
        if(ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED){
            loadSong();

        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private List<Song> getSongs(){
        List<Song> song = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        try(Cursor cursor =getContentResolver().query(uri,null, selection,null, sortOrder)){
            if(cursor!=null){
                int idColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                int dataColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                int albumColumn=cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);

                while (cursor.moveToNext()){
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    String data = cursor.getString(dataColumn);
                    long albumID = cursor.getLong(albumColumn);

                    song.add(new Song(id, title, artist, data, albumID));
                }
            }
        }
        return song;
    }
    private void loadSong(){
        songList = getSongs();

        if (songList.isEmpty()) {
            Toast.makeText(this, "Cannot find anysong in the device!", Toast.LENGTH_SHORT).show();
        }

        adapter = new SongAdapter(songList, this);
        binding.recyclerViewSongs.setAdapter(adapter);

    }



    @Override
    public void OnClick(int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putParcelableArrayListExtra("songList", new ArrayList<>(songList));
        intent.putExtra("position", position);
        startActivity(intent);
    }
}