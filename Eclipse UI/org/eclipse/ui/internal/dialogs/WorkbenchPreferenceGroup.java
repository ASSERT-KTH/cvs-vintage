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
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.misc.StringMatcher;

/**
 * WorkbenchPreferenceGroup is the representation of a category
 * in the workbench.
 */
public class WorkbenchPreferenceGroup {

	private String id;

	private String name;

	private Collection pages = new ArrayList();

	private Collection pageIds;

	private ImageDescriptor imageDescriptor;
	
	private ImageDescriptor largeImageDescriptor;

	private Image image;
	
	private Image largeImage;

	private boolean highlight = false;

	private Object lastSelection = null;

	private boolean isDefault = false;

	/**
	 * Create a new instance of the receiver.
	 * @param uniqueID The unique id. Must be unique and non null.
	 * @param displayableName The human readable name
	 * @param ids
	 * @param icon The ImageDescriptor for the icon for the
	 * receiver. May be <code>null</code>.
	 * @param largeIcon The ImageDescriptor for the largeIcon for the
	 * receiver. May be <code>null</code>.
	 * @param defaultValue <code>true</code> if this is the default group
	 */
	public WorkbenchPreferenceGroup(String uniqueID, String displayableName, Collection ids,
			ImageDescriptor icon, ImageDescriptor largeIcon,boolean defaultValue) {
		id = uniqueID;
		name = displayableName;
		imageDescriptor = icon;
		largeImageDescriptor = largeIcon;
		pageIds = ids;
		isDefault = defaultValue;
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

		if (imageDescriptor == null)
			return null;

		if (image == null)
			image = imageDescriptor.createImage();
		return image;
	}
	
	/**
	 * Return the image for the receiver. Return a default
	 * image if there isn't one.
	 * @return Image
	 */
	public Image getLargeImage() {

		if (largeImageDescriptor == null)
			return null;

		if (largeImage == null)
			largeImage = largeImageDescriptor.createImage();
		return largeImage;
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
	public void disposeResources() {
		if(image != null)
			image.dispose();
		if(largeImage != null)
			largeImage.dispose();
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

	/**
	 * Return the pageIds for the receiver.
	 * @return Collection
	 */
	public Collection getPageIds() {
		return pageIds;
	}

	/**
	 * Add all of the children that match text to the
	 * highlight list.
	 * @param text
	 */
	public void highlightHits(String text) {
		Iterator pagesIterator = pages.iterator();
		StringMatcher matcher = new StringMatcher('*' + text + '*', true, false);

		while (pagesIterator.hasNext()) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) pagesIterator.next();
			if (text.length() == 0)
				clearSearchResults(node);
			else
				matchNode(matcher, node);
		}

	}

	void clearSearchResults(WorkbenchPreferenceNode node) {
		node.setHighlighted(false);
		IPreferenceNode[] children = node.getSubNodes();
		for (int i = 0; i < children.length; i++)
			clearSearchResults((WorkbenchPreferenceNode) children[i]);
	}

	/**
	 * Match the node to the pattern and highlight it if there is
	 * a match.
	 * @param matcher
	 * @param node
	 */
	private void matchNode(StringMatcher matcher, WorkbenchPreferenceNode node) {
		node.setHighlighted(matcher.match(node.getLabelText()));
		IPreferenceNode[] children = node.getSubNodes();
		for (int i = 0; i < children.length; i++)
			matchNode(matcher, (WorkbenchPreferenceNode) children[i]);
	}

	/**
	 * Return whether or not the receiver is highlighted.
	 * @return Returns the highlight.
	 */
	public boolean isHighlighted() {
		return highlight;
	}

	/**
	 * Get the last selected object in this group.
	 * @return Object
	 */
	public Object getLastSelection() {
		return lastSelection;
	}

	/**
	 * Set the last selected object in this group.
	 * @param lastSelection WorkbenchPreferenceGroup
	 * or WorkbenchPreferenceNode.
	 */
	public void setLastSelection(Object lastSelection) {
		this.lastSelection = lastSelection;
	}

	/**
	 * Find the parent of this element starting at this node.
	 * @param node
	 * @param element
	 * @return Object or <code>null</code>.
	 */
	private Object findParent(IPreferenceNode node, Object element) {
		IPreferenceNode[] subs = node.getSubNodes();
		for (int i = 0; i < subs.length; i++) {
			IPreferenceNode subNode = subs[i];
			if (subNode.equals(element))
				return node;
			Object parent = findParent(subNode, element);
			if (parent != null)
				return parent;
		}
		return null;
	}

	/**
	 * Add any page ids that match the filteredIds
	 * to the list of highlights.
	 * @param filteredIds
	 */
	public void highlightIds(String[] filteredIds) {
		for (int i = 0; i < filteredIds.length; i++) {
			checkId(filteredIds[i]);
		}

	}

	/**
	 * Check the passed id to see if it matches
	 * any of the receivers pages.
	 * @param id
	 */
	private void checkId(String id) {
		Iterator pagesIterator = pages.iterator();
		while (pagesIterator.hasNext()) {
			WorkbenchPreferenceNode next = (WorkbenchPreferenceNode) pagesIterator.next();
			checkHighlightNode(id, next);
		}

	}

	/**
	 * Check if the node matches id and needs to be highlighted.
	 * @param id
	 * @param node
	 * @return <code>true</code> if a match is found
	 */
	private boolean checkHighlightNode(String id, IPreferenceNode node) {
		if (node.getId().equals(id)) {
			((WorkbenchPreferenceNode) node).setHighlighted(true);
			return true;
		}
		IPreferenceNode[] subNodes = node.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			if (checkHighlightNode(id, subNodes[i]))
				return true;
		}
		return false;
	}
	/**
	 * Return whether or not this is the default group.
	 * @return boolean
	 */
	public boolean isDefault() {
		return this.isDefault;
	}
}
