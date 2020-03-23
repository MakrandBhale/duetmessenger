package com.makarand.duetmessenger.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;

public class LocalStorage {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocalStorage(Context context){
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

    }

    public void storeString(String key, String val){
        editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key, null);
    }

}
