/*
 * Created on 09.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.protocol;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.columba.core.main.MainInterface;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DebugOutputStream extends FilterOutputStream {

	OutputStream debug;
	/**
	 * @param arg0
	 */
	public DebugOutputStream(OutputStream out, OutputStream debug) {
		super(out);
		
		this.debug = debug;
		
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[], int, int)
	 */
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		
		if (MainInterface.DEBUG)
			debug.write(arg0, arg1, arg2);
		
		out.write(arg0, arg1, arg2);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(byte[])
	 */
	public void write(byte[] arg0) throws IOException {
		
		if (MainInterface.DEBUG)
			debug.write(arg0);
		
		out.write(arg0);
	}

	/* (non-Javadoc)
	 * @see java.io.OutputStream#write(int)
	 */
	public void write(int arg0) throws IOException {
		
		if (MainInterface.DEBUG)
			debug.write(arg0);
		
		out.write(arg0);
	}

}
