import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.*;

public class IdcDm {

    /**
     * Receive arguments from the command-line, provide some feedback and start the download.
     *
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        int numberOfWorkers = 1;
        Long maxBytesPerSecond = null;

        if (args.length < 1 || args.length > 3) {
            System.err.printf("usage:\n\tjava IdcDm URL [MAX-CONCURRENT-CONNECTIONS] [MAX-DOWNLOAD-LIMIT]\n");
            System.exit(1);
        } else if (args.length >= 2) {
            numberOfWorkers = Integer.parseInt(args[1]);
            if (args.length == 3)
                maxBytesPerSecond = Long.parseLong(args[2]);
        }

        String url = args[0];

        System.err.printf("Downloading");
        if (numberOfWorkers > 1)
            System.err.printf(" using %d connections", numberOfWorkers);
        if (maxBytesPerSecond != null)
            System.err.printf(" limited to %d Bps", maxBytesPerSecond);
        System.err.printf("...\n");

        DownloadURL(url, numberOfWorkers, maxBytesPerSecond);
    }

    /**
     * Initiate the file's metadata, and iterate over missing ranges. For each:
     * 1. Setup the Queue, TokenBucket, DownloadableMetadata, FileWriter, RateLimiter, and a pool of HTTPRangeGetters
     * 2. Join the HTTPRangeGetters, send finish marker to the Queue and terminate the TokenBucket
     * 3. Join the FileWriter and RateLimiter
     *
     * Finally, print "Download succeeded/failed" and delete the metadata as needed.
     *
     * @param url URL to download
     * @param numberOfWorkers number of concurrent connections
     * @param maxBytesPerSecond limit on download bytes-per-second
     */
    private static void DownloadURL(String url, int numberOfWorkers, Long maxBytesPerSecond) {
        LinkedBlockingQueue blockingQueue = new LinkedBlockingQueue();
        if (maxBytesPerSecond != null) {
            TokenBucket tokenBucket = new TokenBucket();
            RateLimiter rateLimiter = new RateLimiter(tokenBucket, maxBytesPerSecond);
            rateLimiter.run();
            URL firstUrl = null;
            try {
                firstUrl = new URL(url);
            } catch (MalformedURLException e) {
                System.err.println("problem with url" + e);
            }
            URLConnection urlConnection1 = null;
            try {
                urlConnection1 = firstUrl.openConnection();
            } catch (IOException e) {
                System.err.println("problem with opening connection" + e);
            }
            long totalBytes = urlConnection1.getContentLength();
            long threadBytes = totalBytes / numberOfWorkers;
            long curStart = 0;
            Thread[] threads = new Thread[numberOfWorkers];
            long parentThreadId = Thread.currentThread().getId();

            for (int i = 0; i < numberOfWorkers; i++) {
                Range range = new Range(curStart, curStart + threadBytes);
                curStart += threadBytes;
                HTTPRangeGetter httpRangeGetter = new HTTPRangeGetter(url, range, blockingQueue, tokenBucket);

                threads[i] = new Thread();
                if (parentThreadId != Thread.currentThread().getId()) {
                    threads[i].start();
                }

            }

        }
    }

}



