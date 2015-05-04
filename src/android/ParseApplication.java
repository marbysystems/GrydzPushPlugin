package org.apache.cordova.core;

import android.app.Application;
import android.content.Context;

import com.parse.Parse;
import com.parse.ParseInstallation;
import com.parse.PushService;
import com.parse.ParsePush;

import au.com.marby.grydz.MainActivity;

public class ParseApplication extends Application
{
	private static ParseApplication instance = new ParseApplication();

	public ParseApplication() {
		instance = this;
	}

	public static Context getContext() {
		return instance;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// register device for parse
		Parse.initialize(this, "X8KCzxkGGX15zmbwny4zSqUaB5gl1tOWS5lmcZc6", "icM5CdrtrDErQ4NwvtH2CnafbioOLm4qunnyQloQ");
    ParsePush.subscribeInBackground("");
		PushService.setDefaultPushCallback(this, MainActivity.class);
		ParseInstallation.getCurrentInstallation().saveInBackground();
	}
}
