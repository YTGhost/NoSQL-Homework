package com.bjtu.redis.domain;

public class CounterConfig {

    private String counterName;

    private String condition;

    public String getCounterName() {
        return counterName;
    }

    public String getCondition() {
        return condition;
    }

    public void setCounterName(String counterName) {
        this.counterName = counterName;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    @Override
    public String toString() {
        return "CounterRetrieveConfig{" +
                "counterName='" + counterName + '\'' +
                ", condition='" + condition + '\'' +
                '}';
    }
}
