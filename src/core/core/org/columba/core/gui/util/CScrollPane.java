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

import org.columba.core.util.Compatibility;

import javax.swing.*;
import java.awt.Component;

public class CScrollPane extends JScrollPane
{
	public CScrollPane()
	{
		enableWheelMouseSupport();
	}
	
	public CScrollPane( Component view )
	{
		super( view );
		
		enableWheelMouseSupport();
	}
	
	protected void enableWheelMouseSupport()
	{
		// The VM class constant "Boolean.TYPE" indicates the primitive type...
        // And we have to pass an object through as the value, but this is fine as it'll get unwrapped.
        Compatibility.simpleSetterInvoke(this, "setWheelScrollingEnabled", Boolean.TYPE, Boolean.TRUE );
	}
}
