package com.example.astrocamera;

import android.Manifest;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class CameraLogic {
    private MainActivity mainActivity;
    private CameraDevice device;
    private ImageButton btn;
    private TextureView imageView;
    private ImageManipulator imageManipulator;

    public CameraLogic(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.imageView = mainActivity.getImageView();
        this.btn = mainActivity.getBtn();
        this.imageManipulator = new ImageManipulator(mainActivity.getApplicationContext());
    }

    public CameraDevice.StateCallback getCameraDevice() {
        return new CameraDevice.StateCallback() {
            @Override
            public void onOpened(@NonNull CameraDevice camera) {
                Log.d("Camera open", "Wooooo");
                device = camera;
            }

            @Override
            public void onDisconnected(@NonNull CameraDevice camera) {
                camera.close();
                device=null;
            }

            @Override
            public void onError(@NonNull CameraDevice camera, int error) {
                camera.close();
                Log.e("This is an error :(", "Big error :(" + error);
            }
        };
    }

    public void startCamera() {
        CameraManager cameraManager = (CameraManager) mainActivity.getSystemService(Context.CAMERA_SERVICE);
        String id = "";
        try {
            for(String s: cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(s);
                if(cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
                        != CameraCharacteristics.LENS_FACING_BACK)continue;
                id=s;
            }
            mainActivity.checkPermission(Manifest.permission.CAMERA);
            CameraDevice.StateCallback cameraStateCallback = getCameraDevice();
            cameraManager.openCamera(id, cameraStateCallback, new Handler());
            btn.setOnClickListener(v -> {
                try {
                    Thread.sleep(5000);
                    capturePhoto();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            });
        } catch (CameraAccessException e) {
            Log.e("What the fuck", "You should not get here");
            throw new RuntimeException(e);
        }

    }

    public void capturePhoto() {
        try {
            ImageReader imageReader = ImageReader.newInstance(imageView.getWidth(),imageView.getHeight(), ImageFormat.JPEG, 10);
            imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image im = reader.acquireLatestImage();
                    new Thread(() -> {
                        imageManipulator.addImage(im);
                        reader.close();
                    }).start();
                    capturePhoto();
                }
            }, new Handler());
            List<Surface> targets = Arrays.asList(new Surface(imageView.getSurfaceTexture()), imageReader.getSurface());
            device.createCaptureSession(targets, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    CaptureRequest.Builder builder = null;
                    try {
                        builder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                        builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, mainActivity.getExposure());
                        builder.set(CaptureRequest.SENSOR_SENSITIVITY, mainActivity.getIso());
                        builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f);
                        builder.set(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_OFF);
                        builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_OFF);
                        //builder.set(CaptureRequest.CONTROL_AWB_MODE, CameraMetadata.CONTROL_AWB_MODE_WARM_FLUORESCENT); TODO: fix
                        builder.addTarget(imageReader.getSurface());
                        builder.addTarget(new Surface(imageView.getSurfaceTexture()));
                        session.capture(builder.build(), null, new Handler());
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, new Handler());
        } catch (CameraAccessException e) {
            Log.e("What the fuck", "You should not get here");
            throw new RuntimeException(e);
        }
    }
}
