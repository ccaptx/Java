package org.dvlyyon.nbi.protocols;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

public class StreamPair implements IOPair {

    Reader is;
    Writer os;
    
    /** Creates a new instance of ReaderConsumer */
    public StreamPair(InputStream is, OutputStream os ) {
        this.is = new InputStreamReader(is );
        this.os = new OutputStreamWriter( os );
    }
    
    public Reader getReader() {
        return is;
    }
    
    public Writer getWriter() {
        return os;
    }
    
    /**
     * TODO evaluate if this is even needed
     */
    public void reset() {
        try {
            is.reset();
        }catch(IOException ioe) {
        }
    }
    
    public void close() {
        try { is.close(); } catch(Exception e) { }
        try { os.close(); } catch(Exception e) { }
    }

}
