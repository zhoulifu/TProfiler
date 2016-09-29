package com.taobao.profile;

public class MethodFrame {
    private long methodId;
    private long useTime;
    private long depth;

    public MethodFrame(long methodId, long useTime, long depth) {
        this.methodId = methodId;
        this.useTime = useTime;
        this.depth = depth;
    }

    public long depth() {
        return this.depth;
    }

    public long useTime() {
        return this.useTime;
    }

    public long methodId() {
        return this.methodId;
    }

    public void useTime(long useTime) {
        this.useTime = useTime;
    }
}
