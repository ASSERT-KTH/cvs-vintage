package org.eclipse.jface.operation;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.ProgressMonitorWrapper;
import org.eclipse.jface.util.Assert;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility class for supporting modal operations.
 * The runnable passed to the <code>run</code> method is executed in a
 * separate thread, depending on the value of the passed fork argument. 
 * If the runnable is executed in a separate thread then the current thread
 * either waits until the new thread ends or, if the current thread is the 
 * UI thread, it polls the SWT event queue and dispatches each event.
 * <p>
 * This class is not intended to be subclassed.
 * </p>
 */
public class ModalContext {

	/**
	 * Indicated whether ModalContext is in debug mode;
	 * <code>false</code> by default.
	 */
	private static boolean debug = false;
	
	/**
	 * The number of nested modal runs, or 0 if not inside a modal run.
	 * This is global state.
	 */
	private static int modalLevel = 0;
	
	/**
	 * Indicates whether operations should be run in a separate thread.
	 * Defaults to true.
	 * For internal debugging use, set to false to run operations in the calling thread.
	 */
	private static boolean runInSeparateThread = true;

	/**
	 * Thread which runs the modal context.
	 */
	private static class ModalContextThread extends Thread {
		/**
		 * The operation to be run.
		 */
		private IRunnableWithProgress runnable;
		
		/** 
		 * The exception thrown by the operation starter.
		 */
		private Throwable throwable;
		
		/**
		 * The progress monitor used for progress and cancelation.
		 */
		private IProgressMonitor progressMonitor;
		
		/**
		 * The display used for event dispatching.
		 */
		private Display display;
		
		/**
		 * Indicates whether to continue event queue dispatching.
		 */
		private volatile boolean continueEventDispatching = true;

