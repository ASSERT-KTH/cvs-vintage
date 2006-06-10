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
package org.columba.api.command;



public interface IWorkerStatusController {
    /**
 * Set the text to be displayed in the status bar
 * @param text        Text to display in status bar
 */
    public void setDisplayText(String text);

    /**
 * Returns the text currently displayed in the status bar
 */
    public String getDisplayText();

    /**
 * Clears the text displayed in the status bar - without any delay
 */
    public void clearDisplayText();

    /**
 * Clears the text displayed in the status bar - with a given delay.
 * The delay used is 500 ms.
 * <br>
 * If a new text is set within this delay, the text is not cleared.
 */
    public void clearDisplayTextWithDelay();

    /**
 * Sets the maximum value for the progress bar.
 * @param max                New max. value for progress bar
 */
    public void setProgressBarMaximum(int max);

    /**
 * Sets the current value of the progress bar.
 * @param value                New current value of progress bar
 */
    public void setProgressBarValue(int value);

    /**
 * Sets the progress bar value to zero, i.e. clears the progress bar.
 * This is the same as calling setProgressBarValue(0)
 */
    public void resetProgressBar();

    /**
 * Returns the max. value for the progress bar
 */
    public int getProgessBarMaximum();

    /**
 * Returns the current value for the progress bar
 */
    public int getProgressBarValue();

    public void cancel();

    public boolean cancelled();

    public void addWorkerStatusChangeListener(IWorkerStatusChangeListener l);

    public int getTimeStamp();

	/**
	 * @param listener
	 */
	public void removeWorkerStatusChangeListener(IWorkerStatusChangeListener listener);
}
