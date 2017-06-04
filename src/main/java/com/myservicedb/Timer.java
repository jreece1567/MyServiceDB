/**
 *
 */
package com.myservicedb;

import java.util.Date;

/**
 * Utility timer.
 *
 * <pre>
 * Timer t = new Timer().start();
 * ...
 * ... do stuff
 * ...
 * System.out.println("Elapsed: "+t.stop().elapsed()+"ms");
 * </pre>
 *
 * @author jreece
 *
 */
public class Timer {

    private Long start;
    private Long stop;

    public Timer() {
        this.init();
    }

    private void init() {
        start = new Date().getTime();
        stop = start;
    }

    /**
     * Start the timer.
     *
     * @return this {@code Timer} instance.
     */
    public Timer start() {
        this.init();
        return this;
    }

    /**
     * Stop the timer.
     *
     * @return this {@code Timer} instance.
     */
    public Timer stop() {
        stop = new Date().getTime();
        return this;
    }

    /**
     * Get the elapsed time recorded by this {@code Timer} instance.
     *
     * @return the elapsed time recorded by this {@code Timer}, in milliseconds.
     */
    public Long elapsed() {
        return stop - start;
    }
}
