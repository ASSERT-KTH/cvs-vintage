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
package org.eclipse.jdt.core.search;

/**
 * A search pattern defines how search results are found. Use <code>SearchEngine.createSearchPattern</code>
 * to create a search pattern.
 *
 * @see SearchEngine#createSearchPattern(IJavaElement, int)
 * @see SearchEngine#createSearchPattern(String, int, int, boolean)
 * TODO (jerome) deprecate this interface - should use SearchPattern instead
 */
public interface ISearchPattern {
	// used as a marker interface: contains no methods
}