		/**
		 * Creates a new modal context.
		 * 
		 * @param operation the runnable to run
		 * @param monitor the progress monitor to use to display progress and receive
		 *   requests for cancelation
		 * @param display the display to be used to read and dispatch events
		 */
		private ModalContextThread(IRunnableWithProgress operation, IProgressMonitor monitor, Display display) {
			super("ModalContext"); //$NON-NLS-1$
			Assert.isTrue(monitor != null && display != null);
			runnable = operation;
			progressMonitor = new AccumulatingProgressMonitor(monitor, display);
			this.display = display;
		}
		/* (non-Javadoc)
		 * Method declared on Thread.
		 */
		public void run() {
			try {
				if (runnable != null)
					runnable.run(progressMonitor);
			} catch (InvocationTargetException e) {
				throwable= e;
			} catch (InterruptedException e) {
				throwable= e;
			} catch (RuntimeException e) {
				throwable= e;
			} catch (ThreadDeath e) {
				// Make sure to propagate ThreadDeath, or threads will never fully terminate
				throw e;
			} catch (Error e) {
				throwable= e;
			} finally {
				// Make sure that all events in the asynchronous event queue
				// are dispatched.
				display.syncExec(new Runnable() {
					public void run() {
						// do nothing
					}
				});
				
				// Stop event dispatching
				continueEventDispatching= false;
				
				// Force the event loop to return from sleep () so that
				// it stops event dispatching.
				display.asyncExec(null);
			}	
		}
		/**
		 * Processes events or waits until this modal context thread terminates.
		 */
		public void block() {
			if (display == Display.getCurrent()) {
				while (continueEventDispatching) {
					if (!display.readAndDispatch())
						display.sleep();
				}
			} else {
				try {
					join();
				} catch (InterruptedException e) {
				}
			}
		}		
	}
/**
 * Returns whether the first progress monitor is the same as, or
 * a wrapper around, the second progress monitor.
 *
 * @param monitor1 the first progress monitor
 * @param monitor2 the second progress monitor
 * @return <code>true</code> if the first is the same as, or
 *   a wrapper around, the second
 * @see ProgressMonitorWrapper
 */
public static boolean canProgressMonitorBeUsed(IProgressMonitor monitor1, IProgressMonitor monitor2) {
	if (monitor1 == monitor2)
		return true;
		
	while (monitor1 instanceof ProgressMonitorWrapper) {
		monitor1= ((ProgressMonitorWrapper)monitor1).getWrappedProgressMonitor();
		if (monitor1 == monitor2)
			return true;
	}
	return false;
}
/**
 * Checks with the given progress monitor and throws 
 * <code>InterruptedException</code> if it has been canceled.
 * <p>
 * Code in a long-running operation should call this method
 * regularly so that a request to cancel will be honored.
 * </p>
 * <p>
 * Convenience for:
 * <pre>
 * if (monitor.isCanceled())
 *    throw new InterruptedException();
 * </pre>
 * </p>
 *
 * @param monitor the progress monitor
 * @exception InterruptedException if cancelling the operation has been requested
 * @see IProgressMonitor#isCanceled
 */
public static void checkCanceled(IProgressMonitor monitor) throws InterruptedException {
	if (monitor.isCanceled())
		throw new InterruptedException();
}
/**
 * Returns the currently active modal context thread, or null if no modal context is active.
 */
private static ModalContextThread getCurrentModalContextThread() {
	Thread t = Thread.currentThread();
	if (t instanceof ModalContextThread)
		return (ModalContextThread) t;
	return null;
}
/**
 * Returns the modal nesting level.
 * <p>
 * The modal nesting level increases by one each time the
 * <code>ModalContext.run</code> method is called within the
 * dynamic scope of another call to <code>ModalContext.run</code>.
 * </p>
 *
 * @return the modal nesting level, or <code>0</code> if 
 *  this method is called outside the dynamic scope of any
 *  invocation of <code>ModalContext.run</code>
 */
public static int getModalLevel() {
	return modalLevel;
}
/**
 * Returns whether the given thread is running a modal context.
 *
 * @return <code>true</code> if the given thread is running a modal context, <code>false</code> if not
 */
public static boolean isModalContextThread(Thread thread) {
	return thread instanceof ModalContextThread;
}
/**
 * Runs the given runnable in a modal context, passing it a progress monitor.
 * <p>
 * The modal nesting level is increased by one from the perspective
 * of the given runnable.
 * </p>
 *
 * @param operation the runnable to run
 * @param fork <code>true</code> if the runnable should run in a separate thread,
 *   and <code>false</code> if in the same thread
 * @param monitor the progress monitor to use to display progress and receive
 *   requests for cancelation
 * @param display the display to be used to read and dispatch events
 * @exception InvocationTargetException if the run method must propagate a checked exception,
 * 	it should wrap it inside an <code>InvocationTargetException</code>; runtime exceptions and errors are automatically
 *  wrapped in an <code>InvocationTargetException</code> by this method
 * @exception InterruptedException if the operation detects a request to cancel, 
 *  using <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing 
 *  <code>InterruptedException</code>; this method propagates the exception
 */
public static void run(IRunnableWithProgress operation, boolean fork, IProgressMonitor monitor, Display display) throws InvocationTargetException, InterruptedException {
	Assert.isTrue(operation != null && monitor != null);
		
	modalLevel++;
	try {
		if (monitor != null)
			monitor.setCanceled(false);
		// Is the runnable supposed to be execute in the same thread.
		if (!fork || !runInSeparateThread) {
			runInCurrentThread(operation, monitor);
		} else {
			ModalContextThread t = getCurrentModalContextThread();
			if (t != null) {
				Assert.isTrue(canProgressMonitorBeUsed(monitor, t.progressMonitor));
				runInCurrentThread(operation, monitor);
			} else {
				t = new ModalContextThread(operation, monitor, display);
				t.start();
				t.block();
				Throwable throwable= t.throwable;
				if (throwable != null) {
					if (debug
						&& !(throwable instanceof InterruptedException)
						&& !(throwable instanceof OperationCanceledException)) {
						System.err.println("Exception in modal context operation:"); //$NON-NLS-1$
						throwable.printStackTrace();
						System.err.println("Called from:"); //$NON-NLS-1$
						// Don't create the InvocationTargetException on the throwable,
						// otherwise it will print its stack trace (from the other thread).
						new InvocationTargetException(null).printStackTrace();
					}
					if (throwable instanceof InvocationTargetException) {
						throw (InvocationTargetException) throwable;
					} else if (throwable instanceof InterruptedException) {
						throw (InterruptedException) throwable;
					} else if (
						throwable instanceof OperationCanceledException) {
						// See 1GAN3L5: ITPUI:WIN2000 - ModalContext converts OperationCancelException into InvocationTargetException
						throw new InterruptedException(throwable.getMessage());
					} else {
						throw new InvocationTargetException(throwable);
					}	
				}
			}
		}
	}
	finally {
		modalLevel--;
	}
}
/**
 * Run a runnable.  Convert all thrown exceptions to 
 * either InterruptedException or InvocationTargetException
 */
private static void runInCurrentThread(IRunnableWithProgress runnable, IProgressMonitor progressMonitor) 
	throws InterruptedException, InvocationTargetException
{
	try {
		if (runnable != null)
			runnable.run(progressMonitor);
	} catch (InvocationTargetException e) {
		throw e;
	} catch (InterruptedException e) {
		throw e;
	} catch (OperationCanceledException e) {
		throw new InterruptedException();
	} catch (ThreadDeath e) {
		// Make sure to propagate ThreadDeath, or threads will never fully terminate
		throw e;
	} catch (RuntimeException e) {
		throw new InvocationTargetException(e);
	} catch (Error e) {
		throw new InvocationTargetException(e);
	}
}
/**
 * Sets whether ModalContext is running in debug mode.
 *
 * @param debugMode <code>true</code> for debug mode, 
 *  and <code>false</code> for normal mode (the default)
 */
public static void setDebugMode(boolean debugMode) {
	debug = debugMode;
}
}
