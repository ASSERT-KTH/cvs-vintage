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

package org.eclipse.ui.internal.menus;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A menu container is a menu element that can contain other elements. This
 * means that it can optionally have a class the will control the appearance of
 * elements on a dynamic basis.
 * </p>
 * <p>
 * Clients must not implement or extend.
 * </p>
 * <p>
 * <strong>PROVISIONAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/UI team.
 * </p>
 * <p>
 * This class will eventually exist in <code>org.eclipse.jface.menus</code>.
 * </p>
 * 
 * @since 3.2
 * @see org.eclipse.ui.internal.menus.SMenu
 * @see org.eclipse.ui.internal.menus.SGroup
 */
public abstract class MenuContainer extends MenuElement {

	/**
	 * The property for a property change event indicating that the dynamic
	 * class for this menu container has changed.
	 */
	public static final String PROPERTY_DYNAMIC = "DYNAMIC"; //$NON-NLS-1$

	/**
	 * The class providing dynamic menu elements to this menu container. If this
	 * value is <code>null</code>, then there are no dynamic elements.
	 */
	protected IDynamicMenu dynamic;

	/**
	 * Constructs a new instance of <code>MenuCollection</code>.
	 * 
	 * @param id
	 *            The identifier of the container to create; must not be
	 *            <code>null</code>.
	 */
	protected MenuContainer(final String id) {
		super(id);
	}

	/**
	 * Returns the class generating dynamic menu elements for this menu
	 * container.
	 * 
	 * @return The class generating dynamic menu elements for this menu
	 *         container; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final IDynamicMenu getDynamic() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the dynamic class from an undefined menu container"); //$NON-NLS-1$
		}

		return dynamic;
	}

	protected final void setDynamic(final IDynamicMenu dynamic) {
		if (!Util.equals(this.dynamic, dynamic)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_DYNAMIC,
						this.dynamic, dynamic);
			}
			this.dynamic = dynamic;
			firePropertyChangeEvent(event);
		}
	}
}
