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
        eventually(Duration.ofSeconds(10), () -> {
            // The value should be retrieved within the command block (not outside otherwise it will not be retried)
            Response response = myWebservice.retrieveSomething(id);
            assertThat(response.getSomething().getName()).isEqualTo("foo");
        });
    }
    
    @Test
    public void thisWillNotWork() {
        // This is incorrect! The result is retrieved only once and not retried to become consistent.
        Response response = myWebservice.retrieveSomething(id);
        eventually(Duration.ofSeconds(10), () ->
            assertThat(response.getSomething().getName()).isEqualTo("foo")
        );
    }
}
```
See the test class for runnable examples: [EventuallyConsistentTest.java](src/test/java/example/EventuallyConsistentTest.java)

