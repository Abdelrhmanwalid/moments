package abdelrhman.moments;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.nanotasks.BackgroundWork;
import com.nanotasks.Completion;
import com.nanotasks.Tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosFragment extends ListFragment {

    Context context;
    AppCompatActivity activity;
    List<Video> videos;

    @Override
    public void onStart() {
        super.onStart();
        context = getActivity();
        activity = (AppCompatActivity) getActivity();
        if (videos == null) {
            Tasks.executeInBackground(context, new BackgroundWork<VideosAdapter>() {
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
                    Cursor cursor = context.getContentResolver().query(uri, projection, condition, selectionArguments, sortOrder);
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

                    VideosAdapter videosAdapter = new VideosAdapter(context, videos);

                    cursor.close();
                    return videosAdapter;
                }
            }, new Completion<VideosAdapter>() {
                @Override
                public void onSuccess(Context context, VideosAdapter videosAdapter) {
                    setListAdapter(videosAdapter);
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }

                @Override
                public void onError(Context context, Exception e) {
                    Toast.makeText(context, "Error Loading Moments", Toast.LENGTH_LONG).show();
                }
            });

        }
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
