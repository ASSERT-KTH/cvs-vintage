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
package org.eclipse.ui.internal.progress;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IconAndMessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.progress.WorkbenchJob;
/**
 * The BlockedJobsDialog class displays a dialog that provides information on
 * the running jobs.
 */
public class BlockedJobsDialog extends IconAndMessageDialog {
	/**
	 * The singleton dialog instance. A singleton avoids the possibility of
	 * recursive dialogs being created. The singleton is created when a dialog
	 * is requested, and cleared when the dialog is disposed.
	 */
	protected static BlockedJobsDialog singleton;
	/**
	 * The running jobs progress tree.
	 * 
	 * @see org.eclipse.ui.internal.progress.ProgressTreeViewer
	 */
	private ProgressTreeViewer viewer;
	/**
	 * The name of the task that is being blocked.
	 */
	private String blockedTaskName = null;
	/**
	 * The Cancel button control.
	 */
	private Button cancelSelected;
	/**
	 * The cursor for the buttons.
	 */
	private Cursor arrowCursor;
	/**
	 * The cursor for the Shell.
	 */
	private Cursor waitCursor;
	private IProgressMonitor blockingMonitor;
	private JobTreeElement blockedElement = new BlockedUIElement();
	/**
	 * The BlockedUIElement is the JobTreeElement that represents the blocked
	 * job in the dialog.
	 */
	private class BlockedUIElement extends JobTreeElement {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#getChildren()
		 */
		Object[] getChildren() {
			return null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayString()
		 */
		String getDisplayString() {
			if (blockedTaskName == null)
				return ProgressMessages
						.getString("BlockedJobsDialog.UserInterfaceTreeElement"); //$NON-NLS-1$
			else
				return blockedTaskName;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#getDisplayImage()
		 */
		public Image getDisplayImage() {
			return JFaceResources.getImage(ProgressManager.WAITING_JOB_KEY);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#getParent()
		 */
		Object getParent() {
			return null;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#hasChildren()
		 */
		boolean hasChildren() {
			return false;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#isActive()
		 */
		boolean isActive() {
			return true;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#isJobInfo()
		 */
		boolean isJobInfo() {
			return false;
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#cancel()
		 */
		public void cancel() {
			blockingMonitor.setCanceled(true);
		}
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.progress.JobTreeElement#isCancellable()
		 */
		public boolean isCancellable() {
			return true;
		}
	}
	/**
	 * Creates a progress monitor dialog under the given shell. It also sets the
	 * dialog's message. The dialog is opened automatically after a reasonable
	 * delay. When no longer needed, the dialog must be closed by calling
	 * <code>close(IProgressMonitor)</code>, where the supplied monitor is
	 * the same monitor passed to this factory method.
	 * 
	 * @param parentShell
	 *            The parent shell, or <code>null</code> to create a top-level
	 *            shell.
	 * @param monitor
	 *            The monitor that is currently blocked
	 * @param reason
	 *            A status describing why the monitor is blocked
	 */
	public static BlockedJobsDialog createBlockedDialog(Shell parentShell,
			IProgressMonitor monitor, IStatus reason) {
		//use an existing dialog if available
		if (singleton != null)
			return singleton;
		singleton = new BlockedJobsDialog(parentShell, monitor, reason);
		//create the job that will open the dialog after a delay.
		WorkbenchJob dialogJob = new WorkbenchJob(WorkbenchMessages
				.getString("EventLoopProgressMonitor.OpenDialogJobName")) { //$NON-NLS-1$
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
			 */
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (singleton == null)
					return Status.CANCEL_STATUS;
				if (ProgressManagerUtil.rescheduleIfModalShellOpen(this))
						return Status.CANCEL_STATUS;
					singleton.open();
				return Status.OK_STATUS;
			}
		};
		//Wait for long operation time to prevent a proliferation
		//of dialogs
		dialogJob.setSystem(true);
		dialogJob.schedule(PlatformUI.getWorkbench().getProgressService()
				.getLongOperationTime());
		return singleton;
	}
	/**
	 * Creates a progress monitor dialog under the given shell. It also sets the
	 * dialog's\ message. <code>open</code> is non-blocking.
	 * 
	 * @param parentShell
	 *            The parent shell, or <code>null</code> to create a top-level
	 *            shell.
	 * @param blocking
	 *            The monitor that is blocking the job
	 * @param blockingStatus
	 *            A status describing why the monitor is blocked
	 */
	private BlockedJobsDialog(Shell parentShell, IProgressMonitor blocking,
			IStatus blockingStatus) {
		super(parentShell == null
				? ProgressManagerUtil.getDefaultParent()
				: parentShell);
		blockingMonitor = blocking;
		setShellStyle(SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		// no close button
		setBlockOnOpen(false);
		setMessage(blockingStatus.getMessage());
	}
	/**
	 * This method creates the dialog area under the parent composite.
	 * 
	 * @param parent
	 *            The parent Composite.
	 * 
	 * @return parent The parent Composite.
	 */
	protected Control createDialogArea(Composite parent) {
		setMessage(message);
		createMessageArea(parent);
		showJobDetails(parent);
		return parent;
	}
	/**
	 * This method creates a dialog area in the parent composite and displays a
	 * progress tree viewer of the running jobs.
	 * 
	 * @param parent
	 *            The parent Composite.
	 */
	void showJobDetails(Composite parent) {
		viewer = new NewProgressViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.ProgressTreeViewer#updateColors(org.eclipse.swt.widgets.TreeItem,
			 *      org.eclipse.ui.internal.progress.JobTreeElement)
			 */
			protected void updateColors(TreeItem treeItem,
					JobTreeElement element) {
				super.updateColors(treeItem, element);
				//Color the blocked element the not running color.
				if (element == blockedElement)
					setNotRunningColor(treeItem);
			}
		};
		viewer.setUseHashlookup(true);
		viewer.setSorter(new ViewerSorter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
			 *      java.lang.Object, java.lang.Object)
			 */
			public int compare(Viewer testViewer, Object e1, Object e2) {
				return ((Comparable) e1).compareTo(e2);
			}
		});
		IContentProvider provider = getContentProvider();
		viewer.setContentProvider(provider);
		viewer.setInput(provider);
		viewer.setLabelProvider(new ProgressLabelProvider());
		GridData data = new GridData();
		data.horizontalSpan = 2;
		int heightHint = convertHeightInCharsToPixels(10);
		data.heightHint = heightHint;
		data.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(data);
	}
	/**
	 * Return the content provider used for the receiver.
	 * 
	 * @return ProgressTreeContentProvider
	 */
	private ProgressTreeContentProvider getContentProvider() {
		return new ProgressTreeContentProvider(viewer, true) {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.ui.internal.progress.ProgressContentProvider#getElements(java.lang.Object)
			 */
			public Object[] getElements(Object inputElement) {
				Object[] elements = super.getElements(inputElement);
				Object[] result = new Object[elements.length + 1];
				System.arraycopy(elements, 0, result, 1, elements.length);
				result[0] = blockedElement;
				return result;
			}
		};
	}
	/**
	 * Clear the cursors in the dialog.
	 */
	private void clearCursors() {
		clearCursor(cancelSelected);
		clearCursor(getShell());
		if (arrowCursor != null)
			arrowCursor.dispose();
		if (waitCursor != null)
			waitCursor.dispose();
		arrowCursor = null;
		waitCursor = null;
	}
	/**
	 * Clear the cursor on the supplied control.
	 * 
	 * @param control
	 */
	private void clearCursor(Control control) {
		if (control != null && !control.isDisposed()) {
			control.setCursor(null);
		}
	}
	/**
	 * This method complements the Window's class' configureShell method by
	 * adding a title, and setting the appropriate cursor.
	 * 
	 * @param shell
	 *            The dialog's shell.
	 * 
	 * @see /org.eclipse.jface/src/org/eclipse/jface/window/Window.java
	 *      (org.eclipse.jface.window;)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(ProgressMessages
				.getString("BlockedJobsDialog.BlockedTitle")); //$NON-NLS-1$
		if (waitCursor == null)
			waitCursor = new Cursor(shell.getDisplay(), SWT.CURSOR_WAIT);
		shell.setCursor(waitCursor);
	}
	/**
	 * This method sets the message in the message label.
	 */
	private void setMessage(String messageString) {
		//must not set null text in a label
		message = messageString == null ? "" : messageString; //$NON-NLS-1$
		if (messageLabel == null || messageLabel.isDisposed())
			return;
		messageLabel.setText(message);
	}
	/**
	 * This method returns the dialog's lock image.
	 */
	protected Image getImage() {
		return JFaceResources.getImageRegistry().get(Dialog.DLG_IMG_INFO);
	}
	/**
	 * Returns the progress monitor being used for this dialog. This allows
	 * recursive blockages to also respond to cancelation.
	 * 
	 * @return
	 */
	public IProgressMonitor getProgressMonitor() {
		return blockingMonitor;
	}
	/**
	 * Requests that the blocked jobs dialog be closed. The supplied monitor
	 * must be the same one that was passed to the createBlockedDialog method.
	 */
	public boolean close(IProgressMonitor monitor) {
		//ignore requests to close the dialog from all but the first monitor
		if (blockingMonitor != monitor)
			return false;		
		return close();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#close()
	 */
	public boolean close() {
		//Clear the singleton first
		singleton = null;
		clearCursors();
		return super.close();
	}
	/**
	 * Set the name of the task being blocked. If this value is not set then the
	 * default blocked name will be used.
	 * 
	 * @param taskName
	 */
	public void setBlockedTaskName(String taskName) {
		blockedTaskName = taskName;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IconAndMessageDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createButtonBar(Composite parent) {
		// Do nothing here as we want no buttons
		return parent;
	}
}