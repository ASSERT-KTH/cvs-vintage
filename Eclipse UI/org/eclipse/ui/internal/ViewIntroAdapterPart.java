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
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.intro.IIntroPart;
import org.eclipse.ui.intro.IIntroSite;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.ui.internal.intro.IntroMessages;

/**
 * Simple view that will wrap an <code>IIntroPart</code>.
 * 
 * <em>EXPERIMENTAL</em>
 * 
 * @since 3.0
 */
public final class ViewIntroAdapterPart extends ViewPart {

	private IIntroPart introPart;
	private IIntroSite introSite;

	/**
	 * Adds a listener that toggles standby state if the view pane is zoomed. 
	 */
	private void addPaneListener() {	
		((PartSite)getSite()).getPane().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(PartPane.PROP_ZOOMED)) {
					boolean standby = !((Boolean)event.getNewValue()).booleanValue();
					introPart.standbyStateChanged(standby);					
					WorkbenchWindow window = ((WorkbenchWindow)((PartSite)getSite()).getPane().getWorkbenchWindow());
					if (standby) {
						window.setCoolBarVisible(true);
						window.setPerspectiveBarVisible(true);
					}
					else {
						window.setCoolBarVisible(false);
						window.setPerspectiveBarVisible(false);						
					}
				}
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		addPaneListener();
		introPart.createPartControl(parent);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {		
		super.dispose();
		getSite().getWorkbenchWindow().getWorkbench().closeIntro(introPart);
		introPart.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return introPart.getAdapter(adapter);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitle()
	 */
	public String getTitle() {
		return introPart.getTitle();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitleImage()
	 */
	public Image getTitleImage() {
		return introPart.getTitleImage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#getTitleToolTip()
	 */
	public String getTitleToolTip() {
		return introPart.getTitleToolTip();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
	 */
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		Workbench workbench = (Workbench)site.getWorkbenchWindow().getWorkbench();
		try {
			introPart = workbench.createNewIntroPart();
			introPart.addPropertyListener(new IPropertyListener() {
				public void propertyChanged(Object source, int propId) {
					firePropertyChange(propId);					
				}});
			introSite = new ViewIntroAdapterSite(site, workbench.getIntroDescriptor());
			introPart.init(introSite);
		} catch (CoreException e) {
			WorkbenchPlugin.log(IntroMessages.getString("Intro.could_not_create_proxy"), new Status(IStatus.ERROR, WorkbenchPlugin.PI_WORKBENCH, IStatus.ERROR, IntroMessages.getString("Intro.could_not_create_proxy"), e)); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#setFocus()
	 */
	public void setFocus() {
		introPart.setFocus();
	}
}
