package abdelrhman.moments;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    Button button;
    Camera camera;
    CameraPreview cameraPreview;
    FrameLayout frameLayout;
    MediaRecorder mediaRecorder;
    boolean isRecording;
    File temp;
    Date start, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        isRecording = false;

        button = (Button) findViewById(R.id.button);



        if (!CameraHelper.checkCameraHardware(this)) {
            Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
        }

        frameLayout = (FrameLayout) findViewById(R.id.camera_preview);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording) {
                    mediaRecorder.stop();
                    releaseMediaRecorder();
                    releaseCamera();
                    isRecording = false;
                    button.setText("Start");
                    stop = new Date();
                    getVideo();
                } else {
                    start = new Date();
                    isRecording = true;
                    button.setText("Stop");
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        camera = CameraHelper.getInstenace();
        cameraPreview = new CameraPreview(this, camera);
        frameLayout.addView(cameraPreview);
        temp = getTempFile();
        new Thread(new Runnable(){

            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    prepareMediaRecorder();
                    mediaRecorder.start();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseCamera();
        deleteFile(temp);
    }

    void getVideo() {

        long diff = stop.getTime() - start.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;


        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        String cmd = String.format("-i %s  -ss %d:%d:%d -c copy -async 1 %s", temp.toString(), hours, minutes, seconds, getOutputMediaFile().toString());
        Log.d("diff: ", String.valueOf(diff));
        Log.d("seconds: ", String.valueOf(seconds));
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler());
        } catch (FFmpegNotSupportedException | FFmpegCommandAlreadyRunningException e) {
            e.printStackTrace();
        }

    }

    void prepareMediaRecorder() {
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(temp.toString());
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
            releaseMediaRecorder();
        }

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
            camera.release();
            camera = null;
        }
    }

    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
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
                "VID_" + timeStamp + ".mp4");


        return mediaFile;
    }

    File getTempFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        File file = new File(mediaStorageDir.getPath() + File.separator + "temp.mp4");
        return file;
    }

    void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }

}
