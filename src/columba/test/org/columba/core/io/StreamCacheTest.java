package org.columba.core.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import junit.framework.TestCase;

public class StreamCacheTest extends TestCase {

	File tempDir = new File("test_temp/");
	
	public void testAdd() throws IOException {
		byte[] random = new byte[1000];
		new Random().nextBytes(random);
		
		StreamCache cache = new StreamCache(tempDir);
		
		InputStream in = cache.passiveAdd("test1",new ByteArrayInputStream(random));
		
		byte[] test = new byte[1000];

		in.read(test);		
		in.close();
		
		assertEquals(1000, cache.getActSize());
		
		in = cache.get("test1");
		in.read(test);
		in.close();
		
		for( int i=0; i<1000; i++) {
			assertEquals(test[i], random[i]);			
		}
		
		cache.clear();
		
		assertEquals( 0, cache.getActSize());
		
		assertEquals( 0, tempDir.list().length);
	}
	
	public void testMaxsize() throws IOException, InterruptedException {
		byte[] random1 = new byte[1000];
		byte[] random2 = new byte[1000];
		byte[] random3 = new byte[1000];
		new Random().nextBytes(random1);
		new Random().nextBytes(random2);
		new Random().nextBytes(random3);
		
		StreamCache cache = new StreamCache(tempDir, 1700);
		
		InputStream in = cache.passiveAdd("test1",new ByteArrayInputStream(random1));
		
		byte[] test = new byte[1000];

		in.read(test);		
		in.close();
		
		assertEquals(1000, cache.getActSize());

		Thread.sleep(100);

		in = cache.passiveAdd("test2", new ByteArrayInputStream(random2));
		in.read(test);		
		in.close();

		assertEquals(1000, cache.getActSize());
		assertEquals(null, cache.get("test1"));
		
		
		in = cache.get("test2");
		in.read(test);
		in.close();
		
		for( int i=0; i<1000; i++) {
			assertEquals(test[i], random2[i]);			
		}

		cache.setMaxSize(2000);		
		in = cache.passiveAdd("test3", new ByteArrayInputStream(random3));
		in.read(test);		
		in.close();

		assertEquals(2000, cache.getActSize());

		in = cache.get("test3");
		in.read(test);
		in.close();
		
		for( int i=0; i<1000; i++) {
			assertEquals(test[i], random3[i]);			
		}
		
		cache.clear();
		
		assertEquals( 0, cache.getActSize());
		
		assertEquals( 0, tempDir.list().length);
	}

	protected void tearDown() throws Exception {
		File[] rest = tempDir.listFiles();
		for( int i=0; i<rest.length; i++) {
			rest[i].delete();
		}
		
	}
}
