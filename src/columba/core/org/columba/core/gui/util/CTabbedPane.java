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
package org.columba.core.gui.util;

import java.lang.reflect.Field;

import javax.swing.JTabbedPane;

import org.columba.core.util.Compatibility;

public class CTabbedPane extends JTabbedPane
{
	public CTabbedPane()
	{
		setLayoutPolicy();
	}
	
	protected void setLayoutPolicy()
	{
        Integer fieldValue = null;

        // Get the value of SCROLL_TAB_LAYOUT layout (if it exists)
		Field layoutField = Compatibility.getObjectField(this, "SCROLL_TAB_LAYOUT");

        // If the field is available we can use it 8-)
        if (layoutField != null)
        {
            try
            {
                fieldValue = new Integer(layoutField.getInt(this));
                Compatibility.simpleSetterInvoke(this, "setTabLayoutPolicy", Integer.TYPE, fieldValue);
            }
            catch (Exception e)
            {
                System.err.println("Failed to get a value for SCROLL_TAB_LAYOUT");
            }
        }
	}

}
