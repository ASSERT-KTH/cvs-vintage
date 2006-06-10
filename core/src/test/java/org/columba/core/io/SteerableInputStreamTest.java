package org.columba.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class SteerableInputStreamTest extends TestCase {
	
	public void test() throws IOException {
		byte[] test = new byte[] {0, 1, 2, 3, 4, 5 , 6, 7, 8, 9, 10};
		byte[] dummy = new byte[5];
		
		SteerableInputStream in = new SteerableInputStream(new ByteArrayInputStream(test));
		
		assertEquals(test.length,in.getLengthLeft());
		assertEquals(0, in.getPosition());
		
		in.setLengthLeft(3);
		in.setPosition(2);
		assertEquals(1,in.getLengthLeft());
		assertEquals(2, in.getPosition());
		
		assertEquals(2, in.read());
		assertEquals(0,in.getLengthLeft());
		assertEquals(-1, in.read());
		assertEquals(0,in.read(dummy));
		
		in.setLengthLeft(100);
		assertEquals(test.length - in.getPosition(), in.getLengthLeft());
		assertEquals(3, in.read());
		
		assertEquals(3,in.read(dummy,2,3));
		assertEquals(4,dummy[2]);
		assertEquals(6,dummy[4]);
		
		assertEquals(4,in.read(dummy));
		assertEquals(10, dummy[3]);
		
	}

}
