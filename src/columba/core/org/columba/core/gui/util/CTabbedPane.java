// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
