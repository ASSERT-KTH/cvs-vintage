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
package org.columba.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
