/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.fpnav;

import android.util.SparseArray;

/** Shared by the settings UI and the key handler running in system_server. */
public final class Constants {

    /** Name of the input device created by the FPC fingerprint HAL. */
    public static final String INPUT_DEVICE_NAME = "uinput-fpc";

    /* Actions, stored as integers in Settings.Secure. */
    public static final int ACTION_NONE = 0;
    public static final int ACTION_HOME = 1;
    public static final int ACTION_BACK = 2;
    public static final int ACTION_RECENTS = 3;
    public static final int ACTION_LAST_APP = 4;
    public static final int ACTION_ASSISTANT = 5;
    public static final int ACTION_NOTIFICATIONS = 6;
    public static final int ACTION_QUICK_SETTINGS = 7;
    public static final int ACTION_SCREENSHOT = 8;
    public static final int ACTION_FLASHLIGHT = 9;
    public static final int ACTION_SLEEP = 10;
    public static final int ACTION_PLAY_PAUSE = 11;

    /* Settings.Secure keys. */
    public static final String KEY_TAP = "fpnav_action_tap";
    public static final String KEY_HOLD = "fpnav_action_hold";
    public static final String KEY_SWIPE_LEFT = "fpnav_action_swipe_left";
    public static final String KEY_SWIPE_RIGHT = "fpnav_action_swipe_right";
    public static final String KEY_HAPTIC = "fpnav_haptic_feedback";

    /* Scan codes sent by the FPC HAL, see configs/keylayout/uinput-fpc.kl. */
    public static final int SCAN_TAP = 616;
    public static final int SCAN_HOLD = 617;
    public static final int SCAN_SWIPE_RIGHT = 620;
    public static final int SCAN_SWIPE_LEFT = 621;

    public static final SparseArray<String> SCAN_CODE_KEYS = new SparseArray<>();
    static {
        SCAN_CODE_KEYS.put(SCAN_TAP, KEY_TAP);
        SCAN_CODE_KEYS.put(SCAN_HOLD, KEY_HOLD);
        SCAN_CODE_KEYS.put(SCAN_SWIPE_LEFT, KEY_SWIPE_LEFT);
        SCAN_CODE_KEYS.put(SCAN_SWIPE_RIGHT, KEY_SWIPE_RIGHT);
    }

    /** Action of each gesture until the user changes it, as on the stock ROM. */
    public static int getDefaultAction(String key) {
        switch (key) {
            case KEY_TAP:
                return ACTION_HOME;
            case KEY_HOLD:
                return ACTION_SLEEP;
            case KEY_SWIPE_LEFT:
                return ACTION_BACK;
            case KEY_SWIPE_RIGHT:
                return ACTION_RECENTS;
            default:
                return ACTION_NONE;
        }
    }

    private Constants() {}
}
