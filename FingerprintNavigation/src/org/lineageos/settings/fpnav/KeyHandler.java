/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.fpnav;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.input.InputManager;
import android.hardware.input.InputManagerGlobal;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;

import com.android.internal.os.DeviceKeyHandler;

import org.lineageos.internal.util.ActionUtils;

/**
 * Runs the action configured for each fingerprint navigation gesture.
 *
 * Loaded into system_server by PhoneWindowManager, so it only uses
 * framework APIs.
 */
public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = "FpNavKeyHandler";

    private final Context mContext;
    private final ContentResolver mResolver;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final PowerManager mPowerManager;
    private final Vibrator mVibrator;
    private final CameraManager mCameraManager;

    private String mRearCameraId;
    private boolean mTorchEnabled;

    public KeyHandler(Context context) {
        mContext = context;
        mResolver = context.getContentResolver();
        mPowerManager = context.getSystemService(PowerManager.class);
        mVibrator = context.getSystemService(Vibrator.class);
        mCameraManager = context.getSystemService(CameraManager.class);
        mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
            @Override
            public void onTorchModeChanged(String cameraId, boolean enabled) {
                if (cameraId.equals(mRearCameraId)) mTorchEnabled = enabled;
            }

            @Override
            public void onTorchModeUnavailable(String cameraId) {
                if (cameraId.equals(mRearCameraId)) mTorchEnabled = false;
            }
        }, mHandler);
    }

    @Override
    public KeyEvent handleKeyEvent(KeyEvent event) {
        final String key = Constants.SCAN_CODE_KEYS.get(event.getScanCode());
        if (key == null || !isFromFingerprintSensor(event)) {
            return event;
        }

        if (!hasSetupCompleted()) {
            return event;
        }

        final int action = Settings.Secure.getIntForUser(mResolver, key,
                Constants.getDefaultAction(key), UserHandle.USER_CURRENT);

        if (event.getAction() == KeyEvent.ACTION_UP) {
            mHandler.post(() -> performAction(action));
        }
        return null;
    }

    private boolean isFromFingerprintSensor(KeyEvent event) {
        final InputDevice device = InputDevice.getDevice(event.getDeviceId());
        return device != null && Constants.INPUT_DEVICE_NAME.equals(device.getName());
    }

    private boolean hasSetupCompleted() {
        return Settings.Secure.getIntForUser(mResolver, Settings.Secure.USER_SETUP_COMPLETE, 0,
                UserHandle.USER_CURRENT) != 0;
    }

    private void performAction(int action) {
        switch (action) {
            case Constants.ACTION_NONE:
                return;
            case Constants.ACTION_HOME:
                injectKey(KeyEvent.KEYCODE_HOME);
                break;
            case Constants.ACTION_BACK:
                injectKey(KeyEvent.KEYCODE_BACK);
                break;
            case Constants.ACTION_RECENTS:
                injectKey(KeyEvent.KEYCODE_APP_SWITCH);
                break;
            case Constants.ACTION_LAST_APP:
                ActionUtils.switchToLastApp(mContext, ActivityManager.getCurrentUser());
                break;
            case Constants.ACTION_ASSISTANT:
                injectKey(KeyEvent.KEYCODE_ASSIST);
                break;
            case Constants.ACTION_NOTIFICATIONS:
                mContext.getSystemService(StatusBarManager.class).togglePanel();
                break;
            case Constants.ACTION_QUICK_SETTINGS:
                mContext.getSystemService(StatusBarManager.class).expandSettingsPanel();
                break;
            case Constants.ACTION_SCREENSHOT:
                injectKey(KeyEvent.KEYCODE_SYSRQ);
                break;
            case Constants.ACTION_FLASHLIGHT:
                toggleFlashlight();
                break;
            case Constants.ACTION_SLEEP:
                mPowerManager.goToSleep(SystemClock.uptimeMillis(),
                        PowerManager.GO_TO_SLEEP_REASON_POWER_BUTTON, 0);
                break;
            case Constants.ACTION_PLAY_PAUSE:
                dispatchMediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                break;
            default:
                Log.w(TAG, "Unknown action " + action);
                return;
        }
        doHapticFeedback();
    }

    private void injectKey(int keyCode) {
        final long now = SystemClock.uptimeMillis();
        final KeyEvent down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0, 0,
                KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_FROM_SYSTEM | KeyEvent.FLAG_VIRTUAL_HARD_KEY,
                InputDevice.SOURCE_KEYBOARD);
        final InputManagerGlobal im = InputManagerGlobal.getInstance();
        im.injectInputEvent(down, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        im.injectInputEvent(KeyEvent.changeAction(down, KeyEvent.ACTION_UP),
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private void dispatchMediaKey(int keyCode) {
        final MediaSessionLegacyHelper helper = MediaSessionLegacyHelper.getHelper(mContext);
        if (helper == null) {
            return;
        }
        final long now = SystemClock.uptimeMillis();
        final KeyEvent down = new KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0);
        helper.sendMediaButtonEvent(down, true);
        helper.sendMediaButtonEvent(KeyEvent.changeAction(down, KeyEvent.ACTION_UP), true);
    }

    private void toggleFlashlight() {
        final String cameraId = getRearCameraId();
        if (cameraId == null) {
            return;
        }
        try {
            mCameraManager.setTorchMode(cameraId, !mTorchEnabled);
            mTorchEnabled = !mTorchEnabled;
        } catch (CameraAccessException e) {
            Log.w(TAG, "Unable to toggle the flashlight", e);
        }
    }

    private String getRearCameraId() {
        if (mRearCameraId == null) {
            try {
                for (String id : mCameraManager.getCameraIdList()) {
                    final CameraCharacteristics c = mCameraManager.getCameraCharacteristics(id);
                    final Boolean flash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                    final Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                    if (Boolean.TRUE.equals(flash) && facing != null
                            && facing == CameraCharacteristics.LENS_FACING_BACK) {
                        mRearCameraId = id;
                        break;
                    }
                }
            } catch (CameraAccessException e) {
                Log.w(TAG, "Unable to find the rear camera", e);
            }
        }
        return mRearCameraId;
    }

    private void doHapticFeedback() {
        if (mVibrator == null || !mVibrator.hasVibrator()) {
            return;
        }
        if (Settings.Secure.getIntForUser(mResolver, Constants.KEY_HAPTIC, 1,
                UserHandle.USER_CURRENT) != 0) {
            mVibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        }
    }
}
