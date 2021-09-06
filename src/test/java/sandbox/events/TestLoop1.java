package sandbox.events;

public class TestLoop1 {

    public void foo() {
        for (int i = 0; i < 20; i++) {
            bar(i);
        }
    }

    public void bar(int i) {
        if (i == 13) {
            throw new RuntimeException();
        }
    }
}
