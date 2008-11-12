package org.junit.tests.experimental.max;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.max.CouldNotReadCoreException;
import org.junit.experimental.max.MaxCore;
import org.junit.runner.Computer;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;


public class MaxStarterTest {
	
	private MaxCore fMax;

	@Before public void createMax() {
		fMax= MaxCore.createFresh();
	}
	
	@After public void forgetMax() {
		fMax.forget();
	}
	
	public static class TwoTests {
		@Test public void succeed() {}
		@Test public void dontSucceed() { fail(); }
	}
	
	@Test public void twoTestsNotRunComeBackInRandomOrder() {
		Request request= Request.aClass(TwoTests.class);
		List<Description> things= fMax.sortedLeavesForTest(request);
		Description succeed= Description.createTestDescription(TwoTests.class, "succeed");
		Description dontSucceed= Description.createTestDescription(TwoTests.class, "dontSucceed");
		assertTrue(things.contains(succeed));
		assertTrue(things.contains(dontSucceed));
		assertEquals(2, things.size());
	}
	
	@Test public void preferNewTests() {
		Request one= Request.method(TwoTests.class, "succeed");
		fMax.run(one);
		Request two= Request.aClass(TwoTests.class);
		List<Description> things= fMax.sortedLeavesForTest(two);
		Description dontSucceed= Description.createTestDescription(TwoTests.class, "dontSucceed");
		assertEquals(dontSucceed, things.get(0));
		assertEquals(2, things.size());
	}
	
	// This covers a seemingly-unlikely case, where you had a test that failed on the
	// last run and you also introduced new tests. In such a case it pretty much doesn't matter
	// which order they run, you just want them both to be early in the sequence
	@Test public void preferNewTestsOverTestsThatFailed() {
		Request one= Request.method(TwoTests.class, "dontSucceed");
		fMax.run(one);
		Request two= Request.aClass(TwoTests.class);
		List<Description> things= fMax.sortedLeavesForTest(two);
		Description succeed= Description.createTestDescription(TwoTests.class, "succeed");
		assertEquals(succeed, things.get(0));
		assertEquals(2, things.size());
	}
	
	@Test public void preferRecentlyFailed() {
		Request request= Request.aClass(TwoTests.class);
		fMax.run(request);
		List<Description> tests= fMax.sortedLeavesForTest(request);
		Description dontSucceed= Description.createTestDescription(TwoTests.class, "dontSucceed");
		assertEquals(dontSucceed, tests.get(0));
	}
	
	@Test public void sortTestsInMultipleClasses() {
		Request request= Request.classes(Computer.serial(), TwoTests.class, TwoTests.class);
		fMax.run(request);
		List<Description> tests= fMax.sortedLeavesForTest(request);
		Description dontSucceed= Description.createTestDescription(TwoTests.class, "dontSucceed");
		assertEquals(dontSucceed, tests.get(0));
		assertEquals(dontSucceed, tests.get(1));
	}	
	
	public static class TwoUnEqualTests {
		@Test public void slow() throws InterruptedException { Thread.sleep(100); }
		@Test public void fast() throws InterruptedException { Thread.sleep(50); }
	}
	
	@Test public void preferFast() {
		Request request= Request.aClass(TwoUnEqualTests.class);
		fMax.run(request);
		Description thing= fMax.sortedLeavesForTest(request).get(1);
		assertEquals(Description.createTestDescription(TwoUnEqualTests.class, "slow"), thing);
	}
	
	@Test public void remember() throws CouldNotReadCoreException {
		Request request= Request.aClass(TwoUnEqualTests.class);
		fMax.run(request);
		MaxCore reincarnation= MaxCore.forFolder(fMax.getFolder());
		try {
			Description thing= reincarnation.sortedLeavesForTest(request).get(1);
			assertEquals(Description.createTestDescription(TwoUnEqualTests.class, "slow"), thing);
		} finally {
			reincarnation.forget();
		}
	}
	
	@Test public void listenersAreCalledCorrectlyInTheFaceOfFailures() throws Exception {
		JUnitCore core= new JUnitCore();
		final List<Failure> failures= new ArrayList<Failure>();
		core.addListener(new RunListener() {
			@Override
			public void testRunFinished(Result result) throws Exception {
				failures.addAll(result.getFailures());
			}
		});
		fMax.run(Request.aClass(TwoTests.class), core);
		assertEquals(1, failures.size());
	}
	
	@Ignore
	@Test public void testsAreOnlyIncludedOnceWhenExpandingForSorting() throws Exception {
		Result result= fMax.run(Request.aClass(TwoTests.class));
		assertEquals(2, result.getRunCount());		
	}
	
	public static class TwoOldTests extends TestCase {
		public void testOne() {}
		public void testTwo() {}
	}
	
	@Ignore
	@Test public void junit3TestsAreOnlyIncludedOnceWhenExpandingForSorting() throws Exception {
		Result result= fMax.run(Request.aClass(TwoOldTests.class), new JUnitCore());
		assertEquals(2, result.getRunCount());		
	}
	
}
