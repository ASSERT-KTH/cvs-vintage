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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.common.IIdentifiable;

/**
 * <p>
 * A decorator on top of a layout node which allows it to hold ordering
 * information -- including nodes that are ordering relative to it. An order
 * node tracks whether it should appear in the beginning, middle or end block.
 * It also holds layout nodes that are intended to appear before or after it.
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
final class OrderNode implements IIdentifiable {

	/**
	 * The nodes that should appear after this ordering node, in the order they
	 * should appear. This value is <code>null</code> if there are no such
	 * nodes.
	 */
	private List after;

	/**
	 * The nodes that should appear before this ordering node, in the order they
	 * should appear. This value is <code>null</code> if there are no such
	 * nodes.
	 */
	private List before;

	/**
	 * The layout node associated with this ordering node; never
	 * <code>null</code>.
	 */
	private final LayoutNode layoutNode;

	/**
	 * Constructs a new instance of <code>OrderNode</code>.
	 * 
	 * @param layoutNode
	 *            The layout node which is to be ordered; must not be
	 *            <code>null</code>.
	 */
	OrderNode(final LayoutNode layoutNode) {
		if (layoutNode == null) {
			throw new NullPointerException("The layout node cannot be null"); //$NON-NLS-1$
		}

		this.layoutNode = layoutNode;
	}

	/**
	 * Add a node to the list of nodes that appear after this one. This is an
	 * alphabetical sorted insert by id.
	 * 
	 * @param orderNode
	 *            The node to appear after this one; never <code>null</code>.
	 */
	public final void addAfterNode(final OrderNode orderNode) {
		if (after == null) {
			after = new ArrayList(1);
			after.add(orderNode);
			return;
		}

		LayoutNode.sortedInsert(after, orderNode);
	}

	/**
	 * Add a node to the list of nodes that appear before this one. This is an
	 * alphabetical sorted insert by id.
	 * 
	 * @param orderNode
	 *            The node to appear before this one; never <code>null</code>.
	 */
	public final void addBeforeNode(final OrderNode orderNode) {
		if (before == null) {
			before = new ArrayList(1);
			before.add(orderNode);
			return;
		}

		LayoutNode.sortedInsert(before, orderNode);
	}

	/**
	 * @param sortedChildren
	 */
	public final void addTo(final ArrayList sortedChildren) {
		Iterator itr;

		if (before != null) {
			itr = before.iterator();
			while (itr.hasNext()) {
				final OrderNode node = (OrderNode) itr.next();
				node.addTo(sortedChildren);
			}
		}

		sortedChildren.add(getLayoutNode());

		if (after != null) {
			itr = after.iterator();
			while (itr.hasNext()) {
				final OrderNode node = (OrderNode) itr.next();
				node.addTo(sortedChildren);
			}
		}
	}

	/**
	 * Returns the nodes that should appear after this node, in the order they
	 * should appear.
	 * 
	 * @return The nodes that should appear after this node; never
	 *         <code>null</code>.
	 */
	public final List getAfterNodes() {
		if (after == null) {
			return Collections.EMPTY_LIST;
		}

		return after;
	}

	/**
	 * Returns the nodes that should appear before this node, in the order they
	 * should appear.
	 * 
	 * @return The nodes that should appear before this node; never
	 *         <code>null</code>.
	 */
	public final List getBeforeNodes() {
		if (before == null) {
			return Collections.EMPTY_LIST;
		}

		return before;
	}

	public final String getId() {
		return layoutNode.getId();
	}

	/**
	 * Returns the main layout node for this order node.
	 * 
	 * @return The layout node; never <code>null</code>.
	 */
	public final Object getLayoutNode() {
		return layoutNode;
	}
	
	public final String toString() {
		final StringBuffer buffer = new StringBuffer();
		buffer.append("OrderNode("); //$NON-NLS-1$
		buffer.append(layoutNode);
		buffer.append(')');
		return buffer.toString(); 
	}
}
