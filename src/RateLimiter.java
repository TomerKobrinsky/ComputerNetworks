/**
 * A token bucket based rate-limiter.
 *
 * This class should implement a "soft" rate limiter by adding maxBytesPerSecond tokens to the bucket every second,
 * or a "hard" rate limiter by resetting the bucket to maxBytesPerSecond tokens every second.
 */
public class RateLimiter implements Runnable {
    private final TokenBucket tokenBucket;
    private final Long maxBytesPerSecond;

    RateLimiter(TokenBucket tokenBucket, Long maxBytesPerSecond) {
        this.tokenBucket = tokenBucket;
        this.maxBytesPerSecond = maxBytesPerSecond;
    }

    @Override
    public void run() {
        while(!(tokenBucket.terminated())) {
            tokenBucket.set(maxBytesPerSecond);
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("err message: " + e + ", problem waiting with the tread. Download Failed");
            }
        }
    }
}
