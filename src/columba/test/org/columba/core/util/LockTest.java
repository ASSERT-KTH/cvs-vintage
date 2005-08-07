package org.columba.core.util;

import org.columba.core.base.Lock;

import junit.framework.TestCase;

public class LockTest extends TestCase {

	public boolean testBool;
	public int testInt;
	
	
	public void test1() throws InterruptedException {
		final Lock testLock = new Lock();
		testBool = false;
		
		assertTrue(testLock.tryToGetLock(this));
		
		Thread t = new Thread() {
			public void run() {
				testLock.getLock(this);
				assertTrue(testBool);
			}
		};
		
		t.start();
		
		Thread.sleep(100);
		
		testBool = true;
		testLock.release(this);
		
		t.join(100);
		assertFalse(t.isAlive());
		
		assertFalse(testLock.tryToGetLock(this));
		testLock.release(t);
		assertTrue(testLock.tryToGetLock(this));		
	}

	public void test2() throws InterruptedException {
		final Lock testLock = new Lock();
		testInt = 0;
		
		assertTrue(testLock.tryToGetLock(this));
		
		Thread t1 = new Thread() {
			public void run() {
				testLock.getLock(this);
				testInt++;
				testLock.release(this);
			}
		};
		
		Thread t2 = new Thread() {
			public void run() {
				testLock.getLock(this);
				testInt++;
				testLock.release(this);
			}
		};

		t1.start();
		t2.start();
		
		Thread.sleep(100);
		
		testLock.release(this);
		
		t1.join(100);
		t2.join(100);
		
		assertEquals(2,testInt);
	}
	
}
