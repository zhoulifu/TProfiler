package com.taobao.profile.analysis;

class MethodCount extends Countable {
    private String methodName;

    MethodCount(int count, String methodName) {
        super(count);
        this.methodName = methodName;
    }

    String getMethodName() {
        return methodName;
    }

    void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public int compareTo(Countable o) {
        int rtn = super.compareTo(o);
        return rtn == 0 ? this.methodName.compareTo(((MethodCount) o).methodName) : rtn;
    }
}
