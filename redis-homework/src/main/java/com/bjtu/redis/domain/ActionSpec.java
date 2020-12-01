package com.bjtu.redis.domain;

import java.util.List;

public class ActionSpec {

    private List<CounterConfig> retrieve;

    private List<CounterConfig> save;

    public List<CounterConfig> getRetrieve() {
        return retrieve;
    }

    public List<CounterConfig> getSave() {
        return save;
    }

    public void setRetrieve(List<CounterConfig> retrieve) {
        this.retrieve = retrieve;
    }

    public void setSave(List<CounterConfig> save) {
        this.save = save;
    }

    public ActionSpec(List<CounterConfig> retrieve, List<CounterConfig> save) {
        this.retrieve = retrieve;
        this.save = save;
    }

    @Override
    public String toString() {
        return "ActionSpec{" +
                "retrieve=" + retrieve +
                ", save=" + save +
                '}';
    }
}
