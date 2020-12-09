package com.bjtu.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bjtu.redis.domain.ActionSpec;
import com.bjtu.redis.domain.CounterConfig;
import com.bjtu.redis.domain.CounterSpec;
import com.bjtu.redis.factory.TypeFactory;
import com.bjtu.redis.util.FileListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {
    // counter映射
    private static final HashMap<String, CounterSpec> counterMap = new HashMap<>();
    // action映射
    private static final HashMap<String, ActionSpec> actionMap = new HashMap<>();
    // typeFactory
    private static final TypeFactory typeFactory = new TypeFactory();
    // 终端输入
    private static final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    // 终端输出
    private static final PrintWriter out = new PrintWriter(System.err, true);
    // monitor
    private static FileAlterationMonitor monitor;
    // lock
    public static AtomicBoolean lock = new AtomicBoolean(false);

    public static void loadConfigJson() {
        try {
            out.println();
            out.println("读取Json配置文件中...");
            // 清空
            counterMap.clear();
            actionMap.clear();
            // 获取actions和counters的json配置文件
            String actionsPath = "src/main/resources/actions.json";
            String countersPath = "src/main/resources/counters.json";

            //获取配置文件，并转为String
            InputStream inputStream = new FileInputStream(actionsPath);
            String actionsText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            inputStream = new FileInputStream(countersPath);
            String countersText = IOUtils.toString(inputStream, StandardCharsets.UTF_8);

            // 将配置文件中的action转为实体类并放入actionMap中
            JSONObject obj1 = JSON.parseObject(actionsText);
            JSONArray actions = obj1.getJSONArray("actions");
            for(int i = 0; i < actions.size(); i++) {
                String actionName = (String) actions.getJSONObject(i).get("name");
                List<CounterConfig> retrieve = new ArrayList<>();
                List<CounterConfig> save = new ArrayList<>();
                JSONArray retrieveArray = actions.getJSONObject(i).getJSONArray("retrieve");
                JSONArray saveArray = actions.getJSONObject(i).getJSONArray("save");
                for(int j = 0; j < retrieveArray.size(); j++)
                    retrieve.add(retrieveArray.getJSONObject(j).toJavaObject(CounterConfig.class));
                for(int j = 0; j < saveArray.size(); j++)
                    save.add(saveArray.getJSONObject(j).toJavaObject(CounterConfig.class));
                ActionSpec actionSpec = new ActionSpec(retrieve, save);
                actionMap.put(actionName, actionSpec);
            }

            // 将配置文件中的counter转为实体类并放入counterMap中
            JSONObject obj2 = JSON.parseObject(countersText);
            JSONArray counters = obj2.getJSONArray("counters");
            for(int i = 0; i < counters.size(); i++) {
                CounterSpec counterSpec = counters.getJSONObject(i).toJavaObject(CounterSpec.class);
                counterMap.put(counterSpec.getCounterName(), counterSpec);
            }
            out.println("读取完毕！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<CounterSpec> resolveAction(String actionName) {
        ActionSpec actionSpec = actionMap.get(actionName);
        List<CounterSpec> counterList = new ArrayList<>();
        actionSpec.getRetrieve().forEach(counterConfig -> {
            counterList.add(counterMap.get(counterConfig.getCounterName()));
        });
        actionSpec.getSave().forEach(counterConfig -> {
            counterList.add(counterMap.get(counterConfig.getCounterName()));
        });
        return counterList;
    }

    public static void resolveCounters(List<CounterSpec> counterList) {
        counterList.forEach(counter -> {
            System.out.println(counter.getCounterName() + "执行中...");
            String res = null;
            try {
                res = typeFactory.getResolver(counter.getType(), counter).resolve();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println(res);
        });
        out.println();
    }

    private static int getChoice() throws IOException {
        int input;
        do {
            try {
                out.println();
                out.print("[0]  退出\n"
                        + "[1]  显示所有actions\n"
                        + "[2]  执行action\n"
                        + "choice> ");
                out.flush();

                input = Integer.parseInt(in.readLine());

                out.println();

                if (0 <= input && 3 >= input) {
                    break;
                } else {
                    out.println("非法的选择:  " + input);
                }
            } catch (NumberFormatException nfe) {
                out.println(nfe);
            }
        } while (true);

        return input;
    }

    private static void showAllActions() {
        out.println("您配置中的actions如下：");
        actionMap.forEach((name, action) -> out.println(name));
    }

    private static void toResolveAction() throws IOException {
        out.print("请输入想要执行的action：");
        out.flush();
        String name = in.readLine();
        // 去actionMap中查询是否存在
        if (actionMap.containsKey(name)) {
            // 将指定action中Counter取出
            List<CounterSpec> counterList = resolveAction(name);
            // 执行counters
            resolveCounters(counterList);
        } else {
            out.println("您输入的action不存在");
        }
    }

    private static void startObserver() throws Exception {
        String monitorDir = "src/main/resources";
        // 轮询间隔时间（1000）毫秒
        long interval = TimeUnit.SECONDS.toMillis(1);
        FileAlterationObserver observer = new FileAlterationObserver(monitorDir);
        observer.addListener(new FileListener());
        // 创建文件变化监听器
        monitor = new FileAlterationMonitor(interval, observer);
        // 开始监听
        monitor.start();
        out.println("开始监听json变化");
    }

    public static void main(String[] args) throws Exception {
        // 开始监听
        startObserver();
        // 加载Json配置文件
        loadConfigJson();
        out.println("欢迎使用Counter系统V1.0版本");
        out.println("作者：邓梁 18221221");
        // 获取用户所选择的操作
        int choice = getChoice();
        while (choice != 0) {
            lock.compareAndSet(false, true);
            if (choice == 1) {
                showAllActions();
            } else if (choice == 2) {
                toResolveAction();
            }
            // 释放锁
            lock.set(false);
            choice = getChoice();
        }
        monitor.stop();
    }

    public static HashMap<String, CounterSpec> getCounterMap() {
        return counterMap;
    }

    public static HashMap<String, ActionSpec> getActionMap() {
        return actionMap;
    }
}
