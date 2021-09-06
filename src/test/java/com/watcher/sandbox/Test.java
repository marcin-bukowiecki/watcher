package com.watcher.sandbox;

import java.util.ArrayList;

public class Test {

    private static int b = 8;

    public static void testThrow(Integer a) {
        int c = b + 7;
        System.out.println("Hello world" + c);
        adder(12, 23);

        final ArrayList<Object> objects = new ArrayList<>();
        objects.add("test");
    }

    public static int adder(int a, int b) {
        System.out.println(a + b);
        throw new NullPointerException();
    }
}
