package com.tourapi.tourapi.auth.token;

public final class KeyNames {

    private KeyNames() {}

    public static String rtIndex(String tokenHash) {
        return String.format("rt:index:%s", tokenHash);
    }

    public static String rtSession(String uid, String sid) {
        return String.format("rt:session:%s:%s", uid, sid);
    }

    public static String userSessions(String uid) {
        return String.format("user:%s:sessions", uid);
    }
}


