/*
 * $Header: /tmp/cvs-vintage/tomcat/src/share/org/apache/jasper/Attic/PluginJavaCompiler.java,v 1.1 1999/12/28 13:25:31 rubys Exp $
 * $Revision: 1.1 $
 * $Date: 1999/12/28 13:25:31 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *	 "This product includes software developed by the
 *	  Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.	IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.jasper.compiler;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
  * A Plug-in class for specifying a 'javac' compiler.
  *
  * @author Jeffrey Chiu
  */
public class PluginJavaCompiler implements JavaCompiler {

    static final int OUTPUT_BUFFER_SIZE = 1024;
    static final int BUFFER_SIZE = 512;

    OutputStream out;
    String compilerPath;

    public PluginJavaCompiler(String compilerPath) {
	this.compilerPath = compilerPath;
    }

    public void setOut(OutputStream out) {
	this.out = out;
    }

    public boolean compile(String[] args) {
	Process p;
	String[] compilerCmd = new String[args.length+1];
	int exitValue = -1;

	compilerCmd[0] = compilerPath;
	for(int x = 0; x < args.length; x++) {
	    compilerCmd[x+1] = args[x];
	}

	try {
	    p = Runtime.getRuntime().exec(compilerCmd);

	    BufferedInputStream compilerErr = new
		BufferedInputStream(p.getErrorStream());

	    StreamPumper errPumper = new StreamPumper(compilerErr);

	    errPumper.start();

	    try {
		// try grabbing exitValue first, if fails, then wait.
		// (very fast compilation returns almost immediately)
		exitValue = p.exitValue();
	    } catch (IllegalThreadStateException itse) {
		p.waitFor();
		exitValue = p.exitValue();
	    }

	    errPumper.gentleStop();
	    errPumper.cleanUp(); // deplete the stream.

	    compilerErr.close();
	    p.destroy();

	} catch (IOException ioe) {
	    return false;

	} catch (InterruptedException ie) {
	    return false;
	}

	return (exitValue==0);
    }

    // Inner class for continually pumping the input stream during
    // Process's runtime.
    class StreamPumper extends Thread {
	BufferedInputStream stream;
	boolean endOfStream = false;
	boolean stopSignal  = false;
	int SLEEP_TIME = 5;

	public StreamPumper(BufferedInputStream is) {
	    this.stream = is;
	}

	public void pumpStream()
	    throws IOException
	{
	    byte[] buf = new byte[BUFFER_SIZE];
	    if (!endOfStream) {
		int bytesRead=stream.read(buf, 0, BUFFER_SIZE);

		if (bytesRead > 0) {
		    out.write(buf, 0, bytesRead);
		} else if (bytesRead==-1)
		    endOfStream=true;
	    }
	}

	public void run() {
	    try {
		while (!endOfStream || stopSignal) {
		    pumpStream();
		    sleep(SLEEP_TIME);
		}
	    } catch (InterruptedException ie) {
	    } catch (IOException ioe) {
	    }
	}

	// causes this thread to stop in a safe manner.
	public void gentleStop() {
	    stopSignal = true;
	}

	// makes sure the stream is depleted.
	public void cleanUp() throws IOException {
	    while (!endOfStream) {
		pumpStream();
	    }
	}

    }
}


