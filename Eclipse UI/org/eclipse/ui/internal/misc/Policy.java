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
package org.eclipse.ui.internal.misc;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.PlatformUI;

public class Policy {
	public static boolean DEFAULT = false;
	
	// @issue this is an IDE specific debug option
	public static boolean DEBUG_OPEN_ERROR_DIALOG = DEFAULT;
	public static boolean DEBUG_SWT_GRAPHICS = DEFAULT;

	public static boolean DEBUG_PART_CREATE = DEFAULT;
	public static boolean DEBUG_PART_ACTIVATE = DEFAULT;
	public static boolean DEBUG_PART_LISTENERS = DEFAULT;
	public static boolean DEBUG_PERSPECTIVE = DEFAULT;
	public static boolean DEBUG_RESTORE_WORKBENCH = DEFAULT;
	public static boolean DEBUG_START_WORKBENCH = DEFAULT;
	public static boolean DEBUG_DRAG_DROP = DEFAULT;
	public static boolean DEBUG_KEY_BINDINGS = DEFAULT;

	static {
		if (getDebugOption("/debug")) { //$NON-NLS-1$
			DEBUG_OPEN_ERROR_DIALOG = getDebugOption("/debug/internalerror/openDialog"); //$NON-NLS-1$
			DEBUG_SWT_GRAPHICS = getDebugOption("/trace/graphics"); //$NON-NLS-1$
			DEBUG_PART_CREATE = getDebugOption("/trace/part.create"); //$NON-NLS-1$
			DEBUG_PERSPECTIVE = getDebugOption("/trace/perspective"); //$NON-NLS-1$
			DEBUG_RESTORE_WORKBENCH = getDebugOption("/trace/workbench.restore"); //$NON-NLS-1$
			DEBUG_START_WORKBENCH = getDebugOption("/trace/workbench.start"); //$NON-NLS-1$
			DEBUG_PART_ACTIVATE = getDebugOption("/trace/part.activate"); //$NON-NLS-1$
			DEBUG_PART_LISTENERS = getDebugOption("/trace/part.listeners"); //$NON-NLS-1$
			DEBUG_DRAG_DROP = getDebugOption("/trace/dragDrop"); //$NON-NLS-1$
			DEBUG_KEY_BINDINGS = getDebugOption("/trace/keyBindings"); //$NON-NLS-1$
		}
	}
	
	private static boolean getDebugOption(String option) {
		return "true".equalsIgnoreCase(Platform.getDebugOption(PlatformUI.PLUGIN_ID + option)); //$NON-NLS-1$
	}
}
