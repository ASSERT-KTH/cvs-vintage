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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//Portions created by Celso Pinto are Copyright (C) 2004.
//
//All Rights Reserved.
package org.columba.core.gui.util;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


/**
 * Class that handles double-click event. On double-click the abstract
 * method doubleClick is executed and should be implemented by subclasses.
 * 
 * @author Celso Pinto &lt;cpinto@yimports.com&gt;
 */
public abstract class DoubleClickListener implements MouseListener
{

  public final void mouseClicked(MouseEvent event)
  {
  	if (event.getButton() == MouseEvent.BUTTON1 &&
  	    event.getClickCount() > 1)
  	{
  		doubleClick(event);  
  	}
  }

  public abstract void doubleClick(MouseEvent event);
  
  public final void mouseEntered(MouseEvent event)
  {}

  public final void mouseExited(MouseEvent event)
  {}

  public final void mousePressed(MouseEvent event)
  {}

  public final void mouseReleased(MouseEvent event)
  {}

}
