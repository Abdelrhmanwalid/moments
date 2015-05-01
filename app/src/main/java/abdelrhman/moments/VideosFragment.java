package abdelrhman.moments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;

public class VideosFragment extends ListFragment {

    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.moments_layout, container, false);

        context = getActivity();

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
        String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.BUCKET_ID,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA,
                MediaStore.Video.Media.DURATION};
        Cursor cursor = context.getContentResolver().query(uri, projection, condition, selectionArguments, sortOrder);

        VideosAdapter videosAdapter = new VideosAdapter(context, R.layout.moments_list_item, cursor);

        this.setListAdapter(videosAdapter);

        cursor.close();

        return rootView;
    }
}
