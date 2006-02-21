package org.columba.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PassiveCopyStream extends FilterInputStream {

	OutputStream out;
	
	public PassiveCopyStream(InputStream in, OutputStream out) {
		super(in);
		
		this.out = out;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(byte[] b, int off, int len) throws IOException {		
		int read =  super.read(b, off, len);
		
		if( read != -1) {
			out.write(b,off,read);
		}
		
		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException {
		super.close();
		
		out.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read() throws IOException {
		int result = super.read();
		
		if(result != -1) {
			out.write(result);
		}
		
		return result;
	}

	
	

}
