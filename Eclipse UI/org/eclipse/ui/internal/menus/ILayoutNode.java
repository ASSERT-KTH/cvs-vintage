/******************************************************************************* * Copyright (c) 2006 IBM Corporation and others. * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.List;

import org.eclipse.core.commands.common.IIdentifiable;

/**
 * <p>
 * A node within the menu layout. This holds information about the location, the
 * corresponding menu element as well as some children. This layout is
 * immutable.
 * </p>
 * <p>
 * Clients must not extend or implement.
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
public interface ILayoutNode extends IIdentifiable {

	/**
	 * <p>
	 * Returns the children of this node, if any. This collection is sorted into
	 * the ordering, as specified in the ordering constraints. The sort order is
	 * broken into three blocks: beginning, middle and end. Within those blocks,
	 * items appear in alphabetical order by id. Without any ordering
	 * constraints, a menu element will appear in the middle block.
	 * {@link SOrder#POSITION_START} and {@link SOrder#POSITION_END} can be used
	 * to put a menu element in the beginning group or the end group,
	 * respectively.
	 * </p>
	 * <p>
	 * If an {@link SOrder#POSITION_BEFORE} or {@link SOrder#POSITION_AFTER}
	 * constraint is given, then the item appears in a position relative to
	 * other item. Note that the {@link SOrder#POSITION_BEFORE} and
	 * {@link SOrder#POSITION_AFTER} constraints have higher priority than the
	 * {@link SOrder#POSITION_START} and {@link SOrder#POSITION_END}
	 * constraints. As such, an item the specifies itself as being at the start
	 * can be pulled to the end of a menu collection by a
	 * {@link SOrder#POSITION_AFTER} constraint.
	 * </p>
	 * 
	 * @return The children ({@link ILayoutNode}); never <code>null</code>,
	 *         but may be empty.
	 */
	List getChildrenSorted();

	/**
	 * Returns the specific location for this node. Normally, a menu element can
	 * be associated with one or more locations. This location is one of the
	 * menu element's location, and represented this particular position within
	 * the menu layout.
	 * 
	 * @return The location represented by this layout node; may be
	 *         <code>null</code> if this is a top-level layout node or if the
	 *         node is implicitly created.
	 */
	SLocation getLocation();

	/**
	 * Returns the menu element for this node.
	 * 
	 * @return The menu element; may be <code>null</code>.
	 */
	MenuElement getMenuElement();
	
	/**
	 * Returns whether this node has no children.
	 * 
	 * @return Whether this node is empty.
	 */
	boolean isEmpty();
}

