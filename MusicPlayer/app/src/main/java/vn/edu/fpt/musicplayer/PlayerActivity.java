package vn.edu.fpt.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.frolo.waveformseekbar.WaveformSeekBar;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;
import jp.wasabeef.glide.transformations.BlurTransformation;
import vn.edu.fpt.musicplayer.databinding.ActivityPlayerBinding;

public class PlayerActivity extends AppCompatActivity {
    private ActivityPlayerBinding binding;
    private ExoPlayer player;
    private Handler handler = new Handler();
    private List<Song> songList = new ArrayList<>();
    private List<Song> shuffledList = new ArrayList<>();
    private int currentIndex = 0;
    private boolean isShuffle = false;
    private boolean isRepeat = false;


    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            if (player!=null && player.isPlaying()){
                long currentPosition = player.getCurrentPosition();
                long duration=player.getDuration();
                if(duration>0){
                    float progressPercent=((float) currentPosition/duration);
                    binding.waveformSeekBar.setProgressInPercentage(progressPercent);
                    binding.textElapsed.setText(formatTime((int) (currentPosition / 1000)));
                    binding.textDuration.setText(formatTime((int) (duration / 1000)));
                }

                handler.postDelayed(this, 1000);
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding =ActivityPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        EdgeToEdgeHelper.enable(this);

        songList = getIntent().getParcelableArrayListExtra("songList");
        currentIndex = getIntent().getIntExtra("position", 0);

        if (songList==null || songList.isEmpty()){
            Toast.makeText(this, "no Song Found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        shuffledList=new ArrayList<>(songList);
        binding.waveformSeekBar.setWaveform(createWaveForm(), true);

        initPlayerWithSong(currentIndex);
        setupControls();

        binding.backBtn.setOnClickListener(v ->finish());
    }

    private void setupControls() {
        binding.BtnPlayPause.setOnClickListener(v->togglePlayPause());
        binding.BtnNext.setOnClickListener(v->playNext());
        binding.BtnPreV.setOnClickListener(v->playPrevious());
        binding.BtnShuffle.setOnClickListener(v->toggleShuffle());
        binding.BtnRepeat.setOnClickListener(v->toggleRepeat());



        binding.waveformSeekBar.setCallback(new WaveformSeekBar.Callback() {
            @Override
            public void onProgressChanged(WaveformSeekBar seekBar, float percent, boolean fromUser) {
                if(fromUser && player!=null){
                    long duration=player.getDuration();
                    long seekPos =(long) (percent*duration);
                    player.seekTo(seekPos);
                    binding.textElapsed.setText(formatTime((int) (seekPos / 1000)));
                }
            }

            @Override
            public void onStartTrackingTouch(WaveformSeekBar seekBar) {
                handler.removeCallbacks(updateRunnable);
            }

            @Override
            public void onStopTrackingTouch(WaveformSeekBar seekBar) {
                handler.postDelayed(updateRunnable, 0);
            }
        });
    }

    private void toggleRepeat() {
        isRepeat = !isRepeat;
        player.setRepeatMode(isRepeat ? Player.REPEAT_MODE_ONE : Player.REPEAT_MODE_OFF);
        binding.BtnRepeat.setColorFilter(isRepeat ? getResources().getColor(R.color.purple) : null);
    }

    private void toggleShuffle() {
        isShuffle=!isShuffle;
        if(isShuffle){
            Collections.shuffle(shuffledList);
            binding.BtnShuffle.setColorFilter(getResources().getColor(R.color.purple));
        } else {
            shuffledList= new ArrayList<>(songList);
            binding.BtnShuffle.clearColorFilter();
        }
        initPlayerWithSong(currentIndex);
    }

    private void playPrevious() {
        int listSize= isShuffle ? shuffledList.size() : songList.size();
        currentIndex= (currentIndex - 1 + listSize) % listSize;
        initPlayerWithSong(currentIndex);
    }

    private void togglePlayPause() {
        if(player.isPlaying()){
            player.pause();
            handler.removeCallbacks(updateRunnable);
        } else {
            player.play();
            handler.postDelayed(updateRunnable, 0);

        }
        updatePlayerPauseButtonIcon();
    }

    private void initPlayerWithSong(int index){
        Song song=isShuffle?shuffledList.get(index):songList.get(index);

        if(player!=null) player.release();
        player=new ExoPlayer.Builder(this).build();
        player.setRepeatMode(isRepeat? Player.REPEAT_MODE_ONE:Player.REPEAT_MODE_OFF);
        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                updatePlayerPauseButtonIcon();
                if(playbackState==Player.STATE_READY){
                    binding.textDuration.setText(formatTime((int)(player.getDuration() / 1000)));
                    handler.postDelayed(updateRunnable, 0);
                } else if (playbackState==Player.STATE_ENDED) {
                    playNext();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(PlayerActivity.this, "Error playing media:" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
        player.setMediaItem(MediaItem.fromUri(song.data));
        player.prepare();
        player.play();

        updatePlayerPauseButtonIcon();
        updateUI(song);
    }

    private void updateUI(Song song) {
        binding.textTitle.setText(song.title != null ? song.title : "");
        binding.textArtist.setText(song.artist!=null ? song.artist : "");
        setTitle(song.title);

        Uri albumArtUri= ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),song.albumId);
        if(hasAlbumArt(albumArtUri)){
            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(binding.ImageAlbumArtPlayer);

            Glide.with(this)
                    .asBitmap()
                    .load(albumArtUri)
                    .apply(bitmapTransform(new BlurTransformation(25, 3)))
                    .circleCrop()
                    .placeholder(R.drawable.ic_music_note)
                    .error(R.drawable.ic_music_note)
                    .into(binding.bgAlbumArt);
        } else {
            binding.ImageAlbumArtPlayer.setImageResource(R.drawable.ic_music_note);
            binding.bgAlbumArt.setImageResource(R.drawable.ic_music_note);
        }
    }

    private boolean hasAlbumArt(Uri albumArtUri){
        try(InputStream inputStream=getContentResolver().openInputStream(albumArtUri)){
            return inputStream!=null;
        } catch (Exception e){
            return false;
        }
    }

    private String formatTime(int seconds){
        return String.format("%02d:%02d", seconds / 60,seconds % 60);
    }
    private int[] createWaveForm(){
        Random random = new Random(System.currentTimeMillis());
        int[] values = new int[50];
        for (int i = 0; i < values.length; i++){
            values[i]=5 + random.nextInt(50);
        }
        return  values;
    }

    private void updatePlayerPauseButtonIcon(){
        binding.BtnPlayPause.setImageResource(
                player!=null && player.isPlaying()?
                        R.drawable.ic_pause :
                        R.drawable.ic_play_arrow
        );
    }
    private void playNext(){
        currentIndex= (currentIndex + 1) % (isShuffle ? shuffledList.size() : songList.size());
        initPlayerWithSong(currentIndex);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
        if(player != null){
            player.release();
            player = null;
        }
    }
}