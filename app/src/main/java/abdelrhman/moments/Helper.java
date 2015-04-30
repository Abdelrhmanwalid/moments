package abdelrhman.moments;

import android.util.Log;

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
}
