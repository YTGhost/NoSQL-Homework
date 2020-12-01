package com.bjtu.redis.jedis;

import com.bjtu.redis.util.DateUtil;
import org.junit.Test;

import com.bjtu.redis.JedisInstance;

import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JedisInstanceTest {

    /**
     * 基本使用
     */
    @Test
    public void test() {
        Jedis jedis = JedisInstance.getInstance().getResource();
        jedis.setex("name1", 20, "test");
        String val = jedis.get("name");
        System.out.println(val);
    }

    @Test
    public void testDate() throws ParseException {
        String test = "202011221400 202011221500";
        String[] test1 = test.split("\\s+");
        System.out.println(test1.length);
        System.out.println(test1[0]);
        System.out.println(test1[1]);
        SimpleDateFormat strToDate = new SimpleDateFormat("yyyyMMddHHmm");
        Date date = strToDate.parse("202011221444");
        System.out.println(date);
        SimpleDateFormat dateToStr = new SimpleDateFormat("yyyyMMddHH00");
        String str = dateToStr.format(date);
        System.out.println(str);
    }

    @Test
    public void test3() throws ParseException {
        DateUtil.StringFormat("202011221444 202011221535");
    }

}
