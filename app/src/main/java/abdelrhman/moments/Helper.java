package abdelrhman.moments;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Helper {

    public static long getTime(Date open, Date start, Date stop, int seconds){
        start.setTime(start.getTime() - (seconds*1000));
        long a = stop.getTime() - open.getTime();
        long b = stop.getTime() - start.getTime();
        Log.d("a:", getFormattedTime(a));
        Log.d("b:", getFormattedTime(b));
        Log.d("a-b:", getFormattedTime(a-b));
        Log.d("b-a:", getFormattedTime(b-a));
        return a - b;
    }

    public static String getFormattedTime(long millis) {
        return String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
    }

    static void scanMedia(final Context context, File file, final boolean delete){
        Intent mediaScannerIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri fileContentUri = Uri.fromFile(file);
        mediaScannerIntent.setData(fileContentUri);
        context.sendBroadcast(mediaScannerIntent);
        MediaScannerConnection.scanFile(context, new String[] { file.toString() },
                new String[] {"video/mp4"}, new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        if (delete)
                        context.getContentResolver()
                                .delete(uri, null, null);
                    }
                });
    }
}
