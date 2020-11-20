package edu.upenn.cis.cis455.redisHelper;

import com.github.jedis.lock.JedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RedisDBManager {
    private static final Logger logger = LoggerFactory.getLogger(RedisDBManager.class);
    private static final int DEFAULT_TIMEOUT = 5000;

    private JedisSentinelPool redisSentinelPool;
    private Map<String, String> scriptShaCache = new ConcurrentHashMap<String, String>();
    private Set<String> redisSentinels = new HashSet<>();
    private String redisMasterName;
    private static final JedisPoolConfig poolConfig = new JedisPoolConfig();
    private int dbnum = 0;
    private static final int MAXTOTAL = 100;
    private static final int MAXIDLE = 100;

    public RedisDBManager(RedisConfiguration conf) {
        this(conf.hostNames, conf.redisMasterName, conf.redisDB);
    }

    public RedisDBManager(String[] sentinelHostNames, String redisMasterName, int dbnum) {
        this.redisMasterName = redisMasterName;
        this.dbnum = dbnum;
        for (String sentinelHost : sentinelHostNames) {
            this.redisSentinels.add(sentinelHost.trim());
        }
        poolConfig.setMaxTotal(MAXTOTAL);
        poolConfig.setMaxIdle(MAXIDLE);
        redisSentinelPool = new JedisSentinelPool(redisMasterName, this.redisSentinels, poolConfig, DEFAULT_TIMEOUT, null, dbnum);
    }

    private Jedis getResource() {
        return redisSentinelPool.getResource();
    }


    public RedisLockManager getLock(String lockKey) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        RedisLockManager redisLockManager = new RedisLockManager();
        try {
            redis = getResource();
            if (redis != null) {
                redisLockManager.setJedisLock(new JedisLock(redis, lockKey, 10000, 30000));
                redisLockManager.setRedis(redis);
            }
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                if (redis != null) {
                    redisLockManager.setJedisLock(new JedisLock(redis, lockKey, 10000, 30000));
                    redisLockManager.setRedis(redis);
                }
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception ex) {
            logger.error("Exception : ", ex);
            return null;
        }
        return redisLockManager;
    }

    public void returnResouce(RedisLockManager redisLockManager) {
        returnResource(redisLockManager.getRedis());
    }

    private void returnResource(Jedis redis) {
        redisSentinelPool.returnResource(redis);
    }

    public void createNewConnectionPool(JedisSentinelPool localPool) {
        synchronized (RedisDBManager.class) {
            if (localPool == this.redisSentinelPool) {
                redisSentinelPool.destroy();
                redisSentinelPool = new JedisSentinelPool(redisMasterName, redisSentinels, poolConfig, DEFAULT_TIMEOUT, null, dbnum);
                logger.info("Allocated new JedisSentinelPool");
            } else
                logger.info("Another Thread created a new JedisSentinelPool. Hence ignoring");
        }
    }

    public Set<String> hkeys(String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hkeys(field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hkeys(field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }


    public List<String> hvals(String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hvals(field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hvals(field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long setExpiration(String key, int seconds) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.expire(key, seconds);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.expire(key, seconds);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String getKey(String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.get(field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.get(field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> getKeys(String pattern) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.keys(pattern);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.keys(pattern);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String setKey(String field, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.set(field, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.set(field, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String renameKey(String oldkey, String newkey) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.rename(oldkey, newkey);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.rename(oldkey, newkey);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long setKeyNx(String field, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.setnx(field, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.setnx(field, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String setKeyEx(String key, String value, int ttl) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.setex(key, ttl, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.setex(key, ttl, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public List<String> hmget(String key, String[] fields) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            List<String> values = redis.hmget(key, fields);
            return values;
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                List<String> values = redis.hmget(key, fields);
                return values;
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String hget(String key, String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hget(key, field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hget(key, field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Map<String, String> hgetAll(String key) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hgetAll(key);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hgetAll(key);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void hrem(String key, String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            redis.hdel(key, field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                redis.hdel(key, field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String hmset(String key, Map<String, String> map) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hmset(key, map);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hmset(key, map);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long hset(String key, String field, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hset(key, field, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hset(key, field, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long hdel(String key, String field) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hdel(key, field);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hdel(key, field);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public boolean hsetnx(String key, String field, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return 1 == redis.hsetnx(key, field, value).longValue();
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return 1 == redis.hsetnx(key, field, value).longValue();
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return false;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long hlen(String mapName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hlen(mapName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hlen(mapName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void hclear(String key) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            Set<String> fields = redis.hkeys(key);
            for (Iterator<String> fieldsItr = fields.iterator(); fieldsItr.hasNext(); ) {
                String field = fieldsItr.next();
                redis.hdel(key, field);
            }

        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                Set<String> fields = redis.hkeys(key);
                for (Iterator<String> fieldsItr = fields.iterator(); fieldsItr.hasNext(); ) {
                    String field = fieldsItr.next();
                    redis.hdel(key, field);
                }
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long hincrBy(String key, String field, long value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.hincrBy(key, field, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.hincrBy(key, field, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long incrBy(String key, long value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.incrBy(key, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.incrBy(key, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> zrembyRange(String key, long start, long end) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        Set<String> vals;
        try {
            redis = getResource();
            vals = redis.zrangeByScore(key, start, end);
            redis.zremrangeByScore(key, start, end);
            return vals;
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                vals = redis.zrangeByScore(key, start, end);
                redis.zremrangeByScore(key, start, end);
                return vals;
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void zadd(String key, Map<String, Double> scoreMembers) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            redis.zadd(key, scoreMembers);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                redis.zadd(key, scoreMembers);
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long zcard(String key) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zcard(key);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zcard(key);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0l;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0l;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void zrem(String key, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            redis.zrem(key, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                redis.zrem(key, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> zrange(String key, int min, int max) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zrange(key, min, max);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zrange(key, min, max);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> zrevrange(String key, int min, int max) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zrevrange(key, min, max);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zrevrange(key, min, max);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> zrevrangebyscoreWithLimit(String aKey, double aScore, int aOffset, int aCount) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zrevrangeByScore(aKey, aScore, 0, aOffset, aCount);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zrevrangeByScore(aKey, aScore, 0, aOffset, aCount);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Double zscore(String key, String member) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zscore(key, member);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zscore(key, member);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long zcount(String setName, double min, double max) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zcount(setName, min, max);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zcount(setName, min, max);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0l;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0l;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long zrevrank(String key, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.zrevrank(key, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.zrevrank(key, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Map<String, String> zallWithScores(String key) {
        List<String> keys = new ArrayList<String>();
        keys.add(key);

        List<String> args = new ArrayList<String>();

        return ((Map<String, String>) runLuaScript("lua-scripts/zall.lua", keys, args));
    }

    public Long sAdd(String setName, String member) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sadd(setName, member);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sadd(setName, member);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long sAdd(String setName, String[] members) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sadd(setName, members);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sadd(setName, members);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public boolean sIsMember(String setName, String member) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sismember(setName, member);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sismember(setName, member);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return false;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return false;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> sUnion(String... setNames) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sunion(setNames);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sunion(setNames);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> sDiff(String setName1, String setName2) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sdiff(setName1, setName2);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sdiff(setName1, setName2);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> sDiff(String... sets) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.sdiff(sets);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.sdiff(sets);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String mset(Map<String, Long> keyVals) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            String[] data = new String[keyVals.size() * 2];
            int i = 0;
            for (Map.Entry<String, Long> e : keyVals.entrySet()) {
                data[i] = e.getKey();
                data[i + 1] = "" + e.getValue();
                i = i + 2;
            }
            return redis.mset(data);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                String[] data = new String[keyVals.size() * 2];
                int i = 0;
                for (Map.Entry<String, Long> e : keyVals.entrySet()) {
                    data[i] = e.getKey();
                    data[i + 1] = "" + e.getValue();
                    i = i + 2;
                }
                return redis.mset(data);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public List<String> mget(String... keys) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.mget(keys);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.mget(keys);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long sRem(String setName, String member) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.srem(setName, member);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.srem(setName, member);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long rpush(String listName, String... members) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.rpush(listName, members);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.rpush(listName, members);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String rpop(String listName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.rpop(listName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.rpop(listName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String ltrim(String listName, long startIndex, long endIndex) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.ltrim(listName, startIndex, endIndex);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.ltrim(listName, startIndex, endIndex);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long lrem(String listName, long direction, String value) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.lrem(listName, direction, value);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.lrem(listName, 1, value);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0l;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0l;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String lindex(String listName, Long index) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.lindex(listName, index);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.lindex(listName, index);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Long llen(String listName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.llen(listName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.llen(listName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public List<String> lrange(String listName, long start, long end) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.lrange(listName, start, end);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.lrange(listName, start, end);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String get(String keyName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.get(keyName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.get(keyName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public Set<String> smembers(String setName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.smembers(setName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.smembers(setName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long scard(String setName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.scard(setName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.scard(setName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0l;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0l;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long lpush(String listName, String... vals) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.lpush(listName, vals);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.lpush(listName, vals);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return 0l;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 0l;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String lpop(String listName) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.lpop(listName);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.rpop(listName);
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void delete(String key) {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            redis.del(key);
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                redis.del(key);
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public long incrByEx(String key, int val, int ex) {
        List<String> keys = new ArrayList<String>();
        keys.add(key);

        List<String> args = new ArrayList<String>();
        args.add("" + val);
        args.add("" + ex);

        return (Long) runLuaScript("lua-scripts/incr_ex_and_get.lua", keys, args);
    }

    public long decrIfExistsAndNonNegativeAfterOperation(String key, int val) {
        List<String> keys = new ArrayList<String>();
        keys.add(key);

        List<String> args = new ArrayList<String>();
        args.add("" + val);

        return (Long) runLuaScript("lua-scripts/decr_ifexists_and_after_operation_nonnegative_number.lua", keys, args);
    }

    private Object runLuaScript(String scriptFileName, List<String> keys, List<String> args)
            throws RuntimeException {
        if (scriptFileName == null || scriptFileName.isEmpty()) {
            throw new RuntimeException("Cannot load lua script:" + scriptFileName);
        }

        /* cache the script in redis */
        Jedis redis = null;
        try {
            String sha = scriptShaCache.get(scriptFileName);
            if (sha == null) {
                /* cache the script */
                BufferedReader reader = null;
                InputStream scriptIn = null;
                try {
                    scriptIn = getClass().getResourceAsStream("/" + scriptFileName);
                    StringBuffer script = new StringBuffer(scriptIn.available());
                    reader = new BufferedReader(new InputStreamReader(scriptIn));
                    String s;
                    while ((s = reader.readLine()) != null) {
                        script.append(s).append(" ");
                    }

                    String scriptString = script.toString();

                    redis = getResource();
                    Object returnValue = redis.eval(scriptString, keys, args);
                    sha = toSHA1(scriptString.getBytes());
                    scriptShaCache.put(scriptFileName, sha);

                    logger.info("Caching script {} {}", scriptFileName, sha);
                    return returnValue;
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred in reading script" + e, e);
                } finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e) {
                            logger.warn("Error closing reader.", e);
                        }
                    }
                    if (scriptIn != null) {
                        try {
                            scriptIn.close();
                        } catch (IOException e) {
                            logger.warn("Error closing inputstream.", e);
                        }
                    }
                }
            } else {
                redis = getResource();
                //logger.debug("Redis : {} :: {} :: {} ", scriptShaCache.get(scriptFileName), keys, args);
                return redis.evalsha(sha, keys, args);
            }
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    private static String toSHA1(byte[] script) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            return byteArrayToHexString(md.digest(script));
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA1 computation failed for script:" + script, e);
            throw new RuntimeException("SHA1 computation failed for script:" + script);
        }
    }

    private static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public void flushDB() {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            redis.flushDB();
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                redis.flushDB();
            } catch (Exception e) {
                logger.error("Exception: ", e);
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public String info() {
        JedisSentinelPool localPool = redisSentinelPool;
        Jedis redis = null;
        try {
            redis = getResource();
            return redis.info();
        } catch (JedisConnectionException jex) {
            logger.error("Creating new connection since it encountered Jedis Connection Exception: ", jex);
            createNewConnectionPool(localPool);
            try {
                redis = getResource();
                return redis.info();
            } catch (Exception e) {
                logger.error("Exception: ", e);
                return null;
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return null;
        } finally {
            if (redis != null) {
                returnResource(redis);
            }
        }
    }

    public void shutdown() {
        redisSentinelPool.destroy();
    }
}