package com.cookoo.life.utilities;

import java.util.Iterator;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.Log;

public class CameraUtil {

	private static final String TAG = "CAMERA UTIL";

	public static final boolean isCameraHasFlash(Parameters params) {
		String flashMode = params.getFlashMode();

		if (flashMode == null) {
			return false;
		}
		return true;
	}

	public static final String getFlashType(Parameters params) {
		String currentFlashMode = params.getFlashMode();
		List<String> supportedFlashModes = params.getSupportedFlashModes();
		if(supportedFlashModes == null | supportedFlashModes.isEmpty()) {
			return null;
		}
		
		for (String flashMode : supportedFlashModes) {
			if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)
					&& flashMode.equals(currentFlashMode)) {
				return Camera.Parameters.FLASH_MODE_OFF;
			} else if (flashMode.equals(Camera.Parameters.FLASH_MODE_ON)
					&& flashMode.equals(currentFlashMode)) {
				return Camera.Parameters.FLASH_MODE_ON;
			} else if (flashMode.equals(Camera.Parameters.FLASH_MODE_AUTO)
					&& flashMode.equals(currentFlashMode)) {
				return Camera.Parameters.FLASH_MODE_AUTO;
			}
		}

		return null;
	}

	public static final Camera createCameraById(int cameraId) {
		try {
			return Camera.open(cameraId);
		} catch (Exception e) {
			Log.e(TAG, "camera can't create" + e.getMessage());
		}
		return null;
	}

	public static Size getBestPictureSize(List<Size> supportedSizes) {
		Camera.Size size = null;
		Camera.Size supportedSize = null;
		double temporalDiff = 0;
		
		double diff = Math.sqrt(Math.pow(supportedSizes.get(0).width, 2)
				+ Math.pow(supportedSizes.get(0).height, 2));
		size = supportedSizes.get(0);

		Iterator<Size> iterator = supportedSizes.iterator();
		while (iterator.hasNext()) {
			supportedSize = iterator.next();
			temporalDiff = Math.sqrt(Math.pow(supportedSize.width, 2)
					+ Math.pow(supportedSize.height, 2));

			if (temporalDiff > diff) {
				diff = temporalDiff;
				size = supportedSize;
			}
		}
		return size;
	}
	
	public static void setCameraAutoFocus(Camera camera) {
		Parameters params = camera.getParameters();
		String focusMode = params.getFocusMode();
		
		if(focusMode.equals(Parameters.FOCUS_MODE_AUTO)) {
			params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}
		if(params.getMaxNumFocusAreas() != 0) {
			params.setFocusAreas(params.getFocusAreas());
		}
		
		camera.setParameters(params);
	}
}
