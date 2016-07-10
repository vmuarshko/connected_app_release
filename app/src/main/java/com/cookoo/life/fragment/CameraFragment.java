package com.cookoo.life.fragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.hardware.Camera.Size;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import android.widget.Toast;
import com.cookoo.life.activity.MainActivity;
import com.cookoo.life.utilities.CameraUtil;
import com.cookoo.life.R;

public class CameraFragment extends Fragment implements PictureCallback,
        ShutterCallback, OnClickListener, SensorEventListener {
    private final String TAG = CameraFragment.class.getSimpleName();

    private Camera mCamera;

    private int defaultCameraId = 0;
    private int numberCameras = 0;
    private volatile boolean isMirror = false;
    private volatile int orientation = 0;

    // layout
    private FrameLayout mCameraPreviewContainer;
    private Preview mPreview;
    public static boolean CameraIsActive = false;
    private static ImageButton mTakePhoto;
    private ImageButton mSwitchCamera;
    private ImageButton mFlashMode;

    SurfaceHolder mHolder;
    private static boolean canTakePicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Log.d("camera","onActivityCreated() CameraFragment");
        this.canTakePicture = true;
        numberCameras = Camera.getNumberOfCameras();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d("camera","onCreateView() CameraFragment");
        View rootView = inflater.inflate(R.layout.fragment_camera, null);

        mCameraPreviewContainer = (FrameLayout) rootView
                .findViewById(R.id.camera_layout_parent);
        mCameraPreviewContainer.removeAllViews();


        mPreview = new Preview(getActivity());
        mCameraPreviewContainer.addView(mPreview);
        mCameraPreviewContainer.requestLayout();

        Log.d("camera_logic","ONCREATEVIEW():: mCamera.PreviewContainer() - mCamea.removeAllViews() - " +
                "mPreview new Preview() = mCameraPreviewContainer.addview() - mCameraPreviewContainer.requestLayout();");

        mTakePhoto = (ImageButton) rootView
                .findViewById(R.id.fragment_camera_take_photo_btn);
        mSwitchCamera = (ImageButton) rootView
                .findViewById(R.id.fragment_camera_switch_btn);
       // mFlashMode = (ImageButton) rootView
       //         .findViewById(R.id.fragment_camera_flash_btn);
        CameraFragment.CameraIsActive = true;
        mTakePhoto.setOnClickListener(this);
        mSwitchCamera.setOnClickListener(this);
       // mFlashMode.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d("camera","onCreateOptionsMenu() CameraFragment");
        inflater.inflate(R.menu.camera_menu, menu);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CameraFragment.CameraIsActive = false;
    }

    public static void forceButtonPress(){
        mTakePhoto.performClick();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPreview = null;
        mTakePhoto.setOnClickListener(null);
        mTakePhoto = null;
        mCameraPreviewContainer = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("camera","onStart() CameraFragment");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

	/*
	 * A safe method for opening and closing the camera Implemented by Jesse
	 * Ryan
	 */

    private void releaseCameraAndPreview() {
        mPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
            Log.d("camera","releaseCameraAndPreview() mCamera != null");
        }
        Log.d("camera","releaseCameraAndPreview()");
    }

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.oBack = false;

        try {
            CameraInfo cameraInfo = new CameraInfo();

            for (int i = 0; i < numberCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);

                if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                    defaultCameraId = i;
                }
            }

            mCamera = Camera.open(defaultCameraId);
            mPreview.setCamera(mCamera);
            mCamera.startPreview();


            Log.d("camera_logic","ONRESUME():: mCamera = Camera.open(defaultCameraId);" +
                    " mPreview.setCamera(mCamera); mCamera.startPreview(); ");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCameraAndPreview();
    }

    private class Preview extends ViewGroup implements SurfaceHolder.Callback {

        private final String TAG = getClass().getSimpleName();

        SurfaceView mSurfaceView;
        Size mPreviewSize;
        List<Size> mSupportedPreviewSizes;
        Camera mCamera;

        Preview(Context context) {
            super(context);
            Log.d("camera_logic","Preview Preview()  ");
            mSurfaceView = new SurfaceView(MainActivity.activity);
            addView(mSurfaceView);
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = mSurfaceView.getHolder();
            mHolder.addCallback(this);
        }

        public void setCamera(Camera camera) {
            if (mCamera == camera) {
                Log.d("camera","setCamera() mCamera == camera");
                return;
            }

            mCamera = camera;

            if (mCamera != null) {
                List<Size> localSizes = mCamera.getParameters()
                        .getSupportedPreviewSizes();
                mSupportedPreviewSizes = localSizes;
                requestLayout();


                setCameraDisplayOrientation(getActivity(), 1);
                prepareFlash();
                Log.d("camera","setCamera() mCamera != null");
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            // We purposely disregard child measurements because act as a
            // wrapper to a SurfaceView that centers the camera preview instead
            // of stretching it.
            Point p = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(p);
            final int width = p.x;
            int height = p.y;

            if (mSupportedPreviewSizes != null) {
                mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes,
                        width, height);
                Log.v(TAG, "onMeasure pr w= " + mPreviewSize.width);
                Log.v(TAG, "onMeasure pr h= " + mPreviewSize.height);
                setMeasuredDimension(1088, 1920);
            }

            Log.d("camera_logic","OnMeasure()");
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            final View child = getChildAt(0);
            child.layout(l, t, r, b);

            Log.d("camera_logic","onLayout()");
        }

        public void surfaceCreated(SurfaceHolder holder) {
            try {
                if (mCamera != null) {

                    mCamera.setPreviewDisplay(mHolder);
                    Parameters params = mCamera.getParameters();
                    List<Size> pictureSizes = params.getSupportedPictureSizes();

                    Size resultPicSize = CameraUtil
                            .getBestPictureSize(pictureSizes);
                    params.setPictureSize(resultPicSize.width,
                            resultPicSize.height);

                    mCamera.setParameters(params);
                    mCamera.startPreview();
                }
            } catch (IOException exception) {
                Log.e(TAG, "IOException caused by setPreviewDisplay()",
                        exception);
            }

            Log.d("camera_logic","Preview() surfaceCreated(SurfaceHolder holder)");
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // Surface will be destroyed when we return, so stop the preview.
            if (mCamera != null) {
                mCamera.release();
                //		mCamera.stopPreview();
                Log.d("camera_logic","surfaceDestroyed()");
            }
        }

        protected Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
            double targetRatio = (double) w / h;
            if (sizes == null)
                return null;

            Size optimalSize = null;
            int screenSquare = h * w;
            int minSizeDifference = -1;

            for (Size size : sizes) {
                // TODO test ratio
                double ratio = (double) size.height / size.width;
                int currentPreviewSquare = size.height * size.width;

                if (ratio == targetRatio){
                    continue;
                }

                if (minSizeDifference < 0) {
                    minSizeDifference = Math.abs(screenSquare
                            - currentPreviewSquare);
                    optimalSize = size;
                } else {
                    if (minSizeDifference > Math.abs(screenSquare
                            - currentPreviewSquare)) {
                        minSizeDifference = Math.abs(screenSquare
                                - currentPreviewSquare);
                        optimalSize = size;
                    }
                }

            }

            Log.d("camera_logic","getOptimalPreviewSize() :: optimalSize: h "+optimalSize.height+" w" +optimalSize.width);
            return optimalSize;

        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w,
                                   int h) {


        }

        public void setCameraDisplayOrientation(Activity activity, int cameraId) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            int rotation = activity.getWindowManager().getDefaultDisplay()
                    .getRotation();
            int degrees = 0;
            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360; // compensate the mirror
            } else { // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }

            Parameters params = mCamera.getParameters();
            params.setRotation(result);
            mCamera.setParameters(params);

            mCamera.setDisplayOrientation(result);

            Log.d("camera_logic","Preview() setCameraDisplayOrientation()");
        }
    }

    @Override
    public void onPictureTaken(byte[] image, Camera camera) {
        Log.d(TAG, "Picture took!");

        // save image on external data storage
        new PictureProcessor(image).execute();
    }

    public void takePictureAutoFocus() {

    if(canTakePicture){
        mCamera.autoFocus(new AutoFocusCallback() {

            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (!camera.enableShutterSound(true)) {
                    Log.d(TAG, "Default sound disabled!");
                }

                if(CameraFragment.canTakePicture == true){
                    mCamera.takePicture(CameraFragment.this, null,
                            CameraFragment.this);
                }

                canTakePicture = false;
            }
        });

        }
    }

    private void switchCamera() {

        if (mCamera != null) {
            try {

                mCameraPreviewContainer.removeAllViews();
                mPreview = new Preview(getActivity());
                mCameraPreviewContainer.addView(mPreview);
                mCameraPreviewContainer.requestLayout();
                CameraInfo cameraInfo = new CameraInfo();
                Camera.getCameraInfo(defaultCameraId, cameraInfo);
                if (CameraInfo.CAMERA_FACING_BACK == cameraInfo.facing) {
                    defaultCameraId = CameraInfo.CAMERA_FACING_FRONT;
                    isMirror = true;
                } else if (CameraInfo.CAMERA_FACING_FRONT == cameraInfo.facing) {
                    defaultCameraId = CameraInfo.CAMERA_FACING_BACK;
                    isMirror = false;
                }
                    mCamera = Camera.open(defaultCameraId);
                    mPreview.setCamera(mCamera);
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
            } catch (Exception ex){
                Toast.makeText(this.getActivity(), R.string.camera_unsupported, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "problem opening camera shutter for camera" + defaultCameraId);
                ex.printStackTrace();
                mCamera = Camera.open(CameraInfo.CAMERA_FACING_FRONT);
                mPreview.setCamera(mCamera);
                try {
                    mCamera.setPreviewDisplay(mHolder);
                    mCamera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fragment_camera_take_photo_btn:
                takePictureAutoFocus();
                break;
            case R.id.fragment_camera_switch_btn:
                switchCamera();
                break;
            case R.id.fragment_camera_flash_btn:
                // set up next flash mode
                prepareFlash();
                break;
        }
    }

    @Override
    public void onShutter() {
        Log.d(TAG, "Shutter called");
    }

    private class PictureProcessor extends AsyncTask<Void, Void, String> {
        private byte[] data;

        private ProgressDialog pDialog;

        public PictureProcessor(byte[] data) {
            this.data = data;
        }

        @Override
        public void onPreExecute() {
            mCameraPreviewContainer.setVisibility(View.INVISIBLE);

        }


        @Override
        public void onPostExecute(String path) {
            mCameraPreviewContainer.setVisibility(View.VISIBLE);
            if (!TextUtils.isEmpty(path)) {
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                File f = new File(path);
                Uri contentUri = Uri.fromFile( f );
                mediaScanIntent.setData(contentUri);
                getActivity().sendBroadcast(mediaScanIntent);
            } else {
                Log.e(TAG, "Wrong file!!!");
            }
            Log.d(TAG, "Photo saved on gallery");
            try{
                Log.d("camera_logic","mCamera.startPreview();");
                mCamera.startPreview();
            }
            catch(Exception e)
            {
                Log.d("camera_logic","Critical Error"+e);
            };
            canTakePicture = true;
        }

        @Override
        protected String doInBackground(Void... arg0) {
            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                // temporary picture name;
                String fileName = UUID.randomUUID() + ".jpg";

                File pictureFile = new File(
                        Environment
                                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        fileName);
                FileOutputStream outStream = null;

                try {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                            data.length);

                    if (isMirror) {
                        int rotateDegree = 0;
                        rotateDegree = 180;
                        bitmap = rotateAndMirror(bitmap, rotateDegree, true);
                    }

                    outStream = new FileOutputStream(pictureFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                    bitmap.recycle();

                    if (!isMirror) {// exif
                        ExifInterface exif = new ExifInterface(
                                pictureFile.getAbsolutePath());
                        exif.setAttribute(ExifInterface.TAG_ORIENTATION,
                                String.valueOf(orientation));

                        Log.d(TAG, "Exif orientation is mirror : " + isMirror);
                        exif.saveAttributes();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return pictureFile.getAbsolutePath();
            }

            return null;
        }



        public Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror) {
            if ((degrees != 0 || mirror) && b != null) {
                Matrix m = new Matrix();
                // Mirror first.
                // horizontal flip + rotation = -rotation + horizontal flip
                if (mirror) {
                    m.postScale(-1, 1);
                    degrees = (degrees + 360) % 360;
                    if (degrees == 0 || degrees == 180) {
                        m.postTranslate(b.getWidth(), 0);
                    } else if (degrees == 90 || degrees == 270) {
                        m.postTranslate(b.getHeight(), 0);
                    } else {
                        throw new IllegalArgumentException("Invalid degrees="
                                + degrees);
                    }
                }
                if (degrees != 0) {
                    // clockwise
                    m.postRotate(degrees, (float) b.getWidth() / 2,
                            (float) b.getHeight() / 2);
                }

                try {
                    Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                            b.getHeight(), m, true);
                    if (b != b2) {
                        b.recycle();
                        b = b2;
                    }
                } catch (OutOfMemoryError ex) {
                    // We have no memory to rotate. Return the original bitmap.
                    Log.d("camera_logic","rotateAndMirror() EXCEPTION : " + ex.getMessage());
                }
            }
            Log.d("camera_logic","rotateAndMirror()");
            return b;
        }
    }

    public void prepareFlash() {
        String currentFlashMode = mCamera.getParameters().getFlashMode();
        int drawableRes = R.drawable.camera_flash_auto;
        Parameters params = mCamera.getParameters();

        List<String> supportedFlashMode = mCamera.getParameters()
                .getSupportedFlashModes();

        if (supportedFlashMode == null || supportedFlashMode.size() == 0) {
            drawableRes = R.drawable.camera_flash_off;
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        } else {

            Log.d(TAG, supportedFlashMode.toString());

            if (Camera.Parameters.FLASH_MODE_AUTO.equals(currentFlashMode)) {
                drawableRes = R.drawable.camera_flash_torch;
                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            } else if (Camera.Parameters.FLASH_MODE_OFF
                    .equals(currentFlashMode)) {
                drawableRes = R.drawable.camera_flash_on;
                params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else if (Camera.Parameters.FLASH_MODE_ON.equals(currentFlashMode)) {
                drawableRes = R.drawable.camera_flash_auto;
                params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            } else if (Camera.Parameters.FLASH_MODE_TORCH
                    .equals(currentFlashMode)) {
                drawableRes = R.drawable.camera_flash_off;
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }

        //mFlashMode.setImageResource(drawableRes);
        mCamera.getParameters().setFlashMode(params.getFlashMode());
        Log.d("camera","prepareFlash()");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // set up display orientation;
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            if (event.values[0] < 4 && event.values[0] > -4) {
                if (event.values[1] > 0
                        && orientation != ExifInterface.ORIENTATION_ROTATE_90) {
                    // UP
                    orientation = ExifInterface.ORIENTATION_ROTATE_90;

                    Log.i(TAG, "Orientation : 90");
                } else if (event.values[1] < 0
                        && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
                    // UP SIDE DOWN
                    orientation = ExifInterface.ORIENTATION_ROTATE_270;

                    Log.i(TAG, "Orientation : 270");
                }
            } else if (event.values[1] < 4 && event.values[1] > -4) {
                if (event.values[0] > 0
                        && orientation != ExifInterface.ORIENTATION_NORMAL) {
                    // LEFT
                    orientation = ExifInterface.ORIENTATION_NORMAL;

                    Log.i(TAG, "Orientation : 0");
                } else if (event.values[0] < 0
                        && orientation != ExifInterface.ORIENTATION_ROTATE_180) {
                    // RIGHT
                    orientation = ExifInterface.ORIENTATION_ROTATE_180;

                    Log.i(TAG, "Orientation : 180");
                }
            }
        }
    }
}
