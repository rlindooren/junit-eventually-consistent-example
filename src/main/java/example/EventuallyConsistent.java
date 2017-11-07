package example;

import java.time.Duration;
import java.time.Instant;

public class EventuallyConsistent {

    /**
     * @param maxDuration the duration during which the function/block will be retried after it fails exceptionally
     * @param block       the logic that fails exceptionally while its assertions are not successful
     */
    public static void eventually(Duration maxDuration, Runnable block) {
        final Instant start = Instant.now();
        final Instant max = start.plus(maxDuration);

        boolean failed;
        do {
            try {
                block.run();
                failed = false;
            } catch (Throwable t) {
                failed = true;
                if (Instant.now().isBefore(max)) {
                    // Try again after a short nap
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ignored) {
                    }
                } else {
                    // Max duration has exceeded, it took too long to become consistent
                    throw t;
                }
            }
        } while (failed);
    }
}
