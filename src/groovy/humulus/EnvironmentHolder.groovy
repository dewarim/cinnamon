package humulus

public class EnvironmentHolder {

    private static final ThreadLocal contextHolder  = new ThreadLocal();

        static void setEnvironment(Map environment) {
        contextHolder.set(environment);
    }

    static getEnvironment() {
        return contextHolder.get();
    }

    static void clear() {
        contextHolder.remove();
    }
}
