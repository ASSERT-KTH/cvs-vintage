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
package org.columba.core.command;

import org.columba.ristretto.progress.ProgressObserver;

/**
 * 
 * Represents the clue between the gui and all the folders which want
 * to notify the statusbar.
 * 
 * <p>
 * We want the folders to be independent from the gui code. So, the
 * folders should communicate with the Observable, whereas the 
 * status observers with the Observable.
 * 
 * </p>
 * This makes it necessary of course to register as Observer.
 * 
 * </p>
 * This implementation of <class>StatusObserver</class> encapsulates
 * a <class>Worker</class>, which is more tightly coupled to the
 * gui in Columba.
 * 
 * @author fdietz
 */
public class StatusObservableImpl implements StatusObservable, ProgressObserver {

	/**
	 * encapsulated worker
	 */
	private Worker worker;

	public StatusObservableImpl() {

	}

	public StatusObservableImpl(Worker worker) {
		this.worker = worker;
	}

	/**
	 * set current value of progressbar
	 * @param i
	 */
	public void setCurrent(int i) {

		if (worker != null)
			worker.setProgressBarValue(i);

	}

	/**
	 * set maximum value of progressbar
	 * 
	 * @param i
	 */
	public void setMax(int i) {

		if (worker != null)
			worker.setProgressBarMaximum(i);

	}

	/**
	 * set message of statusbar
	 * 
	 * @param string
	 */
	public void setMessage(String string) {

		if (worker != null)
			worker.setDisplayText(string);
	}

	/**
	 * @return
	 */
	public Worker getWorker() {
		return worker;
	}

	/**
	 * @param worker
	 */
	public void setWorker(Worker worker) {
		this.worker = worker;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.StatusObservable#getCancelled()
	 */
	public boolean isCancelled() {
		return worker.cancelled();
	}
	
	public void cancel( boolean b )
	{
		worker.setCancel(b);
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.progress.ProgressObserver#maximumChanged(int)
	 */
	public void maximumChanged(int maximum) {
		if( worker != null) worker.setProgressBarMaximum( maximum );
	}

	/* (non-Javadoc)
	 * @see org.columba.ristretto.progress.ProgressObserver#valueChanged(int)
	 */
	public void valueChanged(int value) {
		if( worker != null) worker.setProgressBarValue( value );
	}

}
