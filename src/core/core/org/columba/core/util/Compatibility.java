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

package org.columba.core.util;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class Compatibility
{
	public static void simpleSetterInvoke(
		Object target,
		String methodName,
		Class argType,
		Object argValue)
	{
		Class argTypes[] = new Class[]{argType};
		Object argValues[] = new Object[]{argValue};

		// Now we try to invoke the method and see what happens
		try
		{
			Method actionMethod = target.getClass().getMethod(methodName, argTypes);
			actionMethod.invoke(target, argValues);
		}
		// We could get any number of exceptions... unless we want to trap an underlying exception (with
		// "InvocationTargetException" we might aswell put any problem down to an incompatibility.
		catch (Exception e)
		{
			System.err.println(
				"Incompatible " + methodName + " call on " + target.getClass().getName() + " failed");
		}
	}

	public static Field getObjectField( Object target, String fieldName)
	{
		Field resultField = null;
		// Get the value of the constant
		try
		{
			resultField = target.getClass().getField(fieldName);
		}
		// We could get any number of exceptions... unless we want to trap an underlying exception (with
		// "InvocationTargetException" we might aswell put any problem down to an incompatibility.
		catch (Exception e)
		{
			System.err.println(
				"Incompatible field " + fieldName + " call on " + target.getClass().getName() + " failed");
		}
		return resultField;
	}
}
