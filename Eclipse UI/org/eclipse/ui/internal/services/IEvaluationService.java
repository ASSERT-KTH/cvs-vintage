/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.services;

import org.eclipse.core.expressions.Expression;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.services.IServiceWithSources;

/**
 * This should be more a more generic way to have your core expressions
 * evaluated. This is internal in 3.3, and highly experimental.
 * 
 * @since 3.3
 */
public interface IEvaluationService extends IServiceWithSources {
	public static final String RESULT = "org.eclipse.ui.result"; //$NON-NLS-1$

	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property);

	public void removeEvaluationListener(IEvaluationReference ref);
}
