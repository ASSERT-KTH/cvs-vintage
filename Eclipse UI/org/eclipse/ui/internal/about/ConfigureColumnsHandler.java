/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.about;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.about.InstallationPage;

public class ConfigureColumnsHandler extends ProductInfoPageHandler {

	protected Object execute(InstallationPage page, ExecutionEvent event) {
		if (page instanceof TableListPage)
			((TableListPage)page).handleColumnsPressed();
		return null;
	}
}