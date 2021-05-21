package com.morningstar.environmentreadiness.utils;

import com.morningstar.automation.base.core.utils.Logger;

/**
 * This class is a child of the Logger class from the base framework. It works exactly the same as it's parent except that each log message printed using this class also includes the name of the current thread.
 * @author AWaghm1
 */
public class ECLogger extends Logger{

    public ECLogger(Object obj) {
        super(obj);
    }

    public void info(Object msg) {
        super.info("Thread - [" + Thread.currentThread().getName() + "] " + msg);
    }

    public void error(Object msg) {
        super.error("Thread - [" + Thread.currentThread().getName() + "] " + msg);
    }

    public void warn(Object msg) {
        super.warn("Thread - [" + Thread.currentThread().getName() + "] " + msg);
    }

    public void debug(Object msg) {
        super.debug("Thread - [" + Thread.currentThread().getName() + "] " + msg);
    }

    public static ECLogger getLogger(Object obj) {
        return new ECLogger(obj);
    }
}
