package example;

import org.junit.ComparisonFailure;
import org.junit.Test;

import java.time.Duration;
import java.time.Instant;

import static example.EventuallyConsistent.eventually;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

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

    @Test
    public void testNeverSucceeds() {
        try {
            eventually(Duration.ofSeconds(2), () -> {
                // Will always be false, while true is expected at one point in time
                final boolean success = false;
                assertThat(success).isEqualTo(true);
            });
            fail("Should never have succeeded");
        } catch (ComparisonFailure cf) {
            assertThat(cf).hasMessage("expected:<[tru]e> but was:<[fals]e>");
        }
    }

    @Test
    public void testSucceedsRightAway() {
        try {
            eventually(Duration.ofSeconds(2), () -> {
                final boolean success = true;
                assertThat(success).isEqualTo(true);
            });
        } catch (Throwable t) {
            t.printStackTrace();
            fail("Should have succeeded right away");
        }
    }
}
