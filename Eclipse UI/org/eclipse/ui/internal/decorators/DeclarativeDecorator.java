package org.eclipse.ui.internal.decorators;

/************************************************************************
Copyright (c) 2000, 2002 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM - Initial implementation
************************************************************************/

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;

/**
 * The DeclarativeDecorator is a decorator that is made entirely
 * from an XML specification.
 */

public class DeclarativeDecorator implements ILightweightLabelDecorator {

	private String iconLocation;
	private IConfigurationElement configElement;
	private ImageDescriptor descriptor;

	DeclarativeDecorator(
		IConfigurationElement definingElement,
		String iconPath) {
		this.iconLocation = iconPath;
		this.configElement = definingElement;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void addListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
	 */
	public void dispose() {
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
	 */
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	/**
	 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
	 */
	public void removeListener(ILabelProviderListener listener) {
	}

	/**
	 * @see org.eclipse.jface.viewers.ILightweightLabelDecorator#decorate(java.lang.Object, org.eclipse.jface.viewers.IDecoration)
	 */
	public void decorate(Object element, IDecoration decoration) {

		if (descriptor == null) {
			URL source =
				configElement
					.getDeclaringExtension()
					.getDeclaringPluginDescriptor()
					.getInstallURL();
			try {
				decoration.addOverlay(
					ImageDescriptor.createFromURL(
						new URL(source, iconLocation)));
			} catch (MalformedURLException exception) {
				return;
			}
		}

	}

}
