// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.frame;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.selection.SelectionManager;

/**
 * Mediator is reponsible for managing all the interaction between the
 * components found in a {@link AbstractFrameView}.
 * <p>
 * Following an introduction in the Mediator Pattern:
 * <p>
 * When a program is made up of a number of classes, the logic and computation
 * is divided logically among these classes. However, as more of these isolated
 * classes are developed in a program, the problem of communication between
 * these classes become more complex. The more each class needs to know about
 * the methods of another class, the more tangled the class structure can
 * become. This makes the program harder to read and harder to maintain.
 * Further, it can become difficult to change the program, since any change may
 * affect code in several other classes. <bre>The Mediator pattern addresses
 * this problem by promoting looser coupling between these classes. Mediators
 * accomplish this by being the only class that has detailed knowledge of the
 * methods of other classes. Classes send inform the mediator when changes occur
 * and the Mediator passes them on to any other classes that need to be
 * informed.
 * 
 * @author fdietz
 */
public interface FrameMediator {

	/**
	 * forwarded from Container.getViewItem()
	 * 
	 * @return
	 */
	public ViewItem getViewItem();

	public SelectionManager getSelectionManager();

	/**
	 * Loads stored information about the previous size and location of the
	 * view.
	 */
	void loadPositions(ViewItem viewItem);

	/**
	 * Saves information about the current size and location of the view
	 */
	void savePositions(ViewItem viewItem);

	public Container getContainer();

	/**
	 * TODO (@author fdietz): adapter only --> will be removed!
	 * 
	 * @deprecated
	 * 
	 * @return
	 */
	public Container getView();

	public void setContainer(Container c);

	String getString(String sPath, String sName, String sID);
	
	public ContentPane getContentPane();
}