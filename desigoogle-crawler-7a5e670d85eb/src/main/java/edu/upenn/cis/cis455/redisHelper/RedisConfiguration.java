package edu.upenn.cis.cis455.redisHelper;

public class RedisConfiguration {

    public String[] hostNames;
    public String redisMasterName;
    public int redisDB=0;

    public RedisConfiguration(String[] hostNames, String redisMasterName, int redisDB) {
        this.hostNames = hostNames;
        this.redisMasterName = redisMasterName;
        this.redisDB = redisDB;
    }

    public String[] getHostNames() {
        return hostNames;
    }

    public void setHostNames(String[] hostNames) {
        this.hostNames = hostNames;
    }

    public String getRedisMasterName() {
        return redisMasterName;
    }

    public void setRedisMasterName(String redisMasterName) {
        this.redisMasterName = redisMasterName;
    }

    public int getRedisDB() {
        return redisDB;
    }

    public void setRedisDB(int redisDB) {
        this.redisDB = redisDB;
    }
}
