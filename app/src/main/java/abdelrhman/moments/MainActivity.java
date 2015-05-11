package abdelrhman.moments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.appyvet.rangebar.RangeBar;
import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;
import com.software.shell.fab.ActionButton;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    List<Video> videos;
    ActionButton fab;
    TextView emptyList;
    ListView listView;
    SharedPreferences.Editor editor;
    int seconds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        SharedPreferences sharedPreferences = this.getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();

        seconds = sharedPreferences.getInt("sec", 0);

        fab = (ActionButton) findViewById(R.id.fab_moments);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                intent.putExtra("sec", seconds);
                startActivity(intent);
            }
        });
        emptyList = (TextView) findViewById(R.id.moments_empty);
        listView = (ListView) findViewById(R.id.moments_list);
    }

    @Override
    protected void onStart() {
        super.onStart();
        videos = null;
        getVideos();
    }

    private void getVideos() {
        if (videos == null) {
            final LoadToast loadToast = new LoadToast(this);
            loadToast.setText("Loading");
            loadToast.show();
            Tasks.executeInBackground(this, new BackgroundWork<VideosAdapter>() {
                @Override
                public VideosAdapter doInBackground() throws Exception {
                    File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Moments");
                    if (!mediaStorageDir.exists()) {
                        if (!mediaStorageDir.mkdirs()) {
                            return null;
                        }
                    }
                    Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    String condition = MediaStore.Video.Media.DATA + " like?";
                    String[] selectionArguments = new String[]{"%" + mediaStorageDir.toString() + "%"};
                    String sortOrder = MediaStore.Video.Media.DATE_TAKEN + " DESC";
                    String[] projection = {MediaStore.Images.Media.DATA, MediaStore.Video.Media.DURATION,
                            MediaStore.Video.Media._ID};
                    Cursor cursor = getContentResolver().query(uri, projection, condition, selectionArguments, sortOrder);
                    videos = new ArrayList<>();

                    int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

                    while (cursor.moveToNext()) {
                        String filePath = cursor.getString(pathColumn);
                        File file = new File(filePath);
                        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath,
                                MediaStore.Video.Thumbnails.MINI_KIND);
                        if (bitmap != null)
                            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 192, true);
                        Video video = new Video(
                                file.getName().replace(".mp4", ""),
                                Helper.getFormattedTime(cursor.getLong(durationColumn)),
                                filePath,
                                bitmap
                        );
                        videos.add(video);
                    }

                    VideosAdapter videosAdapter = new VideosAdapter(MainActivity.this, videos);

                    cursor.close();
                    return videosAdapter;
                }
            }, new Completion<VideosAdapter>() {
                @Override
                public void onSuccess(Context context, VideosAdapter videosAdapter) {
                    listView.setAdapter(videosAdapter);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (videos.size() == 0) {
                                emptyList.setVisibility(View.VISIBLE);
                                loadToast.error();
                            } else {
                                emptyList.setVisibility(View.GONE);
                                loadToast.success();
                            }
                        }
                    }, 500);
                    emptyList.setText("No Moments Found");
                }

                @Override
                public void onError(Context context, Exception e) {
                    emptyList.setText("Error Loading Moments");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadToast.error();
                        }
                    }, 500);
                }
            });

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_seconds) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            LinearLayout linearLayout = (LinearLayout) layoutInflater.inflate(R.layout.rangebar_dialog, null);
            final RangeBar rangeBar = (RangeBar) linearLayout.findViewById(R.id.rangebar);
            rangeBar.setSeekPinByValue(seconds);
            String s;
            if (seconds == 1) {
                s = "Second";
            } else {
                s = "Seconds";
            }
            final AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.dialog)
                    .setMessage(String.format("Pre Record %d %s", seconds, s))
                    .setView(linearLayout)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            seconds = rangeBar.getRightIndex();
                            editor.putInt("sec", seconds);
                            editor.commit();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .create();
            alertDialog.show();
            rangeBar.setOnRangeBarChangeListener(new RangeBar.OnRangeBarChangeListener() {
                @Override
                public void onRangeChangeListener(RangeBar rangeBar, int i, int i1, String s, String s1) {
                    if (i1 == 1) {
                        s = "Second";
                    } else {
                        s = "Seconds";
                    }
                    alertDialog.setMessage(String.format("Pre Record %d %s", i1, s));
                }
            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public class Video {
        String title;
        String duration;
        String path;
        Bitmap thumbnail;

        public Video(String title, String duration, String path, Bitmap thumbnail) {
            this.title = title;
            this.duration = duration;
            this.path = path;
            this.thumbnail = thumbnail;
        }
    }
}
