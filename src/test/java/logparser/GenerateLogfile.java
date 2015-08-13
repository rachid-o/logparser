package logparser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class GenerateLogfile {

    private CountDownLatch cdl;

    public static void main(String[] args) throws Exception {
        log("Generating log file");
        GenerateLogfile gl = new GenerateLogfile();
        gl.run();
        gl.run();
        log("Finished generating log file");
    }


    private void run() throws InterruptedException {
        List<Thread> threads = new ArrayList<>();
        threads.add(new Thread(() -> {
            // Thread generate some noize in the log, until the others are finished
            while(true) {
                if(cdl.getCount() < 2) {
                    // I'm the only living thread left
                    break;
                }
                log("useless logline, coundown: " + cdl.getCount());
                sleep(500);
            }
            cdl.countDown();
        }));
        threads.add(new Thread(() -> {
            runMethod("slow", 45);
            cdl.countDown();
        }));
        threads.add(new Thread(() -> {
            runMethod("medium", 42);
            runMethod("medium", 42);
            cdl.countDown();
        }));
        threads.add(new Thread(() -> {
            runMethod("fast", 10);
            runMethod("fast", 10);
            runMethod("fast", 10);
            cdl.countDown();
        }));

        cdl = new CountDownLatch(threads.size());
        threads.forEach(t -> t.start());
        // Wait until all threads are finished
        cdl.await(10, TimeUnit.MINUTES);
    }


    private void runMethod(String methodName, int complexity) {
        log("STARTED " + methodName + " with a complexity of: " + complexity);
        long start = System.currentTimeMillis();
        fib(complexity);
        long duration = System.currentTimeMillis() - start;
        log("FINISHED " + methodName + " in " + duration + " ms");
    }


    /**
     * Calc fibonacci number. Starts to take noticeable time when n > 42
     */
    public static long fib(long n) {
        if (n == 1 || n == 2) {
            return 1;
        } else {
            return fib(n - 1) + fib(n - 2);
        }
    }

    private static void log(String message) {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-dd-MM HH:mm:ss:SSS");
        System.out.println(LocalDateTime.now().format(format) + " - " + message);
    }

    private static void sleep(int ms) {
        try {
            TimeUnit.MILLISECONDS.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}