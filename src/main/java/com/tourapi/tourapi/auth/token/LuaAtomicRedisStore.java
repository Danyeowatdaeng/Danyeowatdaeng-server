package com.tourapi.tourapi.auth.token;

import com.tourapi.tourapi.common.exception.general.GeneralException;
import com.tourapi.tourapi.common.exception.token.status.TokenErrorStatus;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Instant;
import java.util.*;

import static com.tourapi.tourapi.auth.token.KeyNames.rtIndex;
import static com.tourapi.tourapi.auth.token.KeyNames.rtSession;
import static com.tourapi.tourapi.auth.token.KeyNames.userSessions;

public class LuaAtomicRedisStore implements RefreshTokenStore {
    private final RedisTemplate<String, String> redisTemplate;
    private final TokenHasher tokenHasher;
    private final DefaultRedisScript<String> rotateScript;
    private final long ttlSeconds;

    public LuaAtomicRedisStore(RedisTemplate<String, String> redisTemplate,
                               TokenHasher tokenHasher,
                               DefaultRedisScript<String> rotateScript,
                               long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.tokenHasher = tokenHasher;
        this.rotateScript = rotateScript;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public void saveOnLogin(String refreshRaw, String uid, String sid, String familyId, long iat, long exp) {
        String tokenHash = tokenHasher.hmacSha256Hex(refreshRaw);
        String indexKey = rtIndex(tokenHash);
        String sessionKey = rtSession(uid, sid);
        String userSessionsKey = userSessions(uid);

        HashOperations<String, Object, Object> h = redisTemplate.opsForHash();
        ZSetOperations<String, String> z = redisTemplate.opsForZSet();

        Map<String, Object> indexFields = new HashMap<>();
        indexFields.put("uid", uid);
        indexFields.put("sid", sid);
        indexFields.put("familyId", familyId);
        indexFields.put("iat", Long.toString(iat));
        indexFields.put("exp", Long.toString(exp));
        indexFields.put("rotated", "0");
        h.putAll(indexKey, indexFields);
        redisTemplate.expire(indexKey, java.time.Duration.ofSeconds(ttlSeconds));

        Map<String, Object> sessionFields = new HashMap<>();
        sessionFields.put("tokenHash", tokenHash);
        sessionFields.put("familyId", familyId);
        sessionFields.put("iat", Long.toString(iat));
        sessionFields.put("exp", Long.toString(exp));
        h.putAll(sessionKey, sessionFields);
        redisTemplate.expire(sessionKey, java.time.Duration.ofSeconds(ttlSeconds));

        z.add(userSessionsKey, sid, Instant.now().toEpochMilli());

        Long count = z.zCard(userSessionsKey);
        if (count != null && count > 5) {
            Set<String> toRemove = z.range(userSessionsKey, 0, count - 6);
            if (toRemove != null) {
                for (String oldSid : toRemove) {
                    String oldSessionKey = rtSession(uid, oldSid);
                    String oldTokenHash = (String) h.get(oldSessionKey, "tokenHash");
                    if (oldTokenHash != null) {
                        redisTemplate.delete(rtIndex(oldTokenHash));
                    }
                    redisTemplate.delete(oldSessionKey);
                }
                z.remove(userSessionsKey, toRemove.toArray());
            }
        }
    }

    @Override
    public RotateResult rotate(String oldRefreshRaw, String uid, String sid, String familyId) {
        String oldHash = tokenHasher.hmacSha256Hex(oldRefreshRaw);
        String indexKey = rtIndex(oldHash);
        HashOperations<String, Object, Object> h = redisTemplate.opsForHash();
        Boolean exists = redisTemplate.hasKey(indexKey);
        if (exists == null || !exists) {
            throw new GeneralException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }
        String storedUid = (String) h.get(indexKey, "uid");
        String storedSid = (String) h.get(indexKey, "sid");
        String storedFamily = (String) h.get(indexKey, "familyId");
        if (!Objects.equals(uid, storedUid) || !Objects.equals(sid, storedSid) || !Objects.equals(familyId, storedFamily)) {
            throw new GeneralException(TokenErrorStatus.INVALID_CONTEXT);
        }

        String newRaw = UUID.randomUUID().toString();
        String newHash = tokenHasher.hmacSha256Hex(newRaw);
        String newIndexKey = rtIndex(newHash);
        String sessionKey = rtSession(uid, sid);

        List<String> keys = Arrays.asList(indexKey, newIndexKey, sessionKey);
        List<String> args = Arrays.asList(Long.toString(ttlSeconds), newHash);
        String result = redisTemplate.execute(rotateScript, keys, args.toArray());

        if ("OK".equals(result)) {
            return new RotateResult(newRaw, newHash, uid, sid, familyId);
        } else if ("REUSED".equals(result)) {
            invalidateFamily(uid, familyId);
            throw new GeneralException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        } else if ("MISSING_OLD".equals(result)) {
            throw new GeneralException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
        }
        throw new GeneralException(TokenErrorStatus.INVALID_REFRESH_TOKEN);
    }

    @Override
    public void invalidateFamily(String uid, String familyId) {
        ZSetOperations<String, String> z = redisTemplate.opsForZSet();
        HashOperations<String, Object, Object> h = redisTemplate.opsForHash();
        String userSessionsKey = userSessions(uid);
        Set<String> sids = z.range(userSessionsKey, 0, -1);
        if (sids == null) return;
        for (String sid : sids) {
            String sessionKey = rtSession(uid, sid);
            String family = (String) h.get(sessionKey, "familyId");
            if (Objects.equals(familyId, family)) {
                String tokenHash = (String) h.get(sessionKey, "tokenHash");
                if (tokenHash != null) redisTemplate.delete(rtIndex(tokenHash));
                redisTemplate.delete(sessionKey);
                z.remove(userSessionsKey, sid);
            }
        }
    }
}


