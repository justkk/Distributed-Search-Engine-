package edu.upenn.cis.cis455.redisHelper;

import com.github.jedis.lock.JedisLock;
import redis.clients.jedis.Jedis;

public class RedisLockManager {
    Jedis redis;
    JedisLock jedisLock;

    public Jedis getRedis() {
        return redis;
    }

    public void setRedis(Jedis redis) {
        this.redis = redis;
    }

    public JedisLock getJedisLock() {
        return jedisLock;
    }

    public void setJedisLock(JedisLock jedisLock) {
        this.jedisLock = jedisLock;
    }
}
