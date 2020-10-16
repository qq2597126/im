package com.lcy.server.balance;

public interface LoadBalance<T> {

    public T balance();

}
