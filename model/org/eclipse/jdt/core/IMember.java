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
package org.eclipse.jdt.core;

/**
 * Common protocol for Java elements that can be members of types.
 * This set consists of <code>IType</code>, <code>IMethod</code>, 
 * <code>IField</code>, and <code>IInitializer</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IMember extends IJavaElement, ISourceReference, ISourceManipulation, IParent {
/**
 * Returns the class file in which this member is declared, or <code>null</code>
 * if this member is not declared in a class file (for example, a source type).
 * This is a handle-only method.
 * 
 * @return the class file in which this member is declared, or <code>null</code>
 * if this member is not declared in a class file (for example, a source type)
 */
IClassFile getClassFile();
/**
 * Returns the compilation unit in which this member is declared, or <code>null</code>
 * if this member is not declared in a compilation unit (for example, a binary type).
 * This is a handle-only method.
 * 
 * @return the compilation unit in which this member is declared, or <code>null</code>
 * if this member is not declared in a compilation unit (for example, a binary type)
 */
ICompilationUnit getCompilationUnit();
/**
 * Returns the type in which this member is declared, or <code>null</code>
 * if this member is not declared in a type (for example, a top-level type).
 * This is a handle-only method.
 * 
 * @return the type in which this member is declared, or <code>null</code>
 * if this member is not declared in a type (for example, a top-level type)
 */
IType getDeclaringType();
/**
 * Returns the modifier flags for this member. The flags can be examined using class
 * <code>Flags</code>.
 * <p>
 * Note that only flags as indicated in the source are returned. Thus if an interface
 * defines a method <code>void myMethod();</code> the flags don't include the
 * 'public' flag.
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the modifier flags for this member
 * @see Flags
 */
int getFlags() throws JavaModelException;
/**
 * Returns the source range of this member's simple name,
 * or <code>null</code> if this member does not have a name
 * (for example, an initializer), or if this member does not have
 * associated source code (for example, a binary type).
 *
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the source range of this member's simple name,
 * or <code>null</code> if this member does not have a name
 * (for example, an initializer), or if this member does not have
 * associated source code (for example, a binary type)
 */
ISourceRange getNameRange() throws JavaModelException;
/**
 * Returns the local or anonymous type declared in this source member with the given simple name and/or
 * with the specified position relative to the order they are defined in the source.
 * The name is empty if it is an anonymous type.
 * Numbering starts at 1 (thus the first occurrence is occurrence 1, not occurrence 0).
 * This is a handle-only method. The type may or may not exist.
 * Throws a <code>RuntimeException</code> if this member is not a source member.
 * 
 * @param name the given simple name
 * @param occurrenceCount the specified position
 * @return the type with the given name and/or with the specified position relative to the order they are defined in the source
 * @since 3.0
 */
IType getType(String name, int occurrenceCount);
/**
 * Returns whether this member is from a class file.
 * This is a handle-only method.
 *
 * @return <code>true</code> if from a class file, and <code>false</code> if
 *   from a compilation unit
 */
boolean isBinary();
}
