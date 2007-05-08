/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.part.StatusPart;
import org.eclipse.ui.part.ViewPart;

/**
 * This part is shown instead the views with errors.
 * 
 * @since 3.3
 */
public class ErrorViewPart extends ViewPart {

	private IStatus error;

	/**
	 * Creates instance of the class
	 */
	public ErrorViewPart() {
	}

	/**
	 * Creates instance of the class
	 */
	public ErrorViewPart(IStatus error) {
		this.error = error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		if (error != null) {
			new StatusPart(parent, error);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.ViewPart#setPartName(java.lang.String)
	 */
	public void setPartName(String newName) {
		super.setPartName(newName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
	}

}
