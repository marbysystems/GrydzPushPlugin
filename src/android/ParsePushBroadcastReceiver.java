package org.apache.cordova.core;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

public class ParsePushBroadcastReceiver extends com.parse.ParsePushBroadcastReceiver {

    private final static String TAG = ParsePushBroadcastReceiver.class.getSimpleName();
    public static int receivedGrydz = 0;
    public static int receivedReplies = 0;

    public ParsePushBroadcastReceiver() {
      super();
    }

    public static void clearMessages() {
      receivedGrydz = 0;
      receivedReplies = 0;
    }

    private static Map<String, String> getParseExtras(Intent intent) {
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString(KEY_PUSH_DATA));
            Map<String, String> extras = new HashMap<String, String>();
            Iterator keys = json.keys();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                extras.put(key, (String) json.get(key));
            }
            return extras;
        } catch (JSONException je) {
            Log.e(TAG, "Error getting extras from intent", je);
            return null;
        }
    }

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        Map<String, String> parseExtras = getParseExtras(intent);

        Log.w(TAG, "Recieved notification, logging data");
        for (Map.Entry<String, String> entry : parseExtras.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            Log.w(TAG, key + " : " + value);
        }

        String type = parseExtras.get("type");
        if (type.equals("NEW_GRYD")) {
            receivedGrydz++;
        } else {
            receivedReplies++;
        }

        Log.w(TAG, String.valueOf(receivedGrydz) + " new grydz");
        Log.w(TAG, String.valueOf(receivedReplies) + " new replies");

        Notification notification = getNotification(context, intent);
        if (notification != null) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notify(1001, notification);
        }
    }

    @Override
    protected void onPushDismiss(Context context, Intent intent) {
        clearMessages();
        super.onPushDismiss(context, intent);
    }

    @Override
    protected void onPushOpen(Context context, Intent intent) {
        clearMessages();
        super.onPushOpen(context, intent);
    }

    @Override
    protected Notification getNotification(Context context, Intent intent) {
        if (ParsePlugin.isPushEnabled == false) {
            return null;
        }

        Bundle extras = intent.getExtras();
        Random random = new Random();
        int contentIntentRequestCode = random.nextInt();
        int deleteIntentRequestCode = random.nextInt();
        String packageName = context.getPackageName();

        Intent contentIntent = new Intent(ACTION_PUSH_OPEN);
        contentIntent.putExtras(extras);
        contentIntent.setPackage(packageName);

        Intent deleteIntent = new Intent(ACTION_PUSH_DELETE);
        deleteIntent.putExtras(extras);
        deleteIntent.setPackage(packageName);

        PendingIntent pContentIntent = PendingIntent.getBroadcast(context,
                contentIntentRequestCode,
                contentIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pDeleteIntent = PendingIntent.getBroadcast(context,
                deleteIntentRequestCode,
                deleteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        String contentText = "";

        if (receivedGrydz > 0) {
            contentText += "You have " + (receivedGrydz == 1 ? "a" : String.valueOf(receivedGrydz)) + " new Gryd" + (receivedGrydz > 1 ? "z" : "");

            if (receivedReplies > 0) {
                contentText += " and " + (receivedReplies == 1 ? "a reply" : String.valueOf(receivedReplies) + " replies") + " to " + (receivedReplies == 1 ? "another" : "other") + " Gryd" + (receivedReplies > 1 ? "z" : "");
            }
        }

        if (receivedGrydz == 0 && receivedReplies > 0) {
            contentText += "You have " + (receivedReplies == 1 ? "a reply to a Gryd" : String.valueOf(receivedReplies) + " replies to Grydz");
        }

        return new Notification.Builder(context)
            .setAutoCancel(true)
            .setContentIntent(pContentIntent)
            .setContentText(contentText)
            .setContentTitle("BFFB")
            .setDeleteIntent(pDeleteIntent)
            .setSmallIcon(this.getSmallIconId(context, intent))
            .setLights(0xffb081d8, 1000, 2000)
            .setStyle(new Notification.BigTextStyle().bigText(contentText))
            .build();
    }
}
