//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
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
