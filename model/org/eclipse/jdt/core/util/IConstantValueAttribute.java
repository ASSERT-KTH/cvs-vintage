/**********************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 *********************************************************************/
package org.eclipse.jdt.core.util;

/**
 * Description of a constant value attribute as described in the JVM 
 * specifications.
 *  
 * @since 2.0
 */
public interface IConstantValueAttribute extends IClassFileAttribute {
	
	/**
	 * Answer back the constant value index.
	 * 
	 * @return <CODE>int</CODE>
	 */
	int getConstantValueIndex();
	
	/**
	 * Answer back the constant pool entry that represents the constant
	 * value of this attribute.
	 * 
	 * @return org.eclipse.jdt.core.util.IConstantPoolEntry
	 */
	IConstantPoolEntry getConstantValue();
}
