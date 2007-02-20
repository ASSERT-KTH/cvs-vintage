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
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.ui.ISourceProvider;

/**
 * @since 3.3
 * 
 */
public final class EvaluationService implements IEvaluationService {
	private EvaluationAuthority evaluationAuthority;
	

	public EvaluationService() {
		evaluationAuthority = new EvaluationAuthority();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addEvaluationListener(org.eclipse.core.expressions.Expression,
	 *      org.eclipse.jface.util.IPropertyChangeListener, java.lang.String)
	 */
	public IEvaluationReference addEvaluationListener(Expression expression,
			IPropertyChangeListener listener, String property) {
		IEvaluationReference ref = new EvaluationReference(expression, listener, property);
		evaluationAuthority.addEvaluationListener(ref);
		return ref;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeEvaluationListener(org.eclipse.ui.internal.services.IEvaluationReference)
	 */
	public void removeEvaluationListener(IEvaluationReference ref) {
		evaluationAuthority.removeEvaluationListener(ref);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#addSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void addSourceProvider(ISourceProvider provider) {
		evaluationAuthority.addSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IServiceWithSources#removeSourceProvider(org.eclipse.ui.ISourceProvider)
	 */
	public void removeSourceProvider(ISourceProvider provider) {
		evaluationAuthority.removeSourceProvider(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.services.IDisposable#dispose()
	 */
	public void dispose() {
		evaluationAuthority.dispose();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#getCurrentState()
	 */
	public IEvaluationContext getCurrentState() {
		return evaluationAuthority.getCurrentState();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#addServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addServiceListener(IPropertyChangeListener listener) {
		evaluationAuthority.addServiceListener(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.services.IEvaluationService#removeServiceListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removeServiceListener(IPropertyChangeListener listener) {
		evaluationAuthority.removeServiceListener(listener);
	}
}
