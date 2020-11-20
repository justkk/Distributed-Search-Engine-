package storage;

import redis.clients.jedis.JedisPool;

public class StoragePool {
    private static JedisPool jedisPool;

    public static JedisPool getPoolInstance() {
        if (StoragePool.jedisPool == null)
            StoragePool.jedisPool = new JedisPool();
        return StoragePool.jedisPool;
    }

    public static void addKey(String key) {
        JedisPool instance = StoragePool.getPoolInstance();
        
    }
}
