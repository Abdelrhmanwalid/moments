package abdelrhman.moments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

public class CameraHelper {
    public static boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getInstance(){
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e){

        }
        return camera;
    }
}
