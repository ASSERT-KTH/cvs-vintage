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
package org.eclipse.ui.internal.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.dialogs.WorkbenchPreferenceNode;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 *  Instances access the registry that is provided at creation time in order
 *  to determine the contributed preference pages
 */
public class PreferencePageRegistryReader extends CategorizedPageRegistryReader {

	private static final String ATT_CLASS = "class"; //$NON-NLS-1$

	private static final String ATT_NAME = "name"; //$NON-NLS-1$

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String TAG_PAGE = "page"; //$NON-NLS-1$

	private static final String ATT_ICON = "icon"; //$NON-NLS-1$

	private final static String TRUE_STRING = "true";//$NON-NLS-1$
	
	private static final String TAG_KEYWORD_REFERENCE = "keywordReference"; //$NON-NLS-1$

	private List nodes;

	private IWorkbench workbench;
	

	class PreferencesCategoryNode extends CategoryNode {

		WorkbenchPreferenceNode node;

		/**
		 * Create a new instance of the receiver.
		 * @param reader
		 * @param nodeToCategorize
		 */
		public PreferencesCategoryNode(CategorizedPageRegistryReader reader,
				WorkbenchPreferenceNode nodeToCategorize) {
			super(reader);
			this.node = nodeToCategorize;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText()
		 */
		String getLabelText() {
			return node.getLabelText();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getLabelText(java.lang.Object)
		 */
		String getLabelText(Object element) {
			return ((WorkbenchPreferenceNode) element).getLabelText();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader.CategoryNode#getNode()
		 */
		Object getNode() {
			return node;
		}
	}

	/**
	 * Create a new instance configured with the workbench
	 */
	public PreferencePageRegistryReader(IWorkbench newWorkbench) {
		workbench = newWorkbench;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.String)
	 */
	Object findNode(String id) {
		for (int i = 0; i < nodes.size(); i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) nodes.get(i);
			if (node.getId().equals(id))
				return node;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#findNode(java.lang.Object, java.lang.String)
	 */
	Object findNode(Object parent, String currentToken) {
		IPreferenceNode[] subNodes = ((WorkbenchPreferenceNode) parent).getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) subNodes[i];
			if (node.getId().equals(currentToken))
				return node;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#add(java.lang.Object, java.lang.Object)
	 */
	void add(Object parent, Object node) {
		((IPreferenceNode) parent).add((IPreferenceNode) node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#createCategoryNode(org.eclipse.ui.internal.registry.CategorizedPageRegistryReader, java.lang.Object)
	 */
	CategoryNode createCategoryNode(CategorizedPageRegistryReader reader, Object object) {
		return new PreferencesCategoryNode(reader, (WorkbenchPreferenceNode) object);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getCategory(java.lang.Object)
	 */
	String getCategory(Object node) {
		return ((WorkbenchPreferenceNode) node).getCategory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getNodes()
	 */
	Collection getNodes() {
		return nodes;
	}

	/**
	 * Load the preference page contirbutions from the registry and
	 * organize preference node contributions by category into hierarchies
	 * If there is no page for a given node in the hierarchy then a blank
	 * page will be created.
	 * If no category has been specified or category information
	 * is incorrect, page will appear at the root level. workbench
	 * log entry will be created for incorrect category information.
	 */
	public void loadFromRegistry(IExtensionRegistry registry) {
		nodes = new ArrayList();

		readRegistry(registry, PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_PREFERENCES);

		processNodes();

	}

	/**
	 * Read preference page element.
	 */
	protected boolean readElement(IConfigurationElement element) {
			if (element.getName().equals(TAG_PAGE) == false)
			return false;
		WorkbenchPreferenceNode node = createNode(workbench, element);
		if (node != null)
			nodes.add(node);
		readElementChildren(element);
		return true;
	}

	/**
	 * Create a workbench preference node.
	 * @param workbench
	 * @param element
	 * @return WorkbenchPreferenceNode
	 */
	public static WorkbenchPreferenceNode createNode(IWorkbench workbench,
			IConfigurationElement element) {
		String name = element.getAttribute(ATT_NAME);
		String id = element.getAttribute(ATT_ID);
		String category = element.getAttribute(ATT_CATEGORY);
		String imageName = element.getAttribute(ATT_ICON);
		String className = element.getAttribute(ATT_CLASS);

		if (name == null) {
			logMissingAttribute(element, ATT_NAME);
		}
		if (id == null) {
			logMissingAttribute(element, ATT_ID);
		}
		if (className == null) {
			logMissingAttribute(element, ATT_CLASS);
		}
		if (name == null || id == null || className == null) {
			return null;
		}
		
		Collection keywordReferences = readKeywordReferences(element); 
		ImageDescriptor image = null;
		if (imageName != null) {
			String contributingPluginId = element.getDeclaringExtension().getNamespace();
			image = AbstractUIPlugin.imageDescriptorFromPlugin(contributingPluginId, imageName);
		}
		WorkbenchPreferenceNode node = new WorkbenchPreferenceNode(id, name, category, image,
				element, keywordReferences, workbench);
		return node;
	}
	
	/**
	 * Read the pages for the receiver from element.
	 * @param element
	 * @return Collection the ids of the children
	 */
	private static Collection readKeywordReferences(IConfigurationElement element) {
		IConfigurationElement[] references = element.getChildren(TAG_KEYWORD_REFERENCE);
		HashSet list = new HashSet();
		for (int i = 0; i < references.length; i++) {
			IConfigurationElement page = references[i];
			String id = page.getAttribute(ATT_ID);
			if (id != null)
				list.add(id);
		}

		return list;
	}

	/**
	 * Return the top level IPreferenceNodes.
	 * @return  Collection of IPreferenceNode.
	 */
	public Collection getTopLevelNodes() {
		return topLevelNodes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.registry.CategorizedPageRegistryReader#getFavoriteNodeId()
	 */
	String getFavoriteNodeId() {
		return ((Workbench) workbench).getMainPreferencePageId();
	}

	
}