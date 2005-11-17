/******************************************************************************* * Copyright (c) 2005 IBM Corporation and others. * All rights reserved. This program and the accompanying materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: *     IBM Corporation - initial API and implementation ******************************************************************************/package org.eclipse.ui.internal.menus;import org.eclipse.core.expressions.EvaluationResult;import org.eclipse.core.expressions.Expression;import org.eclipse.core.expressions.ExpressionInfo;import org.eclipse.core.expressions.IEvaluationContext;import org.eclipse.ui.ISources;import org.eclipse.ui.internal.util.Util;/** * <p> * An expression representing the <code>targetId</code> of the legacy editor * contributions. * </p> * <p> * Clients may neither instantiate nor extend this class. * </p> * <p> * <strong>EXPERIMENTAL</strong>. This class or interface has been added as * part of a work in progress. There is a guarantee neither that this API will * work nor that it will remain the same. Please do not use this API without * consulting with the Platform/UI team. * </p> *  * @since 3.2 */final class LegacyEditorContributionExpression extends Expression {	/**	 * The identifier for the editor that must be active for this expression to	 * evaluate to <code>true</code>. This value is never <code>null</code>.	 */	private final String activeEditorId;	/**	 * Constructs a new instance of	 * <code>LegacyEditorContributionExpression</code>	 * 	 * @param activeEditorId	 *            The identifier of the editor to match with the active editor;	 *            may be <code>null</code>	 */	public LegacyEditorContributionExpression(final String activeEditorId) {		if (activeEditorId == null) {			throw new NullPointerException(					"The targetId for an editor contribution must not be null"); //$NON-NLS-1$		}		this.activeEditorId = activeEditorId;	}	/**	 * Evaluates this expression. This test whether the active editor is the	 * same as the one specified in the constructor.	 * 	 * @param context	 *            The context providing the current workbench state; must not be	 *            <code>null</code>.	 * @return <code>EvaluationResult.TRUE</code> if the conditions all	 *         matches; <code>EvaluationResult.FALSE</code> otherwise.	 */	public final EvaluationResult evaluate(final IEvaluationContext context) {		final Object value = context.getVariable(ISources.ACTIVE_EDITOR_NAME);		if (Util.equals(activeEditorId, value)) {			return EvaluationResult.TRUE;		}		return EvaluationResult.FALSE;	}	public final void collectExpressionInfo(final ExpressionInfo info) {		info.addVariableNameAccess(ISources.ACTIVE_EDITOR_NAME);	}	public final String toString() {		final StringBuffer buffer = new StringBuffer();		buffer.append("LegacyEditorContributionExpression("); //$NON-NLS-1$		buffer.append(activeEditorId);		buffer.append(')');		return buffer.toString();	}}