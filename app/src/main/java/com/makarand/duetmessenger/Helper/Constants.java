package com.makarand.duetmessenger.Helper;

public class Constants {
    public static final String USERS_TREE = "users";
    public static final String CHATROOMS_TREE = "chatrooms";

    public static final String AVTAR_LOCAL_KEY = "avtar_storage";
    public static final String SHARED_PREFERENCES_KEY = "localstorage";
    public static final int ONLINE = 0;
    public static final int OFFLINE = -1;
    public static final int TYPING = 1;
    public static final int TYPING_TIMEOUT = 1;

    public static final String TYPING_MESSAGE_TYPE = "typing";

    public static final int MESSAGE_STATUS_SENDING = -2;
    public static final int MESSAGE_STATUS_SENT = -1;
    public static final int MESSAGE_STATUS_DELIVERED = 0;
    public static final int MESSAGE_STATUS_READ = 1;
    public static final long LATE_REPLY_TIMEOUT = 1;
    public static final int MESSAGE_LIMIT = 100;
    public static final String MY_OBJECT_LOCAL_STORAGE = "my_object";
    public static final String PARTNER_OBJECT_LOCAL_STORAGE = "partner_object";
    public static final String COUPLE_OBJECT_LOCAL_STORAGE = "couple_object";
    public static final String FREE_PASS = "free_pass";

    public static final int NORMAL_MESSAGE = 0;
    public static final int TYPING_MESSAGE = 1;

    public static final int REQUEST_OLD_MESSAGES_OFFSET = 2;


    /*Bubble Animation Constants*/
    public static final int HEART_BEAT = 1001;
    public static final int SOFT = 1002;
    public static final int ANGRY = 1003;
    public static final int EXCITED = 1004;
}
