/******************************************************************************* * Copyright (c) 2005 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.ui.internal.menus;import org.eclipse.core.commands.common.NotDefinedException;import org.eclipse.jface.util.PropertyChangeEvent;import org.eclipse.jface.util.Util;/** * <p> * A logical grouping of menu items and widgets. This grouping can also take on * a physical appearance in the form of separators. * </p> * <p> * Clients may instantiate this class, but must not extend. * </p> * <p> * <strong>PROVISIONAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> * <p> * This class will eventually exist in <code>org.eclipse.jface.menus</code>. * </p> *  * @since 3.2 */public final class SGroup extends MenuContainer {	/**	 * The property for a property change event indicating that whether the	 * separators are visible for this group has changed.	 */	public static final String PROPERTY_SEPARATORS_VISIBLE = "SEPARATORS_VISIBLE"; //$NON-NLS-1$	/**	 * Whether separators should be drawn before and after this group, as	 * appropriate.	 */	public boolean separatorsVisible = true;	/**	 * Constructs a new instance of <code>SGroup</code>.	 * 	 * @param id	 *            The identifier of the group to create; must not be	 *            <code>null</code>	 */	SGroup(final String id) {		super(id);	}	/**	 * <p>	 * Defines this group by indicating whether the separators are visible. The	 * location is optional. The defined property automatically becomes	 * <code>true</code>.	 * </p>	 * 	 * @param separatorsVisible	 *            Whether separators should be drawn before and after this	 *            group, as appropriate.	 * @param location	 *            The location in which this group will appear; may be	 *            <code>null</code>.	 */	public final void define(final boolean separatorsVisible,			final SLocation location) {		define(separatorsVisible, location, null);	}	/**	 * <p>	 * Defines this group by indicating whether the separators are visible. The	 * location and dynamic menu are optional. The defined property	 * automatically becomes <code>true</code>.	 * </p>	 * 	 * @param separatorsVisible	 *            Whether separators should be drawn before and after this	 *            group, as appropriate.	 * @param location	 *            The location in which this group will appear; may be	 *            <code>null</code>.	 * @param dynamic	 *            The class providing dynamic menu elements to this group; may	 *            be <code>null</code>.	 */	public final void define(final boolean separatorsVisible,			final SLocation location, final IDynamicMenu dynamic) {		final SLocation[] locations;		if (location == null) {			locations = null;		} else {			locations = new SLocation[] { location };		}		define(separatorsVisible, locations, dynamic);	}	/**	 * <p>	 * Defines this group by indicating whether the separators are visible. The	 * locations and dynamic menu are optional. The defined property	 * automatically becomes <code>true</code>.	 * </p>	 * 	 * @param separatorsVisible	 *            Whether separators should be drawn before and after this	 *            group, as appropriate.	 * @param locations	 *            The locations in which this group will appear; may be	 *            <code>null</code> or empty.	 * @param dynamic	 *            The class providing dynamic menu elements to this group; may	 *            be <code>null</code>.	 */	public final void define(final boolean separatorsVisible,			SLocation[] locations, final IDynamicMenu dynamic) {		if ((locations != null) && (locations.length == 0)) {			locations = null;		}		setDefined(true);		setDynamic(dynamic);		setLocations(locations);		setSeparatorsVisible(separatorsVisible);	}	/**	 * Sets whether separators should be displayed around this group. This will	 * fire a property change event if anyone cares.	 * 	 * @param separatorsVisible	 *            Whether the separators should be visible.	 */	protected final void setSeparatorsVisible(final boolean separatorsVisible) {		if (this.defined != defined) {			PropertyChangeEvent event = null;			if (isListenerAttached()) {				event = new PropertyChangeEvent(this,						PROPERTY_SEPARATORS_VISIBLE, 						(this.separatorsVisible ? Boolean.TRUE : Boolean.FALSE),						(separatorsVisible ? Boolean.TRUE : Boolean.FALSE));			}			this.separatorsVisible = separatorsVisible;			firePropertyChangeEvent(event);		}	}	/**	 * <p>	 * Defines this group by indicating with only a location is optional. The	 * separators are assumed to be visible. The defined property automatically	 * becomes <code>true</code>.	 * </p>	 * 	 * @param location	 *            The location in which this group will appear; may be	 *            <code>null</code>.	 */	public final void define(final SLocation location) {		define(true, location, null);	}	/**	 * Whether separators should be drawn around the group.	 * 	 * @return <code>true</code> if the separators should be drawn;	 *         <code>false</code> otherwise.	 * @throws NotDefinedException	 *             If the handle is not currently defined.	 */	public final boolean isSeparatorsVisible() throws NotDefinedException {		if (!isDefined()) {			throw new NotDefinedException(					"Cannot get whether the separators are visible from an undefined group"); //$NON-NLS-1$		}		return separatorsVisible;	}	/**	 * The string representation of this group -- for debugging purposes only.	 * This string should not be shown to an end user.	 * 	 * @return The string representation; never <code>null</code>.	 */	public final String toString() {		if (string == null) {			final StringBuffer stringBuffer = new StringBuffer();			stringBuffer.append("SGroup("); //$NON-NLS-1$			stringBuffer.append(id);			stringBuffer.append(',');			stringBuffer.append(separatorsVisible);			stringBuffer.append(',');			stringBuffer.append(Util.toString(locations));			stringBuffer.append(',');			try {				stringBuffer.append(dynamic);			} catch (final Exception e) {				// A bogus toString() in third-party code. Ignore.				stringBuffer.append(e.getClass().getName());			}			stringBuffer.append(',');			stringBuffer.append(defined);			stringBuffer.append(')');			string = stringBuffer.toString();		}		return string;	}	/**	 * Makes this group become undefined. This has the side effect of changing	 * the locations and dynamic class to <code>null</code>. Notification is	 * sent to all listeners.	 */	public final void undefine() {		string = null;		setSeparatorsVisible(false);		setDynamic(null);		setLocations(null);		setDefined(false);	}}
