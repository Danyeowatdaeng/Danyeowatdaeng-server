package com.tourapi.tourapi.auth.token;

public interface RefreshTokenStore {

    void saveOnLogin(String refreshRaw, String uid, String sid, String familyId, long iat, long exp);

    RotateResult rotate(String oldRefreshRaw, String uid, String sid, String familyId);

    void invalidateFamily(String uid, String familyId);

    record RotateResult(String newRefreshRaw, String tokenHash, String uid, String sid, String familyId) {}
}


