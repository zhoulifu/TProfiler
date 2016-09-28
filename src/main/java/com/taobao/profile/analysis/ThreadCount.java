package com.taobao.profile.analysis;

class ThreadCount extends Countable {
    private long threadId;
    private String threadName;
    private String threadState;

    ThreadCount(int count, String threadId, String threadName,
            String threadState) {
        super(count);
        this.threadId = Long.parseLong(threadId);
        this.threadName = threadName;
        this.threadState = threadState;
    }

    long getThreadId() {
        return threadId;
    }

    void setThreadId(long threadId) {
        this.threadId = threadId;
    }

    String getThreadName() {
        return threadName;
    }

    void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    String getThreadState() {
        return threadState;
    }

    void setThreadState(String threadState) {
        this.threadState = threadState;
    }

    @Override
    public int compareTo(Countable o) {
        int rtn = super.compareTo(o);
        return rtn == 0 ? Long.compare(this.threadId, ((ThreadCount) o).threadId) : rtn;
    }
}
