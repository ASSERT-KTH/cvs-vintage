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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.JProgressBar;

public class CProgressBar extends JProgressBar
{
	public CProgressBar()
	{
		super();
	}

	public void setIndeterminate(boolean status)
	{
		try
		{
			Method method = getClass().getSuperclass().getMethod("setIndeterminate",new Class[]{Boolean.TYPE});
			method.invoke(this,new Object[]{new Boolean(status)});
		}
		catch(NoSuchMethodException nsme){}
		catch(IllegalAccessException iae){}
		catch(InvocationTargetException ite){}
	}
}
