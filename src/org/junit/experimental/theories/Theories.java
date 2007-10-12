/**
 * 
 */
package org.junit.experimental.theories;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume.AssumptionViolatedException;
import org.junit.experimental.theories.PotentialAssignment.CouldNotGenerateValueException;
import org.junit.experimental.theories.internal.Assignments;
import org.junit.experimental.theories.internal.ParameterizedAssertionError;
import org.junit.internal.runners.JUnit4ClassRunner;
import org.junit.internal.runners.links.Notifier;
import org.junit.internal.runners.links.Statement;
import org.junit.internal.runners.model.InitializationError;
import org.junit.internal.runners.model.TestMethod;

@SuppressWarnings("restriction")
public class Theories extends JUnit4ClassRunner {
	public Theories(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected void collectInitializationErrors(List<Throwable> errors) {
	}

	@Override
	protected List<TestMethod> getTestMethods() {
		// TODO: (Jul 20, 2007 2:02:44 PM) Only get methods once, even if they
		// have both @Test and @Theory

		List<TestMethod> testMethods= super.getTestMethods();
		testMethods.addAll(getTestClass().getAnnotatedMethods(Theory.class));
		return testMethods;
	}

	@Override
	protected Notifier chain(final TestMethod method, Object test) {
		Statement next= invoke(method, test);
		next= ignoreViolatedAssumptions(next);
		next= possiblyExpectingExceptions(method, next);
		return notifying(method, next);
	}

	@Override
	protected TheoryAnchor invoke(TestMethod method, Object test) {
		return new TheoryAnchor(method);
	}

	public class TheoryAnchor extends Statement {
		private int successes= 0;

		private TestMethod fTestMethod;

		private List<AssumptionViolatedException> fInvalidParameters= new ArrayList<AssumptionViolatedException>();

		public TheoryAnchor(TestMethod method) {
			fTestMethod= method;
		}

		@Override
		public void evaluate() throws Throwable {
			runWithAssignment(Assignments.allUnassigned(
					fTestMethod.getMethod(), fTestMethod.getTestClass()
							.getJavaClass()));

			if (successes == 0)
				Assert
						.fail("Never found parameters that satisfied method.  Violated assumptions: "
								+ fInvalidParameters);
		}

		protected void runWithAssignment(Assignments parameterAssignment) throws Throwable {
			if (!parameterAssignment.isComplete()) {
				runWithIncompleteAssignment(parameterAssignment);
			} else {
				runWithCompleteAssignment(parameterAssignment);
			}
		}

		protected void runWithIncompleteAssignment(Assignments incomplete) throws InstantiationException,
				IllegalAccessException, Throwable {
			for (PotentialAssignment source : incomplete
					.potentialsForNextUnassigned()) {
				runWithAssignment(incomplete.assignNext(source));
			}
		}

		protected void runWithCompleteAssignment(final Assignments complete) throws InstantiationException,
				IllegalAccessException, InvocationTargetException,
				NoSuchMethodException, Throwable {
			try {
				final Object freshInstance= createTest();
				withAfters(fTestMethod, freshInstance, withBefores(fTestMethod, freshInstance, methodCompletesWithParameters(complete, freshInstance))).evaluate();
			} catch (CouldNotGenerateValueException e) {
				// Do nothing
			}
		}

		private Statement methodCompletesWithParameters(final Assignments complete,
				final Object freshInstance) {
			return new Statement() {
				@Override
				public void evaluate() throws Throwable {
					try {
						invokeWithActualParameters(freshInstance, complete);
					} catch (CouldNotGenerateValueException e) {
						// ignore 
						// TODO: (Oct 12, 2007 9:58:11 AM) Do I ignore this elsewhere?
					}
				}
			};
		}

		private void invokeWithActualParameters(Object target,
				Assignments complete) throws Throwable {
			final Object[] values= complete.getActualValues(nullsOk(), target);
			try {
				fTestMethod.invokeExplosively(target, values);
				successes++;
			} catch (AssumptionViolatedException e) {
				handleAssumptionViolation(e);
			} catch (Throwable e) {
				reportParameterizedError(e, values);
			}
		}

		protected void handleAssumptionViolation(AssumptionViolatedException e) {
			fInvalidParameters.add(e);
		}

		protected void reportParameterizedError(Throwable e, Object... params)
				throws Throwable {
			if (params.length == 0)
				throw e;
			throw new ParameterizedAssertionError(e, fTestMethod.getName(),
					params);
		}

		private boolean nullsOk() {
			Theory annotation= fTestMethod.getMethod().getAnnotation(
					Theory.class);
			if (annotation == null)
				return false;
			return annotation.nullsAccepted();
		}
	}
}