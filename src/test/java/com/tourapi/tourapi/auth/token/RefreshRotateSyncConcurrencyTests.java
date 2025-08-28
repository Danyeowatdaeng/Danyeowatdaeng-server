package com.tourapi.tourapi.auth.token;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test","test-sync"})
class RefreshRotateSyncConcurrencyTests {

    @Autowired
    private RefreshTokenStore store;

    @Test
    void concurrent_rotate_compete_sync() throws Exception {
        final int threads = 32;
        String uid = "cu1"; String sid = "cs1"; String family = "cf1";
        String raw = "raw-conc-sync";
        long now = System.currentTimeMillis() / 1000;
        store.saveOnLogin(raw, uid, sid, family, now, now + 3600);

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        List<Future<Boolean>> futures = new ArrayList<>();

        long t0 = System.nanoTime();
        for (int i = 0; i < threads; i++) {
            futures.add(pool.submit(() -> {
                ready.countDown();
                start.await();
                try {
                    store.rotate(raw, uid, sid, family);
                    return true; // one winner expected
                } catch (Exception e) {
                    return false; // losers due to REUSED/INVALID_CONTEXT
                } finally {
                    done.countDown();
                }
            }));
        }
        ready.await(5, TimeUnit.SECONDS);
        start.countDown();
        done.await(10, TimeUnit.SECONDS);
        long t1 = System.nanoTime();
        pool.shutdownNow();

        int success = 0;
        for (Future<Boolean> f : futures) if (f.get()) success++;
        double elapsedMs = (t1 - t0) / 1_000_000.0;
        System.out.println("[CONC][SYNC] threads=" + threads + ", success=" + success + ", elapsedMs=" + String.format("%.3f", elapsedMs));
        assertThat(success).isBetween(1, 1); // exactly one should succeed
    }
}


