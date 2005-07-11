package org.eclipse.ui.internal.progress;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.progress.IProgressConstants;

/**
 * JobTreeElementInfoItem is the abstract superclass of items for displaying
 * jobs and groups of jobs.
 * 
 * @since 3.1
 * 
 */
class ProgressInfoItem extends Composite {

	static String STOP_IMAGE_KEY = "org.eclipse.ui.internal.progress.PROGRESS_STOP_IMAGE_KEY"; //$NON-NLS-1$

	JobTreeElement info;

	Label progressLabel;

	Button stopButton;

	List taskEntries = new ArrayList(0);

	private ProgressBar progressBar;

	static {
		JFaceResources
				.getImageRegistry()
				.put(
						STOP_IMAGE_KEY,
						WorkbenchImages
								.getWorkbenchImageDescriptor("elcl16/progress_stop.gif"));//$NON-NLS-1$

	}

	/**
	 * Create a new instance of the receiver with the specified parent, style
	 * and info object/
	 * 
	 * @param parent
	 * @param style
	 * @param progressInfo
	 */
	public ProgressInfoItem(Composite parent, int style,
			JobTreeElement progressInfo) {
		super(parent, style);
		info = progressInfo;
		createChildren();
		setData(info);
		setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

	}

	/**
	 * Create the child widgets of the receiver.
	 */
	protected void createChildren() {
		FormLayout layout = new FormLayout();
		setLayout(layout);

		progressLabel = new Label(this, SWT.NONE);
		progressLabel.setFont(JFaceResources.getFontRegistry().getBold(
				JFaceResources.DEFAULT_FONT));
		progressLabel.setText(getMainTitle());
		FormData progressData = new FormData();
		progressData.top = new FormAttachment(0);
		progressData.left = new FormAttachment(
				IDialogConstants.HORIZONTAL_SPACING);
		progressData.right = new FormAttachment(100);
		progressLabel.setLayoutData(progressData);

		stopButton = new Button(this, SWT.FLAT);

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		buttonData.right = new FormAttachment(100,
				IDialogConstants.HORIZONTAL_SPACING * -1);

		setButtonImage(buttonData);

		stopButton.setLayoutData(buttonData);
		stopButton.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				info.cancel();
				layout(true);
			}
		});

		refresh();
	}

	/**
	 * Get the main title for the receiver.
	 * 
	 * @return String
	 */
	private String getMainTitle() {
		if (info.isJobInfo())
			return getJobNameAndStatus(false, false);
		return ((GroupInfo) info).getTaskName();
	}

	/**
	 * Get the name and status for the main label.
	 * 
	 * @param terminated
	 *            A boolean indicate that job is terminated
	 * @param withTime
	 *            A boolean to indicate that time information should be shown if
	 *            terminated/
	 * @return String
	 */
	protected String getJobNameAndStatus(boolean terminated, boolean withTime) {

		JobInfo jobInfo = (JobInfo) info;
		Job job = jobInfo.getJob();

		String name = job.getName();

		if (job.isSystem())
			name = NLS.bind(ProgressMessages.JobInfo_System, name);

		if (jobInfo.isCanceled())
			return NLS.bind(ProgressMessages.JobInfo_Cancelled, name);

		if (terminated)
			return getJobInfoFinishedString(job, withTime);

		if (jobInfo.isBlocked()) {
			IStatus blockedStatus = jobInfo.getBlockedStatus();
			return NLS.bind(ProgressMessages.JobInfo_Blocked, name,
					blockedStatus.getMessage());
		}

		switch (job.getState()) {
		case Job.RUNNING:
			return name;
		case Job.SLEEPING:
			return NLS.bind(ProgressMessages.JobInfo_Sleeping, name);
		default:
			return NLS.bind(ProgressMessages.JobInfo_Waiting, name);
		}
	}

	/**
	 * Return the finished String for a job.
	 * 
	 * @param job
	 *            the completed Job
	 * @param withTime
	 * @return String
	 */
	String getJobInfoFinishedString(Job job, boolean withTime) {
		String time = null;
		if (withTime)
			time = getTimeString();
		if (time != null)
			return NLS.bind(ProgressMessages.JobInfo_FinishedAt, job.getName(),
					time);
		return NLS.bind(ProgressMessages.JobInfo_Finished, job.getName());
	}

	/**
	 * Get the time string the finished job
	 * 
	 * @return String or <code>null</code> if this is not one of the finished
	 *         jobs.
	 */
	private String getTimeString() {
		Date date = ProgressManager.getInstance().finishedJobs
				.getFinishDate(info);
		if (date != null)
			return DateFormat.getTimeInstance(DateFormat.SHORT).format(date);
		return null;
	}

	/**
	 * Set the button image and update the button data to reflect it.
	 * 
	 * @param buttonData
	 */
	protected void setButtonImage(FormData buttonData) {
		Image stopImage = JFaceResources.getImageRegistry().get(STOP_IMAGE_KEY);
		Rectangle imageBounds = stopImage.getBounds();
		stopButton.setImage(stopImage);

		buttonData.height = imageBounds.height;
		buttonData.width = imageBounds.width;
	}

	/**
	 * Refresh the contents of the receiver.
	 * 
	 */
	void refresh() {

		if (isDisposed())
			return;

		progressLabel.setText(getMainTitle());
		int percentDone = getPercentDone();

		if (isRunning()) {
			if (progressBar == null)
				createProgressBar(percentDone == IProgressMonitor.UNKNOWN ? SWT.INDETERMINATE
						: SWT.NONE);

			// Protect against bad counters
			if (percentDone >= 0 && percentDone <= 100)
				progressBar.setSelection(percentDone);
		}

		JobInfo[] infos = getJobInfos();
		int taskCount = 0;
		for (int i = 0; i < infos.length; i++) {
			JobInfo jobInfo = infos[i];
			if (jobInfo.hasTaskInfo()) {

				String taskString = jobInfo.getTaskInfo().getTaskName();
				String subTaskString = null;
				Object[] jobChildren = info.getChildren();
				if (jobChildren.length > 0)
					subTaskString = ((JobTreeElement) jobChildren[0])
							.getDisplayString();

				if (subTaskString != null) {
					if (taskString == null)
						taskString = subTaskString;
					else
						taskString = NLS.bind(
								ProgressMessages.JobInfo_DoneNoProgressMessage,
								taskString, subTaskString);
				}
				if (taskString != null) {
					setLinkText(infos[i].getJob(),taskString, taskCount);
					taskCount++;
				}
			}
		}

		// Remove completed tasks
		if (taskCount < taskEntries.size()) {
			for (int i = taskCount; i < taskEntries.size(); i++) {
				((Link) taskEntries.get(i)).dispose();

			}
			taskEntries = taskEntries.subList(0, taskCount + 1);
		}

	}

	/**
	 * Return the job infos in the receiver.
	 * @return JobInfo[]
	 */
	private JobInfo[] getJobInfos() {
		if(info.isJobInfo())
			return new JobInfo[] {(JobInfo) info};
		Object[] children = info.getChildren();
		JobInfo[] infos = new JobInfo[children.length];
		System.arraycopy(children,0,infos,0,children.length);
		return infos;
	}

	/**
	 * Return whether or not the receiver is being displayed as running.
	 * 
	 * @return boolean
	 */
	private boolean isRunning() {
		if (info.isJobInfo())
			return ((JobInfo) info).getJob().getState() == Job.RUNNING;
		return true;
	}

	/**
	 * Get the current percent done.
	 * 
	 * @return int
	 */
	private int getPercentDone() {
		if (info.isJobInfo())
			return ((JobInfo) info).getPercentDone();
		return ((GroupInfo) info).getPercentDone();
	}

	void remap(JobTreeElement element) {
		info =  element;
		setData(element);
		refresh();

	}

	/**
	 * Create the progress bar and apply any style bits from style.
	 * 
	 * @param style
	 */
	void createProgressBar(int style) {
		progressBar = new ProgressBar(this, SWT.HORIZONTAL | style);
		FormData barData = new FormData();
		barData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		barData.left = new FormAttachment(IDialogConstants.HORIZONTAL_SPACING);
		barData.right = new FormAttachment(stopButton,
				IDialogConstants.HORIZONTAL_SPACING * -1);
		progressBar.setLayoutData(barData);

		FormData buttonData = new FormData();
		buttonData.top = new FormAttachment(progressLabel,
				IDialogConstants.VERTICAL_SPACING);
		buttonData.right = new FormAttachment(100,
				IDialogConstants.HORIZONTAL_SPACING * -1);
		setButtonImage(buttonData);

		stopButton.setLayoutData(buttonData);

		if (taskEntries.size() > 0) {
			// Reattach the link label if there is one
			FormData linkData = new FormData();
			linkData.top = new FormAttachment(progressBar,
					IDialogConstants.VERTICAL_SPACING);
			linkData.left = new FormAttachment(
					IDialogConstants.HORIZONTAL_SPACING);
			linkData.right = new FormAttachment(100);

			((Link) taskEntries.get(0)).setLayoutData(linkData);
		}
	}

	/**
	 * Set the text of the link to the taskString.
	 * 
	 * @param taskString
	 */
	void setLinkText(Job linkJob, String taskString, int index) {

		Link link;
		if (index >= taskEntries.size()) {// Is it new?
			link = new Link(this, SWT.NONE);

			FormData linkData = new FormData();
			if (index == 0) {
				Control top = progressBar;
				if (top == null)
					top = progressLabel;
				linkData.top = new FormAttachment(top,
						IDialogConstants.VERTICAL_SPACING);
			} else {
				linkData.top = new FormAttachment((Link) taskEntries
						.get(index - 1), IDialogConstants.VERTICAL_SPACING);
			}

			linkData.left = new FormAttachment(
					IDialogConstants.HORIZONTAL_SPACING);
			linkData.right = new FormAttachment(100);
			link.setLayoutData(linkData);

			final Link finalLink = link;

			link.addSelectionListener(new SelectionAdapter() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				public void widgetSelected(SelectionEvent e) {
					((IAction) finalLink.getData()).run();
				}
			});
			taskEntries.add(link);
		} else
			link = (Link) taskEntries.get(index);

		// check for action property
		Object property = linkJob
				.getProperty(IProgressConstants.ACTION_PROPERTY);
		if (property instanceof IAction) {
			link.setData(property);
		}

		link.setText(property == null ? taskString : NLS.bind(
				"<a>{0}</a>", taskString));//$NON-NLS-1$

	}

	/**
	 * Set the color base on the index
	 * @param i
	 */
	public void setColor(int i) {
		if (i % 2 == 0)
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_LIST_BACKGROUND));
		else
			setAllBackgrounds(getDisplay().getSystemColor(
					SWT.COLOR_WIDGET_LIGHT_SHADOW));
	
	}

	/**
	 * Set the background of all widgets to the supplied systemCOlour
	 * 
	 * @param systemColor
	 */
	private void setAllBackgrounds(Color systemColor) {
		setBackground(systemColor);
		progressLabel.setBackground(systemColor);
		stopButton.setBackground(systemColor);
	
		Iterator taskEntryIterator = taskEntries.iterator();
		while(taskEntryIterator.hasNext()){
			((Link) taskEntryIterator.next()).setBackground(systemColor);
		}
		if (progressBar != null)
			progressBar.setBackground(systemColor);
	}

}
