/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.blurdefault;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

/**
 * Window blurs are expensive on these GPUs: start with them disabled, but
 * only while the user has never set the option, so turning them back on in
 * Settings sticks across reboots and updates.
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ContentResolver resolver = context.getContentResolver();
        if (Settings.Global.getString(resolver, Settings.Global.DISABLE_WINDOW_BLURS) == null) {
            Settings.Global.putInt(resolver, Settings.Global.DISABLE_WINDOW_BLURS, 1);
        }
    }
}
