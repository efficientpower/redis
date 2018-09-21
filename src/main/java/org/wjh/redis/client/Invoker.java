package org.wjh.redis.client;

public interface Invoker {
    public <T> T invoke();
}
