package org.junit.runners;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.internal.runners.CompositeRunner;
import org.junit.internal.runners.InitializationError;
import org.junit.internal.runners.MethodValidator;
import org.junit.internal.runners.TestClass;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.ClassRoadie;
import org.junit.runner.notification.RunNotifier;

/** <p>The custom runner <code>Parameterized</code> implements parameterized
 * tests. When running a parameterized test class, instances are created for the
 * cross-product of the test methods and the test data elements.</p>
 * 
 * For example, to test a Fibonacci function, write:
 * <pre>
 * &#064;RunWith(Parameterized.class)
 * public class FibonacciTest {
 *    &#064;Parameters
 *    public static Collection<Object[]> data() {
 *          return Arrays.asList(new Object[][] { { 0, 0 }, { 1, 1 }, { 2, 1 },
 *             { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 } });
 *    }
 *
 *    private int fInput;
 *    private int fExpected;
 *
 *    public FibonacciTest(int input, int expected) {
 *       fInput= input;
 *       fExpected= expected;
 *    }
 *
 *    &#064;Test public void test() {
 *       assertEquals(fExpected, Fibonacci.compute(fInput));
 *    }
 * }
 * </pre>
 * 
 * <p>Each instance of <code>FibonacciTest</code> will be constructed using the two-argument
 * constructor and the data values in the <code>&#064;Parameters</code> method.</p>
 */
public class Parameterized extends CompositeRunner {
	static class TestClassRunnerForParameters extends JUnit4ClassRunner {
		private final Object[] fParameters;

		private final int fParameterSetNumber;

		private final Constructor<?> fConstructor;

		TestClassRunnerForParameters(TestClass testClass, Object[] parameters, int i) throws InitializationError {
			super(testClass.getJavaClass()); //todo
			fParameters= parameters;
			fParameterSetNumber= i;
			fConstructor= getOnlyConstructor();
		}

		@Override
		protected Object createTest() throws Exception {
			return fConstructor.newInstance(fParameters);
		}
		
		@Override
		protected String getName() {
			return String.format("[%s]", fParameterSetNumber);
		}
		
		@Override
		protected String testName(final Method method) {
			return String.format("%s[%s]", method.getName(), fParameterSetNumber);
		}

		private Constructor<?> getOnlyConstructor() {
			Constructor<?>[] constructors= getTestClass().getJavaClass().getConstructors();
			Assert.assertEquals(1, constructors.length);
			return constructors[0];
		}
		
		@Override
		protected void validate() throws InitializationError {
			// do nothing: validated before.
		}
		
		@Override
		public void run(RunNotifier notifier) {
			runMethods(notifier);
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Parameters {
	}
	
	private final TestClass fTestClass;

	public Parameterized(Class<?> klass) throws Exception {
		super(klass.getName());
		fTestClass= new TestClass(klass);
		
		MethodValidator methodValidator= new MethodValidator(fTestClass);
		methodValidator.validateStaticMethods();
		methodValidator.validateInstanceMethods();
		methodValidator.assertValid();
		
		int i= 0;
		for (final Object each : getParametersList()) {
			if (each instanceof Object[])
				add(new TestClassRunnerForParameters(fTestClass, (Object[])each, i++));
			else
				throw new Exception(String.format("%s.%s() must return a Collection of arrays.", fTestClass.getName(), getParametersMethod().getName()));
		}
	}
	
	@Override
	public void run(final RunNotifier notifier) {
		new ClassRoadie(notifier, fTestClass, getDescription(), new Runnable() {
			public void run() {
				runChildren(notifier);
			}
		}).runProtected();
	}
	
	private Collection<?> getParametersList() throws IllegalAccessException, InvocationTargetException, Exception {
		return (Collection<?>) getParametersMethod().invoke(null);
	}
	
	private Method getParametersMethod() throws Exception {
		List<Method> methods= fTestClass.getAnnotatedMethods(Parameters.class);
		for (Method each : methods) {
			int modifiers= each.getModifiers();
			if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers))
				return each;
		}

		throw new Exception("No public static parameters method on class " + getName());
	}

	public static Collection<Object[]> eachOne(Object... params) {
		List<Object[]> results= new ArrayList<Object[]>();
		for (Object param : params)
			results.add(new Object[] { param });
		return results;
	}
}

