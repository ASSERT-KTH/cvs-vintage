//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import junit.framework.TestCase;

public class ByteArrayOutputStreamTest extends TestCase {

	public void test1() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(10);
		for( int i=0; i<10; i++) {
			out.write(i);
		}
		
		byte[] result = out.getBuffer();
		for( int i=0; i<10; i++) {
			assertTrue(result[i] == i);
		}
	}

	public void test2() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(10);
		byte[] tempBuffer = new byte[10];
		
		for( int i=0; i<10; i++) {
			tempBuffer[i] = (byte)i;
		}
		
		out.write(tempBuffer);
		
		byte[] result = out.getBuffer();
		for( int i=0; i<10; i++) {
			assertTrue(result[i] == i);
		}
	}

	public void test3() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(10);
		byte[] tempBuffer = new byte[5];
		
		for( int i=0; i<5; i++) {
			tempBuffer[i] = (byte)i;
		}
		
		out.write(tempBuffer);
		
		for( int i=5; i<10; i++) {
			out.write(i);
		}

		byte[] result = out.getBuffer();
		for( int i=0; i<10; i++) {
			assertTrue(result[i] == i);
		}
	}

	public void test4() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream(10);
		for( int i=0; i<10; i++) {
			out.write(i);
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(out.getBuffer());
		int size = in.available();
		for( int i=0; i<size; i++) {
			assertTrue(in.read() == i);
		}
	}


}
