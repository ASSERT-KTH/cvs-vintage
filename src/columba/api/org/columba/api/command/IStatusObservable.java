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
 * <p>
 * This makes it necessary of course to register as Observer.
 *
 * @author fdietz
 */
public interface IStatusObservable {
    /**
 * Sets the current value of the progress bar.
 * @param i                New current value of progress bar
 */
    public void setCurrent(int i);

    /**
 * Sets the maximum value for the progress bar.
 * @param i                New max. value for progress bar
 */
    public void setMax(int i);

    /**
 * Sets the progress bar value to zero, i.e. clears the progress bar.
 * This is the same as calling setCurrent(0)
 */
    public void resetCurrent();

    public boolean isCancelled();

    public void cancel(boolean b);

    /**
 * Set the text to be displayed in the status bar
 * @param string        Text to display in status bar
 */
    public void setMessage(String string);

    /**
 * Clears the text displayed in the status bar.
 */
    public void clearMessage();

    /**
 * Clears the text displayed in the status bar - with a given delay.
 * The delay used is 500 ms.
 * <br>
 * If a new text is set within this delay, the text is not cleared.
 */
    public void clearMessageWithDelay();
}
