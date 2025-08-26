package com.tourapi.tourapi.web.controller.auth;

import com.tourapi.tourapi.auth.token.RefreshTokenStore;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RefreshTokenStore store;

    public AuthController(RefreshTokenStore store) {
        this.store = store;
    }

    public static class RefreshRequest { public String oldRefresh; }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody RefreshRequest body) {
        String uid = "demoUser";
        String sid = "demoSession";
        String familyId = "demoFamily";
        try {
            RefreshTokenStore.RotateResult r = store.rotate(body.oldRefresh, uid, sid, familyId);
            Map<String, String> resp = new HashMap<>();
            resp.put("accessToken", "dummy-access-token");
            resp.put("refreshToken", r.newRefreshRaw());
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException ex) {
            Map<String, String> err = new HashMap<>();
            err.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }
}
