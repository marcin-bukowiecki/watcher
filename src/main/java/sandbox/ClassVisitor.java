package sandbox;

public abstract class ClassVisitor {
    protected final int api;
    protected ClassVisitor cv;

    public ClassVisitor(int api) {
        this(api, (ClassVisitor) null);
    }

    public ClassVisitor(int api, ClassVisitor classVisitor) {
        if (api != 458752 && api != 393216 && api != 327680 && api != 262144) {
            throw new IllegalArgumentException("Unsupported api " + api);
        } else {
            this.api = api;
            this.cv = classVisitor;
        }
    }
}
