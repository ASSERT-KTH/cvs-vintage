/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *******************************************************************************/
package org.eclipse.jdt.core;

/**
 * Represents an import declaration in Java compilation unit.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IImportDeclaration extends IJavaElement, ISourceReference, ISourceManipulation {
/**
 * Returns the name that has been imported. 
 * For an on-demand import, this includes the trailing <code>".*"</code>.
 * For example, for the statement <code>"import java.util.*"</code>,
 * this returns <code>"java.util.*"</code>.
 * For the statement <code>"import java.util.Hashtable"</code>,
 * this returns <code>"java.util.Hashtable"</code>.
 * 
 * @return the name that has been imported
 */
String getElementName();
/**
 * Returns the modifier flags for this import. The flags can be examined using class
 * <code>Flags</code>. Only the static flag is meaningful for import declarations.
 * <p>
 * Note: Static imports are an experimental language feature 
 * under discussion in JSR-201 and under consideration for inclusion
 * in the 1.5 release of J2SE. The support here is therefore tentative
 * and subject to change.
 * </p>
 * @exception JavaModelException if this element does not exist or if an
 *      exception occurs while accessing its corresponding resource.
 * @return the modifier flags for this import
 * @see Flags
 * @since 3.0
 */
int getFlags() throws JavaModelException;

/**
 * Returns whether the import is on-demand. An import is on-demand if it ends
 * with <code>".*"</code>.
 * @return true if the import is on-demand, false otherwise
 */
boolean isOnDemand();
}
