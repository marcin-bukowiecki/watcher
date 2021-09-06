package sandbox.events;

public class Main1 {

    public static void main(String[] args) {
        System.out.println("Starting...");
        new Main1().loop();
    }

    public void loop() {
        throw new NullPointerException();
    }
}
