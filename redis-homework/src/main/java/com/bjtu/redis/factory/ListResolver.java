package com.bjtu.redis.factory;

import com.bjtu.redis.domain.CounterSpec;
import redis.clients.jedis.Jedis;

import java.util.List;

public class ListResolver implements TypeResolver{

    private CounterSpec counterSpec;
    private Jedis jedis;

    @Override
    public String resolve() {
        String res = "没有进行有效操作";
        String key = counterSpec.getKeyFields();
        String value = counterSpec.getValueFields();
        int expireTime = counterSpec.getExpireTime();
        if(key != null) {
            if(jedis.exists(key)) {
                if(jedis.type(key).equals("list")) {
                    if(value != null) {
                        if(expireTime != 0) {
                            jedis.lpush(key, value);
                            jedis.expire(key, expireTime);
                            res = "键：" + key + "中列表添加新值：" + value + "，key的过期时间为" + jedis.ttl(key);
                        } else {
                            jedis.lpush(key, value);
                            res = "键：" + key + "中列表添加新值：" + value;
                        }
                    } else {
                        if(expireTime != 0) {
                            jedis.expire(key, expireTime);
                            List<String> list = jedis.lrange(key, 0, -1);
                            res = "键：" + key + "中列表的值如下：";
                            for(int i = 0; i < list.size(); i++)
                                res += list.get(i) + " ";
                        }
                    }
                }
            } else {
                if(value != null) {
                    if(expireTime != 0) {
                        jedis.lpush(key, value);
                        jedis.expire(key, expireTime);
                        res = "键：" + key + "中列表添加新值：" + value + "，key的过期时间为：" + expireTime;
                    } else {
                        jedis.lpush(key, value);
                        res = "键：" + key + "中列表添加新值：" + value;
                    }
                }
            }
        }
        return res;
    }

    @Override
    public void setData(CounterSpec counterSpec, Jedis jedis) {
        this.counterSpec = counterSpec;
        this.jedis = jedis;
    }
}
