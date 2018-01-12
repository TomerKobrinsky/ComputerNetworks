/**
 * A Token Bucket (https://en.wikipedia.org/wiki/Token_bucket)
 *
 * This thread-safe bucket should support the following methods:
 *
 * - take(n): remove n tokens from the bucket (blocks until n tokens are available and taken)
 * - set(n): set the bucket to contain n tokens (to allow "hard" rate limiting)
 * - add(n): add n tokens to the bucket (to allow "soft" rate limiting)
 * - terminate(): mark the bucket as terminated (used to communicate between threads)
 * - terminated(): return true if the bucket is terminated, false otherwise
 *
 */
class TokenBucket {

    Boolean isBucketFull;
    Long tokensLeftToTake;

    TokenBucket() {
        isBucketFull = false;
        tokensLeftToTake = 0L;
    }
// remove tokens from the bucket (blocks until n tokens are available and taken)
    void take(long tokens) {
        // if tokens is bigger than tokensLeftToTake thread is waiting
        while (tokens > tokensLeftToTake)
        {
            try {
               Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                System.err.println("err message: " + e + ", problem waiting with the tread. Download Failed");
            }
        }
        tokensLeftToTake -= tokens;
    }

    void terminate() {
        this.isBucketFull = true;

    }

    boolean terminated() {
        return  isBucketFull;
    }

    void set(long tokens) {
        this.tokensLeftToTake = tokens;
    }

// return the number off tokens left to take
    long getTokensLeftToTake ()
    {
        return tokensLeftToTake;
    }
}
