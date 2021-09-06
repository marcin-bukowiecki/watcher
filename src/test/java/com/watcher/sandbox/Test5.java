package com.watcher.sandbox;

import java.util.concurrent.TimeUnit;

public class Test5 {

    public static void main(String[] args) {
        Test5 test5 = new Test5();
        test5.loop();
    }

    public void loop() {
        int i = 0;
        while (true) {
            hello(i);
            i++;
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void hello(Integer a) {
        System.out.println("Hello1 " + a);
        System.out.println("Hello2 " + a);
    }
}
