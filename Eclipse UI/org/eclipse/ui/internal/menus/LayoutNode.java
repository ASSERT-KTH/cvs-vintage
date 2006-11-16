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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.common.IIdentifiable;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * A node within a menu layout. A node has contains a menu element and some
 * child nodes.
 * </p>
 * <p>
 * This class is only intended to be used from within the
 * <code>org.eclipse.jface</code> plug-in.
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
final class LayoutNode implements ILayoutNode, IMenuCollection,
		IPropertyChangeListener {

	/**
	 * The number of items that need to be contained within a block before we
	 * switch to binary insertion sort.
	 */
	private static final int BINARY_CUT_OFF = 5;

	/**
	 * The index of the block containing order nodes that need to appear at the
	 * end.
	 */
	private static final int END = 2;

	/**
	 * The index of the block containing order nodes that specify no ordering
	 * constraints.
	 */
	private static final int MIDDLE = 1;

	/**
	 * The number of blocks, used for initializing an array.
	 */
	private static final int NUMBER_OF_BLOCKS = 3;

	/**
	 * The index of the block containing order nodes that need to appear at the
	 * start.
	 */
	private static final int START = 0;

	/**
	 * Returns the location to use from the menu element, if the menu element
	 * was provided to the {@link #add(MenuElement)} method.
	 * 
	 * @param element
	 *            The element from which the location should be retrieved; must
	 *            not be <code>null</code>
	 * @return The preferred location for this menu element; may be
	 *         <code>null</code> if none.
	 * @throws NotDefinedException
	 *             If the provided menu element is <code>null</code>.
	 */
	private static final SLocation getLocation(final MenuElement element)
			throws NotDefinedException {
		final SLocation[] locations = element.getLocations();
		if (locations.length > 0) {
			return locations[0];
		}

		return null;
	}

	/**
	 * Inserts a node into a list, sorted based on the identifier of the
	 * element. If there is no element, then the item appears first.
	 * 
	 * @param list
	 *            The list into which the element should be inserted; must not
	 *            be <code>null</code>.
	 * @param node
	 *            The node to inserted; must not be <code>null</code>.
	 */
	static final void sortedInsert(final List list, final IIdentifiable node) {
		if (list.size() < BINARY_CUT_OFF) {
			// Linear insert.
			sortedInsertLinear(list, node);
		} else {
			// Binary insert.
			sortedInsertBinary(list, node);
		}
	}

	/**
	 * Inserts a node into a list using a binary search, sorted based on the
	 * identifier of the element. If there is no element, then the item appears
	 * first.
	 * 
	 * @param list
	 *            The list into which the element should be inserted; must not
	 *            be <code>null</code>.
	 * @param node
	 *            The node to inserted; must not be <code>null</code>.
	 */
	private static final void sortedInsertBinary(final List list,
			final IIdentifiable node) {
		final String nodeId = node.getId();
		int topIndex = list.size() - 1;
		int bottomIndex = 0;
		int middleIndex = 0;
		while (true) {
			middleIndex = (topIndex + bottomIndex) / 2;
			final IIdentifiable current = (IIdentifiable) list.get(middleIndex);
			final String currentId = current.getId();
			final int comparison = Util.compare(nodeId, currentId);
			if (comparison < 0) {
				if (bottomIndex == middleIndex) {
					list.add(bottomIndex, node);
					break;
				}
				topIndex = middleIndex;

			} else if (comparison > 0) {
				if (bottomIndex == middleIndex) {
					list.add(topIndex, node);
					break;
				}
				bottomIndex = middleIndex;

			} else {
				list.add(middleIndex, node);
				break;

			}
		}
	}

	/**
	 * Inserts a node into an array using a linear search, sorted based on the
	 * identifier of the element. If there is no element, then the item appears
	 * first.
	 * 
	 * @param list
	 *            The list into which the element should be inserted; must not
	 *            be <code>null</code>.
	 * @param node
	 *            The node to inserted; must not be <code>null</code>.
	 */
	private static final void sortedInsertLinear(final List list,
			final IIdentifiable node) {
		final String nodeId = node.getId();
		final int length = list.size();
		for (int i = 0; i < length; i++) {
			final IIdentifiable current = (IIdentifiable) list.get(i);
			final String currentId = current.getId();
			final int comparison = Util.compare(nodeId, currentId);
			if (comparison < 0) {
				list.add(i, node);
				return;
			}
		}

		list.add(node);
	}

	/**
	 * The children of this node indexed by their identifiers. This map is
	 * lazily initialized, and will be <code>null</code> until someone adds a
	 * child node.
	 */
	private Map childrenById;

	/**
	 * The element this node represents. This value may be <code>null</code>
	 * if this is a top-level node in some menu layout structure, or if the
	 * element is implied.
	 */
	private MenuElement element;

	/**
	 * The specific location this node is referring to. This value may be
	 * <code>null</code> if this is a top-level node in some menu layout
	 * structure, or if the element is implied.
	 */
	private SLocation location;

	/**
	 * The identifiers of the children, but in the order indicating by their
	 * ordering constraints. This list is lazily generated.
	 */
	private List orderedChildren;

	/**
	 * Constructs a new <code>SMenuLayout</code>.
	 */
	LayoutNode() {
		// Do nothing
	}

	public final void add(final MenuElement element) throws NotDefinedException {
		createChildNode(element, null);
		orderedChildren = null;
	}

	public final void clear() {
		childrenById = null;
		orderedChildren = null;
	}

	/**
	 * Retrieves the child node based on the given menu element.
	 * 
	 * @param element
	 *            The element on which the child node should be based; must not
	 *            be <code>null</code>.
	 * @param location
	 *            The location in which this particular node exists. If
	 *            <code>null</code>, then the first location in the element
	 *            is used as the location.
	 * @throws NotDefinedException
	 *             If the given menu element is not defined.
	 */
	final void createChildNode(final MenuElement element,
			final SLocation location) throws NotDefinedException {
		if (element == null) {
			throw new NullPointerException(
					"A child node cannot be created from a null element"); //$NON-NLS-1$
		}

		final String id = element.getId();
		if (Policy.EXPERIMENTAL_MENU 
				&& id.equals(LeafLocationElement.BREAKPOINT_PATH)) {
			System.err.println("createChildNode: " + location); //$NON-NLS-1$
		}
		LayoutNode childNode = null;
		if (childrenById == null) {
			childrenById = new HashMap(4);
		} else {
			childNode = (LayoutNode) childrenById.get(id);
		}
		if (childNode == null) {
			childNode = new LayoutNode();
			childrenById.put(id, childNode);
			orderedChildren = null;
		}
		if (element!=null) {
			childNode.setElement(element);
		}
		childNode.setLocation((location == null) ? getLocation(element)
				: location);
	}

	/**
	 * Retrieves the child node with the given identifier. If no such node
	 * exists yet, it is created.
	 * 
	 * @param token
	 *            The token representing the child node to retrieve; must not be
	 *            <code>null</code>.
	 * @return The child node; never <code>null</code>.
	 */
	final LayoutNode getChildNode(final LocationElementToken token) {
		if (token == null) {
			throw new NullPointerException(
					"A child node cannot be created from a null token"); //$NON-NLS-1$
		}

		final String id = token.getId();
		LayoutNode childNode = null;
		if (childrenById == null) {
			childrenById = new HashMap(4);
		} else {
			childNode = (LayoutNode) childrenById.get(id);
		}
		if (childNode == null) {
			childNode = new LayoutNode();
			childrenById.put(id, childNode);
			childNode.setLocation(token.getLocation());
			orderedChildren = null;
		}
		return childNode;
	}

	public final List getChildrenSorted() {
		final Collection unsortedChildren = getChildrenUnsorted();
		final int numberOfChildren = unsortedChildren.size();
		final ArrayList sortedChildren = new ArrayList(numberOfChildren);

		/*
		 * Step 1. Sort into four groups and one map. One group is the middle
		 * block, which is all children with no ordering constraint. Two other
		 * groups are the start and end blocks, which contain children that only
		 * specify start or end constraints. The last group are those children
		 * that specify a relative constraints. Finally, a map is maintained for
		 * the first three groups (i.e., start, middle and end blocks). This
		 * allows easy look-up of those nodes when the relatively positioned
		 * nodes are eventually merged. Note: each block is sorted
		 * alphabetically. This algorithm takes O(NlogN) time over the number of
		 * children.
		 */
		final Map orderNodeById = new HashMap();
		final List[] blocks = new List[NUMBER_OF_BLOCKS];
		blocks[START] = new ArrayList(numberOfChildren);
		blocks[MIDDLE] = new ArrayList(numberOfChildren);
		blocks[END] = new ArrayList(numberOfChildren);
		final List relativeOrderedChildren = new ArrayList(numberOfChildren);
		final Iterator childItr = unsortedChildren.iterator();
		while (childItr.hasNext()) {
			final LayoutNode child = (LayoutNode) childItr.next();
			final OrderNode orderNode = new OrderNode(child);
			orderNodeById.put(orderNode.getId(), orderNode);
			final SLocation location = child.getLocation();

			/*
			 * Check to see if there is an ordering constraints. If there isn't,
			 * then the item can be added to the fixed block.
			 */
			if (location == null) {
				sortedInsert(blocks[MIDDLE], orderNode);
				continue;
			}
			final SOrder orderingConstraint = location.getOrdering();
			if (orderingConstraint == null) {
				sortedInsert(blocks[MIDDLE], orderNode);
				continue;
			}

			/*
			 * Check to see if there is a relative ordering constraint. If there
			 * is, add it to the relative block.
			 */
			final int position = orderingConstraint.getPosition();
			switch (position) {
			case SOrder.POSITION_AFTER:
			case SOrder.POSITION_BEFORE:
				sortedInsert(relativeOrderedChildren, child);
				break;
			case SOrder.POSITION_START:
				sortedInsert(blocks[START], orderNode);
				break;
			case SOrder.POSITION_END:
				sortedInsert(blocks[END], orderNode);
				break;
			case SOrder.NO_POSITION:
			default:
				sortedInsert(blocks[MIDDLE], orderNode);
			}
		}

		/*
		 * Step 2. Now we have four alphabetically sorted blocks: start, middle,
		 * end and relative. We now need to merge the relatively ordered block
		 * into the other three blocks. This can be done by using the order node
		 * map we built up in step 1.
		 */
		for (int i = 0; i < relativeOrderedChildren.size(); i++) {
			final LayoutNode node = (LayoutNode) relativeOrderedChildren.get(i);
			final String id = node.getId();
			final SLocation location = node.getLocation();
			final SOrder order = location.getOrdering();
			final String relativeTo = order.getRelativeTo();
			final boolean before = order.getPosition() == SOrder.POSITION_BEFORE;

			final OrderNode orderNode = (OrderNode) orderNodeById.get(id);
			final OrderNode relativeNode = (OrderNode) orderNodeById
					.get(relativeTo);
			if (relativeNode == null) {
				// TODO Print error message?
				continue;
			}

			if (before) {
				relativeNode.addBeforeNode(orderNode);
			} else {
				relativeNode.addAfterNode(orderNode);
			}
		}

		/*
		 * Step 3. Copy the order nodes from the start, middle and end blocks
		 * into the final result.
		 */
		for (int i = 0; i < blocks.length; i++) {
			final Iterator itr = blocks[i].iterator();
			while (itr.hasNext()) {
				final OrderNode node = (OrderNode) itr.next();
				node.addTo(sortedChildren);
			}
		}

		return sortedChildren;
	}

	/**
	 * Returns the children of this node, if any. This collection is unsorted.
	 * 
	 * @return The children ({@link LayoutNode}); never <code>null</code>,
	 *         but may be empty.
	 */
	final Collection getChildrenUnsorted() {
		if (childrenById == null) {
			return Collections.EMPTY_LIST;
		}

		return childrenById.values();
	}

	public final String getId() {
		if (element != null) {
			return element.getId();
		}

		return null;
	}

	public final SLocation getLocation() {
		return location;
	}

	public final MenuElement getMenuElement() {
		return element;
	}

	public final boolean isEmpty() {
		return (childrenById == null) || (childrenById.isEmpty());
	}

	public final void propertyChange(final PropertyChangeEvent event) {
		// TODO Respond to dynamic changes.
	}

	public final boolean remove(final MenuElement element) {
		if (orderedChildren != null) {
			orderedChildren.remove(element);
		}
		if (childrenById != null) {
			final String id = element.getId();
			final Object removedObject = childrenById.remove(id);
			return (removedObject != null);
		}

		return false;
	}

	/**
	 * Sets the menu element for this node.
	 * 
	 * @param element
	 *            The element to set; must not be <code>null</code>.
	 */
	final void setElement(final MenuElement element) {
		if (element == null) {
			throw new NullPointerException(
					"A node cannot be given a null element"); //$NON-NLS-1$
		}

		if (this.element != null) {
			this.element.removeListener(this);
		}
		this.element = element;
		if (element != null) {
			element.addListener(this);
		}
	}

	/**
	 * Sets the location for this node.
	 * 
	 * @param location
	 *            The location to set; must not be <code>null</code>.
	 */
	final void setLocation(final SLocation location) {
		if (location == null) {
			throw new NullPointerException(
					"A node cannot be given a null location"); //$NON-NLS-1$
		}

		this.location = location;
	}

	public final String toString() {
		final StringBuffer buffer = new StringBuffer("LayoutNode("); //$NON-NLS-1$
		buffer.append(element);
		buffer.append(',');
		buffer.append(location);
		buffer.append(')');
		return buffer.toString();
	}
}

