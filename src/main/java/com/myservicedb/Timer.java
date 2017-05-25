/**
 *
 */
package com.myservicedb;

import java.util.Date;

/**
 * Utility timer
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

    public Timer start() {
        this.init();
        return this;
    }

    public Timer stop() {
        stop = new Date().getTime();
        return this;
    }

    public Long elapsed() {
        return stop - start;
    }
}
