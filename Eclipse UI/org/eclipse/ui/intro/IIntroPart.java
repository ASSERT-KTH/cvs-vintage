/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.intro;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;

/**
 * <p>
 * <em>EXPERIMENTAL</em>
 * </p>
 *  
 * @since 3.0
 */
public interface IIntroPart extends IWorkbenchPart {

	/**
	 * Returns the intro site the part was provided during initialization.
	 * 
	 * @return the <code>IIntroSite</code>.
	 */
	IIntroSite getIntroSite();

	/**
	 * Primes the part with the intro area object immediately after the creation.
	 * 
	 * @param area the <code>IIntroArea</code>.
	 * @throws PartInitException thrown if the <code>IntroPart</code> was not initialized 
	 * successfully.
	 */
	void init(IIntroSite area) throws PartInitException;

	/**
	 * Notifies the part that the intro area has been made fullscreen or put on 
	 * standby. Intro part should render itself differently in the full and standby 
	 * mode.  
	 * <p>
	 * Clients should not call this method (the workbench calls this method at
	 * appropriate times). To have the workbench change standby mode, use
	 * <code>IWorkbench.setIntroStandby(IIntroPart,boolean)</code> instead.
	 */
	void standbyStateChanged(boolean standby);
}
