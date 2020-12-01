package com.bjtu.redis.factory;

import com.bjtu.redis.domain.CounterSpec;
import redis.clients.jedis.Jedis;

import java.text.ParseException;

public interface TypeResolver {
    String resolve() throws ParseException;

    void setData(CounterSpec counterSpec, Jedis jedis);
}
