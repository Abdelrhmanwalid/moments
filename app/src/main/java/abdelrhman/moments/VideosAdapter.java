package abdelrhman.moments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.BaseSwipeAdapter;

import java.io.File;
import java.util.List;

public class VideosAdapter extends BaseSwipeAdapter {

    Context context;

    List<MainActivity.Video> videos;

    public VideosAdapter(Context context,List<MainActivity.Video> videos) {
        super();
        this.videos = videos;
        this.context = context;
    }

    @Override
    public int getSwipeLayoutResourceId(int i) {
        return R.id.swipe;
    }

    @Override
    public View generateView(int i, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.moments_list_item, null);
    }

    @Override
    public void fillValues(final int position, View view) {

        final SwipeLayout swipeLayout = (SwipeLayout)view.findViewById(getSwipeLayoutResourceId(position));
        ImageView thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
        final TextView title = (TextView) view.findViewById(R.id.title);
        final TextView duration = (TextView) view.findViewById(R.id.duration);
        final LinearLayout options = (LinearLayout) view.findViewById(R.id.optionsview);
        final ImageView share = (ImageView) view.findViewById(R.id.share);
        final ImageView rename = (ImageView) view.findViewById(R.id.rename);
        final ImageView delete = (ImageView) view.findViewById(R.id.delete);
        final ImageView play = (ImageView) view.findViewById(R.id.play);

        Bitmap thumbnaild = videos.get(position).thumbnail;
        final String titled = videos.get(position).title;
        String durationd = videos.get(position).duration;

        thumbnail.setImageBitmap(thumbnaild);
        title.setText(titled);
        duration.setText(durationd);

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        swipeLayout.addDrag(SwipeLayout.DragEdge.Left, options);
        swipeLayout.setRightSwipeEnabled(false);
        swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri videoURI = Uri.fromFile(new File(videos.get(position).path));
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setDataAndType(videoURI, "video/mp4");
                intent.putExtra(Intent.EXTRA_STREAM, videoURI);
                context.startActivity(intent);
            }
        });

        rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText name = new EditText(context);
                name.setText(videos.get(position).title);
                swipeLayout.close(true);
                new AlertDialog.Builder(context, R.style.dialog)
                        .setTitle("Rename Moment")
                        .setMessage("Enter New Name")
                        .setView(name)
                        .setPositiveButton("Rename", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String s = name.getText().toString();
                                MainActivity.Video video = videos.get(position);
                                videos.remove(position);
                                File file = new File(video.path);
                                File newFile = new File(file.getParent() + File.separator + s + ".mp4");
                                file.renameTo(newFile);
                                Helper.scanMedia(context, file, true);
                                Helper.scanMedia(context, newFile, false);
                                video.path = newFile.toString();
                                video.title = newFile.getName().replace(".mp4", "");
                                videos.add(position, video);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });
        
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri videoURI = Uri.fromFile(new File(videos.get(position).path));
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(videoURI, "video/mp4");
                intent.putExtra(Intent.EXTRA_STREAM, videoURI);
                context.startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final MainActivity.Video video = videos.get(position);
                swipeLayout.close();
                new AlertDialog.Builder(context, R.style.dialog)
                        .setTitle("Delete Moment ?")
                        .setMessage(video.title + " will be deleted from device")
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(video.path);
                                Helper.scanMedia(context, file, true);
                                file.delete();
                                videos.remove(position);
                                notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).show();
            }
        });

    }

    @Override
    public int getCount() {
        return videos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
