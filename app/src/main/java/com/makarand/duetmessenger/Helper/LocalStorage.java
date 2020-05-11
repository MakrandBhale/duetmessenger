package com.makarand.duetmessenger.Helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;

import com.google.gson.Gson;
import com.makarand.duetmessenger.Model.Couple;
import com.makarand.duetmessenger.Model.User;

public class LocalStorage {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public LocalStorage(Context context){
        sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);

    }

    public void setString(String key, String val){
        editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.apply();
    }

    public void setUserObject(String objectKey, User obj){
        editor = sharedPreferences.edit();
        String objectString = new Gson().toJson(obj);
        editor.putString(objectKey, objectString);
        editor.apply();
    }

    public void setBoolean(String key, boolean value){
        editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    public User getUserObject(String key){
        String object = sharedPreferences.getString(key, null);
        if(object == null ) return null;
        return new Gson().fromJson(object, User.class);
    }

    public void setCoupleObject(String key, Couple obj){
        editor = sharedPreferences.edit();
        String objectString = new Gson().toJson(obj);
        editor.putString(key, objectString);
        editor.apply();
    }
    public Couple getCoupleObject(String key){
        String object = sharedPreferences.getString(key, null);
        if(object == null ) return null;
        return new Gson().fromJson(object, Couple.class);
    }
    public String getString(String key){
        return sharedPreferences.getString(key, null);
    }

}
