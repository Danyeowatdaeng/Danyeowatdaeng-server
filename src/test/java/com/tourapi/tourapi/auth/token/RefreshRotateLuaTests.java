package com.tourapi.tourapi.auth.token;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test","test-lua"})
class RefreshRotateLuaTests {

    @Autowired
    private RefreshTokenStore store;

    @Test
    void rotate_success_lua() {
        String uid = "u3"; String sid = "s3"; String family = "f3";
        String raw = "raw-token-3";
        long now = System.currentTimeMillis() / 1000;
        store.saveOnLogin(raw, uid, sid, family, now, now + 3600);
        RefreshTokenStore.RotateResult r = store.rotate(raw, uid, sid, family);
        assertThat(r.newRefreshRaw()).isNotBlank();
        assertThat(r.tokenHash()).isNotBlank();
    }

    @RepeatedTest(3)
    void rotate_reuse_should_fail_lua() {
        String uid = "u4"; String sid = "s4"; String family = "f4";
        String raw = "raw-token-4";
        long now = System.currentTimeMillis() / 1000;
        store.saveOnLogin(raw, uid, sid, family, now, now + 3600);
        store.rotate(raw, uid, sid, family);
        assertThatThrownBy(() -> store.rotate(raw, uid, sid, family))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rotate_performance_lua() {
        String uid = "perfU"; String sid = "perfS"; String family = "perfF";
        final int warmup = 5;
        final int iterations = 500;
        long totalNs = 0L;

        for (int i = 0; i < warmup + iterations; i++) {
            String raw = "raw-" + i + "-" + System.nanoTime();
            long now = System.currentTimeMillis() / 1000;
            store.saveOnLogin(raw, uid, sid, family, now, now + 3600);
            long t0 = System.nanoTime();
            store.rotate(raw, uid, sid, family);
            long t1 = System.nanoTime();
            if (i >= warmup) totalNs += (t1 - t0);
        }
        double avgMs = (totalNs / (double) iterations) / 1_000_000.0;
        System.out.println("[PERF][LUA] rotate avg ms = " + String.format("%.3f", avgMs));
        assertThat(avgMs).isGreaterThanOrEqualTo(0.0);
    }
}


