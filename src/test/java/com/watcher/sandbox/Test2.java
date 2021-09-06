package com.watcher.sandbox;

import java.util.concurrent.TimeUnit;

public class Test2 {

    public static void testThrow(Integer a) {
        Test.testThrow(a);
    }

    public void loop() {
        while (true) {
            System.out.println("Bit");
            try {
                TimeUnit.SECONDS.sleep(2L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void loop2() {
        for (int i = 0; i < 5; i++) {
            System.out.println("Byte");
            try {
                TimeUnit.SECONDS.sleep(1L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
