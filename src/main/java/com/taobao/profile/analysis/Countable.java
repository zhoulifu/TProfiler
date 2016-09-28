package com.taobao.profile.analysis;

abstract class Countable implements Comparable<Countable> {
    private int count;

    Countable(int count) {
        this.count = count;
    }

    @Override
    public int compareTo(Countable o) {
        return o.count - this.count;
    }

    int getCount() {
        return count;
    }

    void setCount(int count) {
        this.count = count;
    }
}
