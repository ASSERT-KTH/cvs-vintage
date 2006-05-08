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

import org.eclipse.core.commands.common.NamedHandleObject;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;

/**
 * <p>
 * A group of menu elements that can be made visible or invisible as a group.
 * Action sets can also be associated with particular parts or perspectives. The
 * end user is also able to enable and disable action sets for a given
 * perspective.
 * </p>
 * <p>
 * Action sets are defined using the <code>org.eclipse.ui.menus</code>
 * extension point. They can be associated with parts using the
 * <code>org.eclipse.ui.actionSetPartAssociations</code> extension point. They
 * can be associated with perspectives using the
 * <code>org.eclipse.ui.perspectiveExtensions</code> extension point.
 * </p>
 * <p>
 * Clients may instantiate, but they must extend.
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
 */
public final class SActionSet extends NamedHandleObject {

	/**
	 * The property for a property change event indicating that the defined
	 * state for this action set has changed.
	 */
	public static final String PROPERTY_DEFINED = "DEFINED"; //$NON-NLS-1$

	/**
	 * The property for a property change event indicating that the description
	 * for this action set has changed.
	 */
	public static final String PROPERTY_DESCRIPTION = "DESCRIPTION"; //$NON-NLS-1$

	/**
	 * The property for a property change event indicating that the name for
	 * this action set has changed.
	 */
	public static final String PROPERTY_NAME = "NAME"; //$NON-NLS-1$

	/**
	 * The property for a property change event indicating that the references
	 * for this action set have changed.
	 */
	public static final String PROPERTY_REFERENCES = "REFERENCES"; //$NON-NLS-1$

	/**
	 * The property for a property change event indicating that the visible
	 * state for this action set has changed.
	 */
	public static final String PROPERTY_VISIBLE = "VISIBLE"; //$NON-NLS-1$

	/**
	 * References to menu elements in the workbench. These are the menu elements
	 * that are in this action set.
	 */
	private SReference[] references;

	/**
	 * Whether this action set should be visible in all perspectives by default.
	 */
	private boolean visible = false;

	/**
	 * Constructs a new instance of <code>SActionSet</code>
	 * 
	 * @param id
	 *            The identifier of the action set to create; must not be
	 *            <code>null</code>.
	 */
	public SActionSet(final String id) {
		super(id);
	}

