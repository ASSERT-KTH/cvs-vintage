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
package org.eclipse.ui.internal.keywords;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamicHelpers.IExtensionAdditionHandler;
import org.eclipse.core.runtime.dynamicHelpers.IExtensionRemovalHandler;
import org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchConstants;

/**
 * Contains extensions defined on the <code>keywords</code> extension point.
 * 
 * @since 3.1
 */
public final class KeywordRegistry implements IExtensionAdditionHandler,
		IExtensionRemovalHandler {

	private static final String ATT_ID = "id"; //$NON-NLS-1$

	private static final String ATT_LABEL = "label"; //$NON-NLS-1$

	private static KeywordRegistry instance;

	private static final String TAG_KEYWORD = "keyword"; //$NON-NLS-1$

	/**
	 * Return the singleton instance of the <code>KeywordRegistry</code>.
	 * 
	 * @return the singleton registry
	 */
	public static KeywordRegistry getInstance() {
		if (instance == null) {
			instance = new KeywordRegistry();
		}

		return instance;
	}

	/**
	 * Map of id->labels.
	 */
	private Map internalKeywordMap = new HashMap();
	
	/**
	 * Private constructor.
	 */
	private KeywordRegistry() {
		PlatformUI.getWorkbench().getExtensionTracker()
				.registerAdditionHandler(this);
		PlatformUI.getWorkbench().getExtensionTracker().registerRemovalHandler(
				this);
		IExtension[] extensions = getExtensionPointFilter().getExtensions();
		for (int i = 0; i < extensions.length; i++) {
			addInstance(PlatformUI.getWorkbench().getExtensionTracker(),
					extensions[i]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionAdditionHandler#addInstance(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker,
	 *      org.eclipse.core.runtime.IExtension)
	 */
	public void addInstance(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i].getName().equals(TAG_KEYWORD)) {
				String name = elements[i].getAttribute(ATT_LABEL);
				String id = elements[i].getAttribute(ATT_ID);
				internalKeywordMap.put(id, name);
				PlatformUI.getWorkbench().getExtensionTracker().registerObject(
						extension, id, IExtensionTracker.REF_WEAK);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionAdditionHandler#getExtensionPointFilter()
	 */
	public IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				PlatformUI.PLUGIN_ID, IWorkbenchConstants.PL_KEYWORDS);
	}
	
	/**
	 * Return the label associated with the given keyword.
	 * 
	 * @param id the keyword id
	 * @return the label or <code>null</code>
	 */
	public String getKeywordLabel(String id) {
		return (String) internalKeywordMap.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionRemovalHandler#removeInstance(org.eclipse.core.runtime.IExtension,
	 *      java.lang.Object[])
	 */
	public void removeInstance(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof String) {
				internalKeywordMap.remove(objects[i]);
			}
		}
	}
}
