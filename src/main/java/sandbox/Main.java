package sandbox;

import java.util.concurrent.TimeUnit;

/**
 * Playground
 *
 * @author Marcin Bukowiecki
 */
public class Main {

    public volatile boolean run = true;

    public static void main(String[] args) {
        System.out.println("Starting...");
        new Main().loop();
    }

    public void loop() {
        int i = 0;
        while (run) {
            hello(i);
            System.out.println("After hello");
            i++;
            try {
                TimeUnit.SECONDS.sleep(5L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (i%10 == 0) {
                System.gc();
            }
        }
    }

    public static int getInt() {
        return 100;
    }

    public static void hello(Integer a) {
        int i = getInt();
        hello2(a * i);
    }

    public static void hello2(Integer a) {
        System.out.println("Hello1 " + a);
        System.out.println("Hello2 " + a);
    }
}