	/**
	 * Adds a listener to this action set that will be notified when this action
	 * set's state changes.
	 * 
	 * @param listener
	 *            The listener to be added; must not be <code>null</code>.
	 */
	public final void addListener(final IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	/**
	 * <p>
	 * Defines this command by giving it a name, visibility and a collection of
	 * references. The description is optional. The defined property
	 * automatically becomes <code>true</code>.
	 * </p>
	 * 
	 * @param name
	 *            The name of the action set; must not be <code>null</code>.
	 *            This name should short (one or two words) and human-readable.
	 * @param description
	 *            The description for the action set; may be <code>null</code>
	 *            if there is no description. The description can be longer: one
	 *            or two sentences.
	 * @param visible
	 *            Whether the action set should be visible in all perspective by
	 *            default.
	 * @param references
	 *            References to the menu elements that are included in this
	 *            action set. This value must not be <code>null</code> and it
	 *            must not be empty.
	 */
	public final void define(final String name, final String description,
			final boolean visible, final SReference[] references) {
		if (name == null) {
			throw new NullPointerException("An action set needs a name"); //$NON-NLS-1$
		}

		if (references == null) {
			throw new NullPointerException(
					"The action set must reference at least one menu element"); //$NON-NLS-1$
		}

		if (references.length == 0) {
			throw new IllegalArgumentException(
					"The action set must reference at least one menu element"); //$NON-NLS-1$
		}

		setDefined(true);
		setName(name);
		setDescription(description);
		setVisible(visible);
		setReferences(references);
	}

	/**
	 * Notifies listeners to this action set that it has changed in some way.
	 * 
	 * @param event
	 *            The event to fire; may be <code>null</code>.
	 */
	protected final void firePropertyChangeEvent(final PropertyChangeEvent event) {
		if (event == null) {
			return;
		}

		final Object[] listeners = getListeners();
		for (int i = 0; i < listeners.length; i++) {
			final IPropertyChangeListener listener = (IPropertyChangeListener) listeners[i];
			listener.propertyChange(event);
		}
	}

	/**
	 * Returns the references for this action set. This performs a copy of the
	 * internal data structure.
	 * 
	 * @return The references for this action set; never <code>null</code>.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final SReference[] getReferences() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the references from an undefined action set"); //$NON-NLS-1$
		}

		final SReference[] result = new SReference[references.length];
		System.arraycopy(references, 0, result, 0, references.length);
		return result;
	}

	/**
	 * Whether the action set should be visible in every perspective by default.
	 * 
	 * @return <code>true</code> if the action set is visible in every
	 *         perspective; <code>false</code> otherwise.
	 * @throws NotDefinedException
	 *             If the handle is not currently defined.
	 */
	public final boolean isVisible() throws NotDefinedException {
		if (!isDefined()) {
			throw new NotDefinedException(
					"Cannot get the visibility from an undefined action set"); //$NON-NLS-1$
		}

		return visible;
	}

	/**
	 * Removes a listener from this action set.
	 * 
	 * @param listener
	 *            The listener to be removed; must not be <code>null</code>.
	 */
	public final void removeListener(final IPropertyChangeListener listener) {
		addListenerObject(listener);
	}

	/**
	 * Sets whether this menu element is defined. This will fire a property
	 * change event if anyone cares.
	 * 
	 * @param defined
	 *            Whether the menu element is defined.
	 */
	protected final void setDefined(final boolean defined) {
		if (this.defined != defined) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_DEFINED, 
						(this.defined ? Boolean.TRUE : Boolean.FALSE),
						(defined ? Boolean.TRUE : Boolean.FALSE));
			}
			this.defined = defined;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * Sets the description that should be displayed for this action set. This
	 * will fire a property change event if anyone cares.
	 * 
	 * @param description
	 *            The new description for this action set; may be
	 *            <code>null</code>.
	 */
	protected final void setDescription(final String description) {
		if (!Util.equals(this.description, description)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_DESCRIPTION,
						this.description, description);
			}
			this.description = description;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * Sets the name that should be displayed for this action set. This will
	 * fire a property change event if anyone cares.
	 * 
	 * @param name
	 *            The new name for this action set; may be <code>null</code>.
	 */
	protected final void setName(final String name) {
		if (!Util.equals(this.name, name)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_NAME, this.name,
						name);
			}
			this.name = name;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * Sets the references associated with this action set. This will fire a
	 * property change event if anyone cares.
	 * 
	 * @param references
	 *            The references for this action set; may be <code>null</code>.
	 */
	protected final void setReferences(final SReference[] references) {
		if (!Util.equals(this.references, references)) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_REFERENCES,
						this.references, references);
			}
			this.references = references;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * Sets whether this action set should be visible. This will fire a property
	 * change event if anyone cares.
	 * 
	 * @param visible
	 *            Whether the action set should be visible
	 */
	protected final void setVisible(final boolean visible) {
		if (this.visible != visible) {
			PropertyChangeEvent event = null;
			if (isListenerAttached()) {
				event = new PropertyChangeEvent(this, PROPERTY_VISIBLE, 
						(this.visible ? Boolean.TRUE : Boolean.FALSE),
						(visible ? Boolean.TRUE : Boolean.FALSE));
			}
			this.visible = visible;
			firePropertyChangeEvent(event);
		}
	}

	/**
	 * The string representation of this action set -- for debugging purposes
	 * only. This string should not be shown to an end user.
	 * 
	 * @return The string representation; never <code>null</code>.
	 */
	public final String toString() {
		if (string == null) {
			final StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append("SGroup("); //$NON-NLS-1$
			stringBuffer.append(id);
			stringBuffer.append(',');
			stringBuffer.append(name);
			stringBuffer.append(',');
			stringBuffer.append(description);
			stringBuffer.append(',');
			stringBuffer.append(visible);
			stringBuffer.append(',');
			stringBuffer.append(references);
			stringBuffer.append(',');
			stringBuffer.append(defined);
			stringBuffer.append(')');
			string = stringBuffer.toString();
		}
		return string;
	}

	/**
	 * Makes this action set become undefined. This has the side effect of
	 * changing the name, description and references to <code>null</code>.
	 * Notification is sent to all listeners.
	 */
	public final void undefine() {
		string = null;

		setReferences(references);
		setVisible(visible);
		setDescription(description);
		setName(name);
		setDefined(false);
	}
}
