/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import org.eclipse.core.expressions.Expression;

/**
 * WidgetData can be provided to the programmatic MenuDataCacheEntry. It must
 * implement createWidget() to return a new abstract workbench widget.
 * 
 * @since 3.3
 */
public abstract class WidgetData extends ServiceData {

	/**
	 * @param id
	 * @param visibleWhen
	 */
	public WidgetData(String id, Expression visibleWhen) {
		super(id, visibleWhen);
	}

	/**
	 * Create the widget when populating menus.
	 * 
	 * @return the newly created widget.
	 */
	public abstract AbstractWorkbenchWidget createWidget();
}
