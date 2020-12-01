package com.bjtu.redis.factory;

import com.bjtu.redis.domain.CounterSpec;
import com.bjtu.redis.util.DateSplitUtils;
import com.bjtu.redis.util.DateUtil;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FreqResolver implements TypeResolver{

    private CounterSpec counterSpec;
    private Jedis jedis;

    @Override
    public String resolve() throws ParseException {
        String res = "没有进行有效操作";
        String key = counterSpec.getKeyFields();
        String field = counterSpec.getFields();
        String value = counterSpec.getValueFields();
        // 有keyFields字段时
        if(key != null) {
            if(jedis.exists(key)) {
                if(field != null) {
                    String[] t = DateUtil.StringFormat(field);
                    if(t.length == 1) {
                        if(jedis.hexists(key, t[0])) {
                            if(value != null) {
                                long val = Long.parseLong(value);
                                jedis.hincrBy(key, t[0], val);
                                res = "键:" + key + "，时段：" + t[0] + "，变化了" + val + "，现在为：" + jedis.hget(key, t[0]);
                            } else {
                                res = "键:" + key + "，时段：" + t[0] + "值为：" + jedis.hget(key, t[0]);
                            }
                        } else {
                            if(value != null) {
                                jedis.hset(key, t[0], value);
                                res = "设置键" + key + "，时段：" + t[0] + "，值为：" + value;
                            } else {
                                res = "没有找到当前时段数据";
                            }
                        }
                    } else if (t.length == 2) {
                        String startStr = t[0];
                        String endStr = t[1];
                        SimpleDateFormat strToDate = new SimpleDateFormat("yyyyMMddHHmm");
                        Date start = strToDate.parse(startStr);
                        Date end = strToDate.parse(endStr);
                        List<DateSplitUtils.DateSplit> dateSplits = DateSplitUtils.splitDate(start, end, DateSplitUtils.IntervalType.HOUR, 1);
                        List<String> timeKeys = new ArrayList<>();
                        for(int i = 0; i < dateSplits.toArray().length; i++)
                        {
                            timeKeys.add(dateSplits.get(i).getStartDateTimeStr());
                        }
                        long total = 0;
                        for(int i = 0; i < timeKeys.size(); i++)
                        {
                            if(jedis.hexists(key, timeKeys.get(i))) {
                                total += Long.parseLong(jedis.hget(key, timeKeys.get(i)));
                            }
                        }
                        res = "键:" + key + "，时段" + startStr + "-->" + endStr + "，总和为：" + total;
                    }
                } else {
                    jedis.hgetAll(key).forEach((k, v) -> {
                        System.out.println("field:" + k + "，value:" + v);
                    });
                    res = "以上为该key中所有field和field的值";
                }
            } else {
                if(field != null) {
                    String[] t = DateUtil.StringFormat(field);
                    if(t.length == 1) {
                        if(value != null) {
                            jedis.hset(key, t[0], value);
                            res = "键:" + key + "，时段：" + t[0] + "，设置值为：" + value;
                        }
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
