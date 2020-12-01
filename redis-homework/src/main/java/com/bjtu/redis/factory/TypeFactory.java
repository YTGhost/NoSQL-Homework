package com.bjtu.redis.factory;

import com.bjtu.redis.JedisInstance;
import com.bjtu.redis.domain.CounterSpec;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;

public class TypeFactory {

    private Map<String, TypeResolver> typeResolveMap = new HashMap<>();

    private Jedis jedis = JedisInstance.getInstance().getResource();

    public TypeFactory() {
        typeResolveMap.put("num", new NumResolver());
        typeResolveMap.put("freq", new FreqResolver());
        typeResolveMap.put("str", new StrResolver());
        typeResolveMap.put("list", new ListResolver());
        typeResolveMap.put("set", new SetResolver());
        typeResolveMap.put("zset", new ZsetResolver());
    }

    public TypeResolver getResolver(String type, CounterSpec counterSpec) {
        typeResolveMap.get(type).setData(counterSpec, jedis);
        return typeResolveMap.get(type);
    }

}
