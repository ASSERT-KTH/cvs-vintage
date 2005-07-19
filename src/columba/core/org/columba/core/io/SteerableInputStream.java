package org.columba.core.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SteerableInputStream extends FilterInputStream {

	private long lengthLeft;
	private long position;
	
	
	public SteerableInputStream(InputStream in) {
		super(in);
		
		try {
			lengthLeft = in.available();
		} catch (IOException e) {
			lengthLeft = 0;
		}
	}

	/**
	 * @see java.io.FilterInputStream#read()
	 */
	public int read() throws IOException {
		if( lengthLeft > 0 ) {
			int read =  super.read();
			if( read != -1) lengthLeft--;
			position++;
			return read;
		} else {
			return -1;
		}
	}

	/**
	 * @see java.io.FilterInputStream#read(byte[], int, int)
	 */
	public int read(byte[] b, int off, int len) throws IOException {
		if( len > lengthLeft ) {
			int correctedLen = (int)lengthLeft;
			int read = super.read(b,off,correctedLen);
			
			lengthLeft -= read;
			position += read;
			return read;
		} else {
			int read = super.read(b, off, len);
			lengthLeft -= read;
			position += read;
			return read;
		}
			
	}

	/**
	 * @return Returns the lengthLeft.
	 */
	public long getLengthLeft() {
		return lengthLeft;
	}

	/**
	 * @param lengthLeft The lengthLeft to set.
	 */
	public void setLengthLeft(long lengthLeft) throws IOException {
		this.lengthLeft = Math.min(lengthLeft, in.available());
	}

	/**
	 * @return Returns the position.
	 */
	public long getPosition() {
		return position;
	}

	/**
	 * @param position The position to set.
	 */
	public void setPosition(long newposition) throws IOException{
		long skipped = in.skip(newposition - position);
		lengthLeft = Math.max(0, lengthLeft - skipped);
		position = newposition;
	}

	/**
	 * @see java.io.FilterInputStream#available()
	 */
	public int available() throws IOException {
		return (int)lengthLeft;
	}

	/**
	 * 
	 * @see java.io.FilterInputStream#close()
	 */
	public void close() throws IOException {
	}
	
	
	/**
	 * 
	 * @see java.io.FilterInputStream#close()
	 */
	public void finalClose() throws IOException {
		super.close();
	}
	

}
