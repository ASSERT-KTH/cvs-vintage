/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 * Interface for the execution of a task. <p>
 * 
 * @see WorkerQueue
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.2 $
 */
public interface Executable
{
	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------

	// Public --------------------------------------------------------
	/**
	 * Executes the implemented task.
	 */
	public void execute() throws Exception;
}
