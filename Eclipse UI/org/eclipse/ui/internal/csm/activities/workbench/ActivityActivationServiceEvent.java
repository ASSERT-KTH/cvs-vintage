/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.csm.activities.workbench;


final class ActivityActivationServiceEvent implements IActivityActivationServiceEvent {

	private IActivityActivationService activityActivationService;

	ActivityActivationServiceEvent(IActivityActivationService activityActivationService) {
		if (activityActivationService == null)
			throw new NullPointerException();
		
		this.activityActivationService = activityActivationService;
	}

	public IActivityActivationService getActivityActivationService() {
		return activityActivationService;
	}
}
