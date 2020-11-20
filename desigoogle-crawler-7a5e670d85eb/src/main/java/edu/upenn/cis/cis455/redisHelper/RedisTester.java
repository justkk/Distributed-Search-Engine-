package edu.upenn.cis.cis455.redisHelper;

import edu.upenn.cis.cis455.ConstantsHW2;

public class RedisTester {

    public static void main(String[] args) {

        RedisDBManager redisDBManager = new RedisDBManager(ConstantsHW2.getInstance().getRedisConfiguration());
        redisDBManager.setKey("nikhil", "kk");
        System.out.println(redisDBManager.get("nikhils"));
        redisDBManager.shutdown();
    }
}
