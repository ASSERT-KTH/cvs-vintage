/*
 * Created on 09.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.imap.protocol;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.columba.core.main.MainInterface;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DebugInputStream extends FilterInputStream {

	OutputStream out;
	
	/**
	 * @param arg0
	 */
	public DebugInputStream(InputStream in, OutputStream out) {
		super(in);
		this.out = out;

	}

	/* (non-Javadoc)
	 * @see java.io.InputStrread()
	 */
	public int read() throws IOException {

		int result = in.read();
		if ( (result != -1) && (MainInterface.DEBUG.equals(Boolean.TRUE)) )
			out.write(result);

		
		return result;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int offset, int length) throws IOException {

		int result = in.read(b, offset, length);
		if ( (result != -1) && (MainInterface.DEBUG.equals(Boolean.TRUE)) )
			out.write(b, offset, result);

		

		return result;
	}

	/* (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
	public int read(byte[] b) throws IOException {

		int result = in.read(b);

		if ( (result != -1) && (MainInterface.DEBUG.equals(Boolean.TRUE)) )
			out.write(b);


		return result;
	}

}
