/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.LinearUndoViolationDetector;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * <p>
 * An operation approver that prompts the user to see if linear undo violations
 * are permitted. A linear undo violation is detected when an operation being
 * undone or redone shares a context with another operation appearing more
 * recently in the history.
 * </p>
 * 
 * @since 3.1
 */
public class LinearUndoViolationUserApprover extends
		LinearUndoViolationDetector {

	private IEditorPart part;

	private IUndoContext context;

	/**
	 * Create a LinearUndoViolationUserApprover associated with the specified
	 * workbench part.
	 * 
	 * @param context -
	 *            the context with the linear undo violation
	 * @param part -
	 *            the part that should be used for prompting the user.
	 */
	public LinearUndoViolationUserApprover(IUndoContext context,
			IEditorPart part) {
		super();
		this.part = part;
		this.context = context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.LinearUndoViolationDetector#allowLinearRedoViolation(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IUndoContext,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected IStatus allowLinearRedoViolation(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable uiInfo) {

		if (this.context != context) {
			return Status.OK_STATUS;
		}

		String message = NLS.bind(
				WorkbenchMessages.Operations_linearRedoViolation, part
						.getEditorInput().getName(), operation.getLabel());
		// Show a dialog.
		part.setFocus();
		boolean proceed = MessageDialog.openQuestion(part.getSite().getShell(),
				part.getEditorInput().getName(), message);

		if (proceed) {
			// redo the local changes first
			while (operation != history.getRedoOperation(context)) {
				try {
					history.redo(context, null, uiInfo);
				} catch (ExecutionException e) {
					// flush the redo history here because it failed.
					history.dispose(context, false, true, false);
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.operations.LinearUndoViolationDetector#allowLinearUndoViolation(org.eclipse.core.commands.operations.IUndoableOperation,
	 *      org.eclipse.core.commands.operations.IUndoContext,
	 *      org.eclipse.core.commands.operations.IOperationHistory,
	 *      org.eclipse.core.runtime.IAdaptable)
	 */
	protected IStatus allowLinearUndoViolation(IUndoableOperation operation,
			IUndoContext context, IOperationHistory history, IAdaptable uiInfo) {

		if (this.context != context) {
			return Status.OK_STATUS;
		}

		String message = NLS.bind(
				WorkbenchMessages.Operations_linearUndoViolation, part
						.getEditorInput().getName(), operation.getLabel());
		// Show a dialog.
		part.setFocus();
		boolean proceed = MessageDialog.openQuestion(part.getSite().getShell(),
				part.getEditorInput().getName(), message);

		if (proceed) {
			// redo the local changes first
			while (operation != history.getUndoOperation(context)) {
				try {
					history.undo(context, null, uiInfo);
				} catch (ExecutionException e) {
					// flush the undo history here because something went wrong.
					history.dispose(context, true, false, false);
					return Status.CANCEL_STATUS;
				}
			}
			return Status.OK_STATUS;
		}
		return Status.CANCEL_STATUS;
	}
}
