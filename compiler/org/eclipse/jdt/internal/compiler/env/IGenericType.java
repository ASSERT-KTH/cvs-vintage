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
package org.eclipse.jdt.internal.compiler.env;

public interface IGenericType extends IDependent {

	// Type decl kinds
	int CLASS_DECL = 1;
	int INTERFACE_DECL = 2;
	int ENUM_DECL = 3;	
	int ANNOTATION_TYPE_DECL = 4;
	
/**
 * Returns the kind of this type CLASS, INTERFACE, ENUM, ANNOTATION_TYPE
 */
int getKind();

/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * NOTE 1: We have added AccDeprecated & AccSynthetic.
 * NOTE 2: If the receiver represents a member type, the modifiers are extracted from its inner class attributes.
 */
int getModifiers();
/**
 * Answer whether the receiver contains the resolved binary form
 * or the unresolved source form of the type.
 */

boolean isBinaryType();
}
