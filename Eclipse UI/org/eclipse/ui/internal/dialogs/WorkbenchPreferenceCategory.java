/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;

/**
 * WorkbenchPreferenceCategory is the representation of a category
 * in the workbench.
 */
public class WorkbenchPreferenceCategory {
	
	private String id;
	private String name;
	private String parentCategoryId;
	private Collection childCategories = new ArrayList();
	private Collection pages = new ArrayList();
	private ImageDescriptor imageDescriptor;
	private Image image;

	/**
	 * Create a new instance of the receiver.
	 * @param uniqueID The unique id. Must be unique and non null.
	 * @param displayableName The human readable name
	 * @param parentId The id of the parent category.
	 * @param icon The ImageDescriptor for the icon for the
	 * receiver. May be <code>null</code>.
	 */
	public WorkbenchPreferenceCategory(String uniqueID, String displayableName, String parentId, ImageDescriptor icon) {
		id = uniqueID;
		name = displayableName;
		parentCategoryId = parentId;
		imageDescriptor = icon;
	}

	/**
	 * Return the id of the parent
	 * @return String
	 */
	public String getParent() {
		return parentCategoryId;
	}

	/**
	 * Add the category to the children.
	 * @param category
	 */
	public void addChild(WorkbenchPreferenceCategory category) {
		childCategories.add(category);
		
	}

	/**
	 * Return the id for the receiver.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Add the node to the list of pages in this category.
	 * @param node
	 */
	public void addNode(WorkbenchPreferenceNode node) {
		pages.add(node);
		
	}

	/**
	 * Return the image for the receiver. Return a default
	 * image if there isn't one.
	 * @return Image
	 */
	public Image getImage() {
		
		if(imageDescriptor == null)
			return JFaceResources.getImage(Dialog.DLG_IMG_INFO);
		
		if(image == null)
			image = imageDescriptor.createImage();
		return image;
	}

	/**
	 * Return the name of the receiver.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Dispose the resources for the receiver.
	 *
	 */
	public void disposeResources(){
		image.dispose();
		image = null;
	}

	/**
	 * Return the preference nodes in the receiver.
	 * @return IPreferenceNode[]
	 */
	public IPreferenceNode[] getPreferenceNodes() {
		IPreferenceNode[] nodes = new IPreferenceNode[pages.size()];
		pages.toArray(nodes);
		return nodes;
	}

}
