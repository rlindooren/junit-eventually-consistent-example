An example of how to give assertions some time to succeed.

This can be useful when testing, for example, a webservice or database that is updated asynchrounously and therefore takes a while to become consistent.

Implementation:
```java
public class EventuallyConsistent {

    /**
     * @param maxDuration the duration during which the command will be retried after it fails exceptionally
     * @param command     the logic that fails exceptionally while its assertions are not successful
     */
    public static void eventually(Duration maxDuration, Runnable command) {
        final Instant start = Instant.now();
        final Instant max = start.plus(maxDuration);

        boolean failed;
        do {
            try {
                command.run();
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
```
Example usage:
```java
import static example.EventuallyConsistent.eventually;
public class EventuallyConsistentTest {

    @Test
    public void testEventuallySucceeds() {
        final Instant start = Instant.now();
        try {
            eventually(Duration.ofSeconds(10), () -> {
                // Expected result (true) is returned only after 3 seconds
                final boolean success = Instant.now().isAfter(start.plus(Duration.ofSeconds(3))) ? true : false;
                
                // This will fail exceptionally (in the form of a ComparisonFailure)
                // as long as the result doesn't have the expected value yet
                assertThat(success).isEqualTo(true);
                
                // This could also be a call to a database, webservice, you-name-it, that takes a while to be updated
                // For example:
                //
                // Response response = myWebservice.retrieveSomething(id);
                // assertThat(response.getSomething().getName()).isEqualTo("foo");
                //
                // Just make sure that you retrieve the value within this command (not outside otherwise it will not be retried)
            });
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Should have succeeded after ~ 3 seconds");
        }
    }
}
```
