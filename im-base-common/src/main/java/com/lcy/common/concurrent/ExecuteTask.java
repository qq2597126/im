
package com.lcy.common.concurrent;


public interface ExecuteTask {

    void execute();

    void onException(Throwable t);
}
