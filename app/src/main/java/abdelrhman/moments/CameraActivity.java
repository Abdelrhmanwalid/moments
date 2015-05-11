package abdelrhman.moments;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.software.shell.fab.ActionButton;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CameraActivity extends AppCompatActivity {

    private static final String TAG = CameraActivity.class.getSimpleName();
    public static int orientation;

    ActionButton fab;
    Camera camera;
    CameraPreview cameraPreview;
    FrameLayout frameLayout;
    MediaRecorder mediaRecorder;
    boolean isRecording;
    File temp, mediaFile;
    Date open, start, stop;
    int s;
    Chronometer rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setRequestedOrientation(getScreenOrientation());
        setContentView(R.layout.activity_cmera);
        isRecording = false;
        s = getIntent().getIntExtra("sec", 0);

        if (!CameraHelper.checkCameraHardware(this)) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        fab = (ActionButton) findViewById(R.id.fab);
        rec = (Chronometer) findViewById(R.id.recorded);

        fab.hide();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    mediaRecorder.stop();
                    rec.stop();
                    rec.setVisibility(View.INVISIBLE);
                    isRecording = false;
                    stop = new Date();
                    getVideo();
                    prepareMediaRecorder();
                    fabTransition(R.drawable.ic_video_start);
                } else {
                    start = new Date();
                    fabTransition(R.drawable.ic_video_stop);
                    if (start.getTime() - open.getTime() < (s*1000)){
                        s = (int) ((start.getTime() - open.getTime()) / 1000);
                    }
                    rec.setBase(SystemClock.elapsedRealtime() - (s*1000));
                    rec.setVisibility(View.VISIBLE);
                    rec.start();
                    isRecording = true;
                    Log.d("save", "start");
                }
            }
        });

        frameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        frameLayout.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                new Handler().postDelayed(new Runnable() {
                                              @Override
                                              public void run() {
                                                  frameLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                                              }
                                          }, 2000
                );
            }
        });
    }

    void fabTransition(final int drawable) {
        fab.hide();
//        fab.bringToFront();
//        fab.setImageDrawable(context.getResources().getDrawable(drawable));
//        fab.show(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                fab.bringToFront();
                fab.setImageDrawable(getResources().getDrawable(drawable));
                fab.show();
            }
        }, 600);
    }

    @Override
    public void onResume() {
        super.onResume();
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_video_start));
        start();
    }

    void start() {
        if (camera != null) {
            camera.startPreview();
        } else {
            camera = CameraHelper.getInstance();
            camera.startPreview();
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRecordingHint(true);
        camera.setParameters(parameters);
        cameraPreview = new CameraPreview(this, camera);
        frameLayout.addView(cameraPreview);
        temp = getTempFile();
        rec.bringToFront();
        fab.bringToFront();
        prepareMediaRecorder();
    }

    void stop() {
        releaseMediaRecorder();
        releaseCamera();
        deleteFile(temp);
    }

    void getVideo() {

        final LoadToast loadToast = new LoadToast(this);
        loadToast.setText("Saving");
        loadToast.show();
        long millis = Helper.getTime(open, start, stop, s);
        String time = Helper.getFormattedTime(millis);

        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        mediaFile = getOutputMediaFile();
        String cmd = String.format("-i %s  -ss %s -c copy -async 1 %s", temp.toString(), time, mediaFile.toString());
        Log.d("cmd:", cmd);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String s) {
                    Helper.scanMedia(CameraActivity.this, mediaFile, false);
                    loadToast.success();
                }

                @Override
                public void onProgress(String s) {

                }

                @Override
                public void onFailure(String s) {
                    loadToast.error();
                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

    }

    void prepareMediaRecorder() {
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        mediaRecorder = new MediaRecorder();
                        camera.unlock();
                        mediaRecorder.setCamera(camera);
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
                        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
                        mediaRecorder.setOutputFile(temp.toString());
                        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
                        mediaRecorder.setOrientationHint(CameraActivity.orientation);
                        try {
                            mediaRecorder.prepare();
                            mediaRecorder.start();
                            open = new Date();
                        } catch (IOException e) {
                            e.printStackTrace();
                            releaseMediaRecorder();
                        }
                        fab.setVisibility(View.VISIBLE);
                        fab.bringToFront();
                        fab.show();
                    }
                }, 1000
        );
    }

    void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            camera.lock();
        }
    }

    void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    private File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "Moments");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                "Moment_" + timeStamp + ".mp4");

        return mediaFile;
    }

    File getTempFile() {
        File mediaStorageDir = new File(getExternalCacheDir(), "Moments");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        return new File(mediaStorageDir.getPath() + File.separator + "temp.mp4");
    }

    void deleteFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        stop();
    }

    private int getScreenOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int orientation;
        // if the device's natural orientation is portrait:
        if ((rotation == Surface.ROTATION_0
                || rotation == Surface.ROTATION_180) && height > width ||
                (rotation == Surface.ROTATION_90
                        || rotation == Surface.ROTATION_270) && width > height) {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    CameraActivity.orientation = 90;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    CameraActivity.orientation = 0;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    CameraActivity.orientation = 270;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    CameraActivity.orientation = 180;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "portrait.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    CameraActivity.orientation = 90;
                    break;
            }
        }
        // if the device's natural orientation is landscape or if the device
        // is square:
        else {
            switch (rotation) {
                case Surface.ROTATION_0:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    CameraActivity.orientation = 0;
                    break;
                case Surface.ROTATION_90:
                    orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    CameraActivity.orientation = 90;
                    break;
                case Surface.ROTATION_180:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    CameraActivity.orientation = 180;
                    break;
                case Surface.ROTATION_270:
                    orientation =
                            ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    CameraActivity.orientation = 270;
                    break;
                default:
                    Log.e(TAG, "Unknown screen orientation. Defaulting to " +
                            "landscape.");
                    orientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    CameraActivity.orientation = 0;
                    break;
            }
        }

        return orientation;
    }
}
