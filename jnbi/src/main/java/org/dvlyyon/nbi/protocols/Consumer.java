package org.dvlyyon.nbi.protocols;

import java.io.IOException;

/**
 *
 * @author justin
 */
public interface Consumer extends Runnable {
    public void run();
    public void waitForBuffer(long timeoutMilli);
    public void send(String str) throws IOException;
    public void send(int value) throws IOException;
    public String pause();
    public void resume();
    public void resume(int offset);
    public void clear();
    public void stop();
    public boolean foundEOF();    
}
