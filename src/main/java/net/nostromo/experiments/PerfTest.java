package net.nostromo.experiments;

public class PerfTest {

    private final long iterations;
    private final long loopLimit;

    public PerfTest(final long iterations, final long loopLimit) {
        this.iterations = iterations;
        this.loopLimit = loopLimit;
    }

    public void timedTest(final String name, final Action action) {
        System.out.println(name + " test");

        for (long x = 0; x < iterations; x++) {
            final long start = System.nanoTime();

            for (long y = 0; y < loopLimit; y++) {
                if (action.execute() == null) {
                    throw new RuntimeException("null");
                }
            }

            final long elapsed = System.nanoTime() - start;
            System.out.printf("%,.0f per second\n", loopLimit / (elapsed / (double) 1_000_000_000));
        }
    }

    public interface Action {
        Object execute();
    }
}
