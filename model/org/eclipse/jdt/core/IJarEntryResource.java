/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.core.resources.IStorage;

/**
 * A jar entry corresponding to a non-Java resource in an archive {@link IPackageFragment} or {@link IPackageFragmentRoot}.
 * <p>
 * One can navigate the non-Java resource tree using the {@link #getChildren()} and {@link #getParent()} methods.
 * Jar entry resources are either files ({@link #isFile()} returns true) or directories ({@link #isFile()} retunrs false).
 * Files don't have any children and the returned array is always empty.
 * </p><p>
 * Jar entry resources that refer to the same element are guaranteed to be equal, but not necessarily identical.
 * <p>
 * 
 * @since 3.3
 */
public interface IJarEntryResource extends IStorage {
	
	/**
	 * Returns the list of children of this jar entry resource.
	 * Returns an empty array if this jar entry is a file, or if this jar entry is a directory and it has no children.
	 * 
	 * @return the children of this jar entry resource
	 */
	IJarEntryResource[] getChildren();
	
	/**
	 * Returns the parent of this jar entry resource. This is either an {@link IJarEntryResource}, an {@link IPackageFragment}
	 * or an {@link IPackageFragmentRoot}.
	 * 
	 * @return the parent of this jar entry resource
	 */
	Object getParent();
	
	/**
	 * Returns <code>true</code> if this jar entry represents a file.
	 * Returns <code>false</code> if it is a directory.
	 * 
	 * @return whether this jar entry is a file
	 */
	boolean isFile();

}
