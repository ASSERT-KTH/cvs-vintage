/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * A class that represent a task that can be scheduled for one-shot or
 * repeated execution by a {@link TimerQueue}. <p>
 * A similar class is present in java.util package of jdk version >= 1.3;
 * for compatibility with jdk 1.2 we reimplemented it.
 *
 * @see TimerQueue
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.2 $
 */
public abstract class TimerTask 
	implements Executable, Comparable
{
	// Constants -----------------------------------------------------
	/* The state before the first execution */
	static final int NEW = 1;
	/* The state after first execution if the TimerTask is repeating */
	static final int SCHEDULED = 2;
	/* The state after first execution if the TimerTask is not repeating */
	static final int EXECUTED = 3;
	/* The state when cancelled */
	static final int CANCELLED = 4;

	// Attributes ----------------------------------------------------
	private final Object m_lock = new Object();
	private int m_state;
	private long m_period;
	private long m_nextExecutionTime;

	// Static --------------------------------------------------------

	// Constructors --------------------------------------------------
	/**
	 * Creates a TimerTask object that will be executed once.
	 */
	protected TimerTask() 
	{
		setState(NEW);
		m_period = 0;
		m_nextExecutionTime = 0;
	}
	/**
	 * Creates a TimerTask object that will be executed every <code>period</code>
	 * milliseconds. <p>
	 * @param period the execution period; if zero, will be executed only once.
	 */
	protected TimerTask(long period) 
	{
		this();
		if (period < 0) throw new IllegalArgumentException("Period can't be negative");
		m_period = period;
	}

	// Public --------------------------------------------------------
	/**
	 * Cancels the next execution of this TimerTask (if any). <br>
	 * If the TimerTask is executing it will prevent the next execution (if any).
	 * @returns true if one or more scheduled execution will not take place,
	 * false otherwise.
	 */
	public boolean cancel() 
	{
		synchronized (getLock()) 
		{
			boolean ret = (getState() == SCHEDULED);
			setState(CANCELLED);
			return ret;
		}
	}

	// Executable implementation ---------------------------------------
	/**
	 * The task to be executed, to be implemented in subclasses.
	 */
	public abstract void execute() throws Exception;
	// Comparable implementation ---------------------------------------
	/**
	 * A TimerTask is less than another if it will be scheduled before.
	 */
	public int compareTo(Object other)
	{
		if (!(other instanceof TimerTask)) throw new IllegalArgumentException("Can't compare a TimerTask with something else");
		if (other == this) return 0;
		// Avoid deadlock
		TimerTask one = this;
		TimerTask two = (TimerTask)other;
		boolean swapped = false;
		if (one.hashCode() > two.hashCode())
		{
			one = two;
			two = this;
			swapped = true;
		}
		synchronized (one) 
		{
			synchronized (two)
			{
				int res = (int)(one.getNextExecutionTime() - two.getNextExecutionTime());
				if (swapped) {return -res;}
				else {return res;}
			}
		}
	}

	// Y overrides ---------------------------------------------------

	// Package protected ---------------------------------------------
	/* Returns the mutex that syncs the access to this object */
	Object getLock() 
	{
		return m_lock;
	}
	/* Sets the state of execution of this TimerTask */
	void setState(int state) 
	{
		synchronized (getLock())
		{
			m_state = state;
		}
	}
	/* Returns the state of execution of this TimerTask */
	int getState() 
	{
		synchronized (getLock())
		{
			return m_state;
		}
	}
	/* Returns whether this TimerTask is periodic */
	boolean isPeriodic() 
	{
		synchronized (getLock())
		{
			return m_period > 0;
		}
	}
	/* Returns the next execution time for this TimerTask */
	long getNextExecutionTime() 
	{
		synchronized (getLock())
		{
			return m_nextExecutionTime;
		}
	}
	/* Sets the next execution time for this TimerTask */
	void setNextExecutionTime(long time) 
	{
		synchronized (getLock())
		{
			m_nextExecutionTime = time;
		}
	}

	// Protected -----------------------------------------------------
	/** Returns the period of this TimerTask */
	protected long getPeriod() 
	{
		synchronized (getLock())
		{
			return m_period;
		}
	}

	// Private -------------------------------------------------------

	// Inner classes -------------------------------------------------
}
