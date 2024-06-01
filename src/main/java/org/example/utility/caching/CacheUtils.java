package org.example.utility.caching;

import com.christ.utility.lib.vault.VaultUtils;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.ReplicatedServersConfig;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CacheUtils {
    private RedissonClient _redisson;

    private final static String[] RedisNodes;
    private final static String RedisPassword;
    public final static CacheUtils instance = new CacheUtils();
    static {
        String urls_in_csv = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG, "redis.server.url").getValue();
        //String urls_in_csv = "redis://10.5.13.210:6379";
        if(urls_in_csv != null && urls_in_csv.trim().length() > 0) {
            RedisNodes = urls_in_csv.split(",");
            RedisPassword = VaultUtils.instance.get(VaultUtils.CONFIG_SECTION_APP_CONFIG, "redis.server.password").getValue();
        }
        else {
            RedisNodes = null;
            RedisPassword = null;
        }
    }

    public CacheUtils() {
    }

    public void set(String key, String value) {
        this.set("_", key, value);
    }
    public void set(Map<String, String> items) {
        this.set("_", items);
    }
    public void set(String mapName, String key, String value) {
        this.initIfRequired();

        key = getKey(mapName, key);
        this._redisson.getMap(mapName).put(key, value);
    }
    public void set(String mapName, Map<String, String> items) {
        this.initIfRequired();

        Map<String, String> refinedItems = new HashMap<String, String>();
        Iterator<String> iterator = items.keySet().iterator();
        while(iterator.hasNext()) {
            String key = iterator.next();
            refinedItems.put(this.getKey(mapName, key), items.get(key));
        }
        this._redisson.getMap(mapName).putAll(refinedItems);
    }
    public String get(String mapName, String key) {
        this.initIfRequired();

        key = getKey(mapName, key);
        String value = null;
        try {
            value = this._redisson.getMap(mapName).get(key).toString();
        }
        catch(Exception ex) { }
        return value;
    }
    public Map<String, String> get(String mapName, List<String> keys) {
        this.initIfRequired();

        Map<String, String> items = new HashMap<String, String>();
        try {
            RMap<String, String> map = this._redisson.getMap(mapName);
            for(String key : keys) {
                key = getKey(mapName, key);
                items.put(key, map.get(key));
            }
        }
        catch(Exception ex) { }
        return items;
    }
    public String get(String key) {
        return this.get("_", key);
    }
    public Map<String, String> get(List<String> keys) {
        return this.get("_", keys);
    }

    public void clearMap(String mapName) {
        this.initIfRequired();
        this._redisson.getMap(mapName).clear();
    }
    public void clearAllMap() {
        this._redisson.shutdown();
    }
    public void clear(String mapName, String key) {

    }
    public void clearKey(String mapName, String key) {
        try {
            RMap<String, String> map = this._redisson.getMap(mapName);
            Map<String,String> tempMap = new HashMap<>();
            tempMap.putAll(map);
            tempMap.keySet().removeIf(key1 -> key1.contains(key));
            this._redisson.getMap(mapName).clear();
            this._redisson.getMap(mapName).putAll(tempMap);
        }
        catch(Exception ex) { }
    }

    private void initIfRequired() {
        if(this._redisson == null) {
            Config config = new Config();
            ReplicatedServersConfig replicatedServersConfig = config.useReplicatedServers().setScanInterval(2000)
                    .setMasterConnectionPoolSize(10).setMasterConnectionMinimumIdleSize(10).setSlaveConnectionPoolSize(10)
                    .setSlaveConnectionMinimumIdleSize(10).setTimeout(3000).setRetryAttempts(1).setRetryInterval(2000);
            String[] redisNodes = CacheUtils.RedisNodes;
            replicatedServersConfig.addNodeAddress(redisNodes);
            if(CacheUtils.RedisPassword!=null && CacheUtils.RedisPassword.trim().length()>0) {
                replicatedServersConfig.setPassword(CacheUtils.RedisPassword);
            }
            this._redisson = Redisson.create(config);
        }
    }
    private String getKey(String mapName, String key) {
        if(mapName == null || mapName.trim().length() == 0) {
            return key.trim();
        }
        return String.format("%s-%s", mapName.trim(), key.trim());
    }

    public void testMethod() {
        this.initIfRequired();
    }

    public void setMultiMap(String mapName, String key, String value) {
        this.initIfRequired();
        key = this.getKey(mapName, key);
        this._redisson.getSetMultimap(mapName);
        this._redisson.getSetMultimap(mapName).put(key,value);
    }

    public RSet<Object> getMultiMap(String mapName, String key) {
        this.initIfRequired();
        key = this.getKey(mapName, key);
        this._redisson.getSetMultimap(mapName);
        return this._redisson.getSetMultimap(mapName).get(key);
    }
}
