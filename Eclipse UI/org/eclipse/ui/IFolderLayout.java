package org.eclipse.ui;

/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. � This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
�
Contributors:
**********************************************************************/

 
/**
 * An <code>IFolderLayout</code> is used to define the initial pages within a folder.
 * The folder itself is component within an <code>IPageLayout</code>.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IPageLayout#createFolder
 */
public interface IFolderLayout extends IPlaceholderFolderLayout {
/**
 * Adds a view with the given id to this folder.
 * The id must name a view contributed to the workbench's view extension point 
 * (named <code>"org.eclipse.ui.views"</code>).
 *
 * @param viewId the view id
 */
public void addView(String viewId);
}
