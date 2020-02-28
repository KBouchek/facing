package com.k.xmlrpc6;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static Preferences preferences;
    private SharedPreferences sharedPreferences;

    public static Preferences getInstance(Context context) {
        if (preferences == null) {
            preferences = new Preferences(context);
        }
        return preferences;
    }

    private Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences("xmlrpc6",Context.MODE_PRIVATE);
    }

    public void saveStringData(String key,String value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putString(key, value);
        prefsEditor.commit();
    }

    public String getStringData(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }

    public void saveIntData(String key,int value) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor .putInt(key, value);
        prefsEditor.commit();
        //.i("K", "saveing  key = "+key + " value => "+value);
    }

    public int getIntData(String key) {
        if (sharedPreferences!= null) {
            return sharedPreferences.getInt(key, -1);
        }
        return -1;
    }
}
