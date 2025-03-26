package com.example.myapplication.model;

import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

public class RingtoneHelper {
    private static Ringtone ringtone;

    /**
     * Plays the user's ringtone to help wake them up for an alarm
     * @param context --> the context that allows the ringtone to be played
     */
    public static void playRingtone(Context context) {
        Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alarmUri == null) {
            alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
        ringtone = RingtoneManager.getRingtone(context, alarmUri);
        if (ringtone != null && !ringtone.isPlaying()) {
            ringtone.play();
        }
    }

    /**
     * Stops the user's ringtone
      */
    public static void stopRingtone() {
        if (ringtone != null && ringtone.isPlaying()) {
            ringtone.stop();
        }
    }
}

