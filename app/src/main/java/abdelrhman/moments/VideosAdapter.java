package abdelrhman.moments;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends ArrayAdapter {

    private LayoutInflater inflater;
    private int layoutResource;
    private ListView listView;
    Context context;

    List<String> titles;
    List<Bitmap> thumbnails;
    List<String> durations;
    Cursor cursor;

    public VideosAdapter(Context context, int resource, Cursor cursor) {
        super(context, resource);
        layoutResource = resource;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cursor = cursor;
        this.context = context;
        titles = new ArrayList<>();
        thumbnails = new ArrayList<>();
        durations = new ArrayList<>();
        extractCursor();
    }

    void extractCursor() {
        int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        int durationColumn = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String filePath = cursor.getString(pathColumn);
                File file = new File(filePath);
                titles.add(file.getName());
                durations.add(Helper.getFormattedTime(cursor.getLong(durationColumn)));
                thumbnails.add(ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Video.Thumbnails.MINI_KIND));
            }
        }
        cursor.close();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View workingView;

        if (convertView == null) {
            workingView = inflater.inflate(layoutResource, null);
        } else {
            workingView = convertView;
        }

        ItemObjectHolder holder = getItemObjectHolder(workingView);
        Bitmap thumbnail = thumbnails.get(position);
        String title = titles.get(position);
        String duration = durations.get(position);

        holder.thumbnail.setImageBitmap(thumbnail);
        holder.title.setText(title);
        holder.duration.setText(duration);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.mainView.getLayoutParams();
        params.rightMargin = 0;
        params.leftMargin = 0;
        holder.mainView.setLayoutParams(params);

        return workingView;
    }

    private ItemObjectHolder getItemObjectHolder(View workingView) {
        Object tag = workingView.getTag();
        ItemObjectHolder holder;
        if (tag == null || !(tag instanceof ItemObjectHolder)) {
            holder = new ItemObjectHolder();
            holder.mainView = (RelativeLayout) workingView.findViewById(R.id.mainView);
            holder.deleteView = (RelativeLayout) workingView.findViewById(R.id.deleteview);
            holder.shareView = (RelativeLayout) workingView.findViewById(R.id.shareview);
            holder.thumbnail = (ImageView) workingView.findViewById(R.id.thumbnail);
            holder.title = (TextView) workingView.findViewById(R.id.title);
            holder.duration = (TextView) workingView.findViewById(R.id.duration);

            workingView.setTag(holder);
        } else {
            holder = (ItemObjectHolder) tag;
        }

        return holder;
    }


    public static class ItemObjectHolder {
        public RelativeLayout mainView;
        public RelativeLayout deleteView;
        public RelativeLayout shareView;
        public ImageView thumbnail;
        public TextView title;
        public TextView duration;

    }

    public void setListView(ListView view) {
        listView = view;
    }


    public class SwipeDetector implements View.OnTouchListener {

        private static final int MIN_DISTANCE = 300;
        private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
        private boolean motionInterceptDisallowed = false;
        private float downX, upX;
        private ItemObjectHolder holder;
        private int position;

        public SwipeDetector(ItemObjectHolder h, int pos) {
            holder = h;
            position = pos;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    upX = event.getX();
                    float deltaX = downX - upX;

                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && listView != null && !motionInterceptDisallowed) {
                        listView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    if (deltaX > 0) {
                        holder.deleteView.setVisibility(View.GONE);
                    } else {
                        // if first swiped left and then swiped right
                        holder.deleteView.setVisibility(View.VISIBLE);
                    }

                    swipe(-(int) deltaX);
                    return true;
                }

                case MotionEvent.ACTION_UP:
                    upX = event.getX();
                    float deltaX = upX - downX;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        // left or right
                        swipeRemove();
                    } else {
                        swipe(0);
                    }

                    if (listView != null) {
                        listView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    holder.deleteView.setVisibility(View.VISIBLE);
                    return true;

                case MotionEvent.ACTION_CANCEL:
                    holder.deleteView.setVisibility(View.VISIBLE);
                    return false;
            }

            return true;
        }

        private void swipe(int distance) {
            View animationView = holder.mainView;
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) animationView.getLayoutParams();
            params.rightMargin = -distance;
            params.leftMargin = distance;
            animationView.setLayoutParams(params);
        }

        private void swipeRemove() {
            remove(getItem(position));
            notifyDataSetChanged();
        }
    }

}
