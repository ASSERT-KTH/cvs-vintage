/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A variable binding represents either a field of a class or interface, or 
 * a local variable declaration (including formal parameters, local variables, 
 * and exception variables).
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see ITypeBinding#getDeclaredFields()
 * @since 2.0
 */
public interface IVariableBinding extends IBinding {
	
	/**
	 * Returns whether this binding is for a field or for a local variable.
	 * 
	 * @return <code>true</code> if this is the binding for a field,
	 *    and <code>false</code> if this is the binding for a local variable
	 */ 
	public boolean isField();
	
	/**
	 * Returns the name of the field or local variable declared in this binding.
	 * The name is always a simple identifier.
	 * 
	 * @return the name of this field or local variable
	 */
	public String getName();
	
	/**
	 * Returns the type binding representing the class or interface
	 * that declares this field.
	 * <p>
	 * The declaring class of a field is the class or interface of which it is
	 * a member. Local variables have no declaring class. The field length of an 
	 * array type has no declaring class.
	 * </p>
	 * 
	 * @return the binding of the class or interface that declares this field,
	 *   or <code>null</code> if none
	 */
	public ITypeBinding getDeclaringClass();

	/**
	 * Returns the binding for the type of this field or local variable.
	 * 
	 * @return the binding for the type of this field or local variable
	 */
	public ITypeBinding getType();
	
	/**
	 * Returns a small integer variable id for this variable binding.
	 * <p>
	 * <b>Local variables inside methods:</b> Local variables (and parameters)
	 * declared within a single method are assigned ascending ids in normal
	 * code reading order; var1.getVariableId()&lt;var2.getVariableId() means that var1 is
	 * declared before var2.
	 * </p>
	 * <p>
	 * <b>Local variables outside methods:</b> Local variables declared in a
	 * type's static initializers (or initializer expressions of static fields)
	 * are assigned ascending ids in normal code reading order. Local variables
	 * declared in a type's instance initializers (or initializer expressions
	 * of non-static fields) are assigned ascending ids in normal code reading
	 * order. These ids are useful when checking definite assignment for
	 * static initializers (JLS 16.7) and instance initializers (JLS 16.8), 
	 * respectively.
	 * </p>
	 * <p>
	 * <b>Fields:</b> Fields declared as members of a type are assigned 
	 * ascending ids in normal code reading order; 
	 * field1.getVariableId()&lt;field2.getVariableId() means that field1 is declared before
	 * field2.
	 * </p>
	 * 
	 * @return a small non-negative variable id
	 */
	public int getVariableId();
	
	/**
	 * Returns this binding's constant value if it has one.
	 * Some variables may have a value computed at compile-time. If the type of
	 * the value is a primitive type, the result is the boxed equivalent (i.e.,
	 * int returned as an <code>Integer</code>). If the type of the value is
	 * <code>String</code>, the result is the string itself. If the variable has
	 * no compile-time computed value, the result is <code>null</code>.
	 * (Note: compile-time constant expressions cannot denote <code>null</code>;
	 * JLS2 15.28.)
	 * 
	 * @return the constant value, or <code>null</code> if none
	 * @since 3.0
	 */
	public Object getConstantValue();
}
