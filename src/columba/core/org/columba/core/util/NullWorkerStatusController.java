package org.columba.core.util;

import org.columba.core.command.WorkerStatusController;

/**
 * @author timo
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class NullWorkerStatusController implements WorkerStatusController {

	
	private static NullWorkerStatusController myInstance;
	
	public static NullWorkerStatusController getInstance() {
		if(myInstance == null) {
			myInstance = new NullWorkerStatusController();
		}
		
		return myInstance;
	}
	
	protected NullWorkerStatusController() {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#setDisplayText(java.lang.String)
	 */
	public void setDisplayText(String text) {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#getDisplayText()
	 */
	public String getDisplayText() {
		return null;
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#setProgressBarMaximum(int)
	 */
	public void setProgressBarMaximum(int max) {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#setProgressBarValue(int)
	 */
	public void setProgressBarValue(int value) {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#incProgressBarValue()
	 */
	public void incProgressBarValue() {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#incProgressBarValue(int)
	 */
	public void incProgressBarValue(int increment) {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#getProgessBarMaximum()
	 */
	public int getProgessBarMaximum() {
		return 0;
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#getProgressBarValue()
	 */
	public int getProgressBarValue() {
		return 0;
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#cancel()
	 */
	public void cancel() {
	}

	/**
	 * @see org.columba.core.command.WorkerStatusController#cancelled()
	 */
	public boolean cancelled() {
		return false;
	}

}
