package com.bjtu.redis.factory;

import com.bjtu.redis.domain.CounterSpec;
import redis.clients.jedis.Jedis;

public class NumResolver implements TypeResolver{

    private CounterSpec counterSpec;
    private Jedis jedis;

    @Override
    public String resolve() {
        String res = "没有进行有效操作";
        String key = counterSpec.getKeyFields();
        String value = counterSpec.getValueFields();
        int expireTime = counterSpec.getExpireTime();
        // 有keyFields字段时
        if(key != null) {
            // key在redis中存在时
            if(jedis.exists(key) && jedis.type(key).equals("string")) {
                // 有valueFields时
                if(value != null) {
                    // 有expireTime时
                    long val = Long.parseLong(value);
                    if(expireTime != 0) {
                        jedis.incrBy(key, val);
                        jedis.expire(key, expireTime);
                        res = "键" + key + "变化了" + val + "，距离过期还有" + expireTime + "秒" + "，现在为：" + jedis.get(key);
                    } else {    // 没有expireTime时（为0）
                        jedis.incrBy(key, val);
                        res = "键" + key + "变化了" + val + "，现在为：" + jedis.get(key);
                    }
                } else {    // 没有valueFields时
                    // 有expireTime时
                    if(expireTime != 0) {
                        jedis.expire(key, expireTime);
                        res = "键值为：" + jedis.get(key) + "，新设置过期时间为" + expireTime + "秒";
                    } else {    // 没有expireTime时（为0）
                        res = "键值为：" + jedis.get(key) + "，过期时间为：" + jedis.ttl(key) + "秒";
                    }
                }
            } else {    // key在redis中不存在时
                // 有valueFields
                if(value != null) {
                    // 有expireTime时
                    if(expireTime != 0) {
                        jedis.setex(key, expireTime, value);
                        res = "新增键：" + key + "，键值为：" + value + "，过期时间为：" + expireTime;
                    } else {    // 没有expireTime时(为0)
                        jedis.set(key, value);
                        res = "新增键：" + key + "，键值为：" + value;
                    }
                } else {    // 没有valueFields
                    res = "没有找到需要展示的键";
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
