package com.wzy;

import lombok.extern.slf4j.Slf4j;

/**
 * 可以看到线程的栈帧执行过程
 */
@Slf4j(topic = "Test01")
public class Test01 {

    public static void main(String[] args) {
        method1(10);
    }

    private static void method1(int x) {
        int y = x + 1;
        Object m = method2();
        System.out.println(m);
    }

    private static Object method2() {
        Object n = new Object();
        return n;
    }

}
