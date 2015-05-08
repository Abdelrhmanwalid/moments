package abdelrhman.moments;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.melnykov.fab.FloatingActionButton;

import net.steamcrafted.loadtoast.LoadToast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFragment extends Fragment {

    private static final String TAG = CameraFragment.class.getSimpleName();
    FloatingActionButton fab;
    Camera camera;
    CameraPreview cameraPreview;
    FrameLayout frameLayout;
    MediaRecorder mediaRecorder;
    boolean isRecording;
    File temp, mediaFile;
    Date open, start, stop;
    int s = 0;
    Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragmetn_camera, container, false);
        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        context = getActivity();
        rootView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                },2000
                );
            }
        });

        isRecording = false;


        fab = (FloatingActionButton) rootView.findViewById(R.id.fab);
        fab.setVisibility(View.GONE);

        if (!CameraHelper.checkCameraHardware(context)) {
            Toast.makeText(context, "error", Toast.LENGTH_SHORT).show();
        }

        frameLayout = (FrameLayout) rootView.findViewById(R.id.camera_preview);


//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (isRecording) {
//                    mediaRecorder.stop();
//                    isRecording = false;
////                    button.setText("Start");
//                    stop = new Date();
//                    getVideo();
//                } else {
//                    start = new Date();
//                    isRecording = true;
////                    button.setText("Stop");
//                    Log.d("save", "start");
//                }
//            }
//        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
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
        cameraPreview = new CameraPreview(context, camera);
        frameLayout.addView(cameraPreview);
        temp = getTempFile();
        final LoadToast loadToast = new LoadToast(context);
        loadToast.setText("Please Wait");
        loadToast.show();
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        prepareMediaRecorder();
                        loadToast.success();
                        fab.setVisibility(View.VISIBLE);
                    }
                }, 1000
        );
    }

    void stop() {
        releaseMediaRecorder();
        releaseCamera();
        deleteFile(temp);
    }

    void getVideo() {

        long millis = Helper.getTime(open, start, stop, s);
        String time = Helper.getFormattedTime(millis);

        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        mediaFile = getOutputMediaFile();
        String cmd = String.format("-i %s  -ss %s -c copy -async 1 %s", temp.toString(), time, mediaFile.toString());
        Log.d("cmd:", cmd);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler());
            ffmpeg.execute(cmd, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String s) {
                    Helper.scanMedia(getActivity(), mediaFile, false);
                }

                @Override
                public void onProgress(String s) {

                }

                @Override
                public void onFailure(String s) {

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
        mediaRecorder = new MediaRecorder();
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
        mediaRecorder.setOutputFile(temp.toString());
        mediaRecorder.setPreviewDisplay(cameraPreview.getHolder().getSurface());
        mediaRecorder.setOrientationHint(MainActivity.orientation);
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            open = new Date();
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
        File mediaStorageDir = new File(getActivity().getExternalCacheDir(), "Moments");
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
}
