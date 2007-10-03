package org.junit.internal.runners;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.Assume.AssumptionViolatedException;
import org.junit.Test.None;

public class TestMethod extends JavaElement {
	private final Method fMethod;

	private TestClass fTestClass;

	public TestMethod(Method method, TestClass testClass) {
		fMethod= method;
		fTestClass= testClass;
	}

	public boolean isIgnored() {
		return getMethod().getAnnotation(Ignore.class) != null;
	}

	public long getTimeout() {
		Test annotation= getMethod().getAnnotation(Test.class);
		if (annotation == null)
			return 0;
		long timeout= annotation.timeout();
		return timeout;
	}

	protected Class<? extends Throwable> getExpectedException() {
		Test annotation= getMethod().getAnnotation(Test.class);
		if (annotation == null || annotation.expected() == None.class)
			return null;
		else
			return annotation.expected();
	}

	boolean isUnexpected(Throwable exception) {
		return !getExpectedException().isAssignableFrom(exception.getClass());
	}

	boolean expectsException() {
		return getExpectedException() != null;
	}

	@Override
	public List<Method> getBefores() {
		return fTestClass.getAnnotatedMethods(Before.class);
	}

	@Override
	public List<Method> getAfters() {
		return fTestClass.getAnnotatedMethods(After.class);
	}

	protected void invoke(Roadie context) throws Throwable {
		invoke(context.getTarget());
	}
	
	// TODO: (Aug 6, 2007 3:01:35 PM) This should not go away.  Write a test for overriding it.

	protected void invoke(Object target) throws Throwable {
		ExplosiveMethod.from(getMethod()).invoke(target);
	}

	protected void runTestUnprotected(final Roadie context) {
		try {
			invoke(context);

			if (expectsException())
				context.addFailure(new AssertionError("Expected exception: "
						+ getExpectedException().getName()));
		} catch (Throwable e) {
			if (e instanceof AssumptionViolatedException) {
				// do nothing
			} else if (!expectsException())
				context.addFailure(e);
			else if (isUnexpected(e)) {
				String message= "Unexpected exception, expected<"
						+ getExpectedException().getName() + "> but was<"
						+ e.getClass().getName() + ">";
				context.addFailure(new Exception(message, e));
			}
		}
	}

	void runWithTimeout(final Roadie context, final long timeout) {
		context.runProtected(this, new Runnable() {
			public void run() {
				ExecutorService service= Executors.newSingleThreadExecutor();
				Callable<Object> callable= new Callable<Object>() {
					public Object call() throws Exception {
						runTestUnprotected(context);
						return null;
					}
				};
				Future<Object> result= service.submit(callable);
				service.shutdown();
				try {
					boolean terminated= service.awaitTermination(timeout,
							TimeUnit.MILLISECONDS);
					if (!terminated)
						service.shutdownNow();
					result.get(0, TimeUnit.MILLISECONDS); // throws the
															// exception if one
															// occurred during
															// the invocation
				} catch (TimeoutException e) {
					context.addFailure(new Exception(String.format(
							"test timed out after %d milliseconds", timeout)));
				} catch (Exception e) {
					context.addFailure(e);
				}
			}
		});
	}

	void run(Roadie context) {
		if (isIgnored()) {
			context.fireTestIgnored();
			return;
		}
		context.fireTestStarted();
		try {
			long timeout= getTimeout();
			if (timeout > 0)
				runWithTimeout(context, timeout);
			else
				runTestProtected(context);
		} finally {
			context.fireTestFinished();
		}
	}

	protected void runTestProtected(final Roadie context) {
		context.runProtected(this, new Runnable() {
			public void run() {
				runTestUnprotected(context);
			}
		});
	}

	public Method getMethod() {
		return fMethod;
	}
}