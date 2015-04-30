package abdelrhman.moments;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CameraFragment extends Fragment{

    AppCompatButton button;
    Camera camera;
    CameraPreview cameraPreview;
    FrameLayout frameLayout;
    MediaRecorder mediaRecorder;
    boolean isRecording;
    File temp;
    Date open, start, stop;
    int s = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragmetn_camera, container, false);

        isRecording = false;

        button = (AppCompatButton) rootView.findViewById(R.id.button);

        if (!CameraHelper.checkCameraHardware(getActivity())) {
            Toast.makeText(getActivity(), "error", Toast.LENGTH_SHORT).show();
        }

        frameLayout = (FrameLayout) rootView.findViewById(R.id.camera_preview);

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
                    Log.d("save", "start");
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        camera = CameraHelper.getInstenace();
        Camera.Parameters parameters = camera.getParameters();
        parameters.setRecordingHint(true);
        camera.setParameters(parameters);
        cameraPreview = new CameraPreview(getActivity(), camera);
        frameLayout.addView(cameraPreview);
//        temp = getTempFile();
//        final ProgressDialog progressDialog = new ProgressDialog(this);
//        progressDialog.setMessage("Please Wait");
//        progressDialog.show();
//        new Handler().postDelayed(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        prepareMediaRecorder();
//                        progressDialog.dismiss();
//                    }
//                }, 1000
//        );
    }

    @Override
    public void onStop() {
        super.onStop();
//        releaseMediaRecorder();
        releaseCamera();
//        deleteFile(temp);
    }

    void getVideo() {

        long millis = Helper.getTime(open, start, stop, s);
        String time = Helper.getFormattedTime(millis);

        FFmpeg ffmpeg = FFmpeg.getInstance(getActivity());
        String cmd = String.format("-i %s  -ss %s -c copy -async 1 %s", temp.toString(), time, getOutputMediaFile().toString());
        Log.d("cmd:", cmd);
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
        return new File(mediaStorageDir.getPath() + File.separator + "temp.mp4");
    }

    void deleteFile(File file) {
        if (file.exists()) {
            file.delete();
        }
    }
}
