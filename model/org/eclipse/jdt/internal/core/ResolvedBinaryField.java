/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Handle representing a binary field that is resolved.
 * The uniqueKey contains the genericSignature of the resolved field. Use BindingKey to decode it.
 */
public class ResolvedBinaryField extends BinaryField {
	
	private String uniqueKey;
	
	/*
	 * See class comments.
	 */
	public ResolvedBinaryField(JavaElement parent, String name, String uniqueKey) {
		super(parent, name);
		this.uniqueKey = uniqueKey;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.core.BinaryField#getKey()
	 */
	public String getKey() {
		return this.uniqueKey;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.IField#isResolved()
	 */
	public boolean isResolved() {
		return true;
	}

	/**
	 * @private Debugging purposes
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
		super.toStringInfo(tab, buffer, info);
		buffer.append(" {key="); //$NON-NLS-1$
		buffer.append(this.uniqueKey);
		buffer.append("}"); //$NON-NLS-1$
	}
}
