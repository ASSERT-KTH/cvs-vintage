package org.eclipse.jface.wizard;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressIndicator;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

/**
 * A standard implementation of an IProgressMonitor. It consists
 * of a label displaying the task and subtask name, and a
 * progress indicator to show progress. In contrast to 
 * <code>ProgressMonitorDialog</code> this class only implements
 * <code>IProgressMonitor</code>.
 */
public class ProgressMonitorPart extends Composite implements IProgressMonitor {

	protected Label fLabel;
	protected String fTaskName;
	protected String fSubTaskName;
	protected ProgressIndicator fProgressIndicator;
	protected Control fCancelComponent;
	protected boolean fIsCanceled;
	
	protected Listener fCancelListener= new Listener() {
		public void handleEvent(Event e) {
			setCanceled(true);
			if (fCancelComponent != null)
				fCancelComponent.setEnabled(false);
		}	
	};
	
	/**
	 * Creates a ProgressMonitorPart.
	 * @param parent The SWT parent of the part.
	 * @param layout The SWT grid bag layout used by the part. A client
	 * can supply the layout to control how the progress monitor part
	 * is layed out. If null is passed the part uses its default layout.
	 */
	public ProgressMonitorPart(Composite parent, Layout layout) {
		this(parent, layout, SWT.DEFAULT);
	}
	/**
	 * Creates a ProgressMonitorPart.
	 * @param parent The SWT parent of the part.
	 * @param layout The SWT grid bag layout used by the part. A client
	 * can supply the layout to control how the progress monitor part
	 * is layed out. If null is passed the part uses its default layout.
	 * @param progressIndicatorHeight The height of the progress indicator in pixel.
	 */
	public ProgressMonitorPart(Composite parent, Layout layout, int progressIndicatorHeight) {
		super(parent, SWT.NONE);
		initialize(layout, progressIndicatorHeight);
	}
	/**
	 * Attaches the progress monitor part to the given cancel
	 * component. 
	 */
	public void attachToCancelComponent(Control cancelComponent) {
		Assert.isNotNull(cancelComponent);
		fCancelComponent= cancelComponent;
		fCancelComponent.addListener(SWT.Selection, fCancelListener);
	}
	/**
	 * Implements <code>IProgressMonitor.beginTask</code>.
	 * @see IProgressMonitor#beginTask(java.lang.String, int)
	 */
	public void beginTask(String name, int totalWork) {
		fTaskName= name;
		updateLabel();
		if (totalWork == IProgressMonitor.UNKNOWN || totalWork == 0) {
			fProgressIndicator.beginAnimatedTask();
		} else {
			fProgressIndicator.beginTask(totalWork);
		}	
	}
	/**
	 * Implements <code>IProgressMonitor.done</code>.
	 * @see IProgressMonitor#done()
	 */
	public void done() {
		fLabel.setText("");//$NON-NLS-1$
		fProgressIndicator.sendRemainingWork();
		fProgressIndicator.done();
	}
	/**
	 * Escapes any occurrence of '&' in the given String so that
	 * it is not considered as a mnemonic
	 * character in SWT ToolItems, MenuItems, Button and Labels.
	 */
	protected static String escapeMetaCharacters(String in) {
		if (in == null || in.indexOf('&') < 0)
			return in;
		int length= in.length();
		StringBuffer out= new StringBuffer(length+1);
		for (int i= 0; i < length; i++) {
			char c= in.charAt(i);
			if (c == '&')
				out.append("&&");//$NON-NLS-1$
			else
				out.append(c);
		}
		return out.toString();
	}
	/**
	 * Creates the progress monitor's UI parts and layouts them
	 * according to the given layout. If the layou is <code>null</code>
	 * the part's default layout is used.
	 */
	protected void initialize(Layout layout, int progressIndicatorHeight) {
		if (layout == null) {
			GridLayout l= new GridLayout();
			l.marginWidth= 0;
			l.marginHeight= 0;
			l.numColumns= 1;
			layout= l;
		}
		setLayout(layout);
	
		fLabel= new Label(this, SWT.LEFT);
		fLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	
		if (progressIndicatorHeight == SWT.DEFAULT) {
			GC gc= new GC(fLabel);
			FontMetrics fm= gc.getFontMetrics();
			gc.dispose();
			progressIndicatorHeight= fm.getHeight();
		}
		
		fProgressIndicator= new ProgressIndicator(this);
		GridData gd= new GridData();
		gd.horizontalAlignment= GridData.FILL;
		gd.grabExcessHorizontalSpace= true;
		gd.verticalAlignment= GridData.CENTER;
		gd.heightHint= progressIndicatorHeight;
		fProgressIndicator.setLayoutData(gd);
	}
	/**
	 * Implements <code>IProgressMonitor.internalWorked</code>.
	 * @see IProgressMonitor#internalWorked(double)
	 */
	public void internalWorked(double work) {
		fProgressIndicator.worked(work);
	}
	/**
	 * Implements <code>IProgressMonitor.isCanceled</code>.
	 * @see IProgressMonitor#isCanceled()
	 */
	public boolean isCanceled() {
		return fIsCanceled;
	}
	/**
	 * Detached the progress monitor part to the given cancel
	 * component
	 */
	public void removeFromCancelComponent(Control cc) {
		Assert.isTrue(fCancelComponent == cc && fCancelComponent != null);
		fCancelComponent.removeListener(SWT.Selection, fCancelListener);
		fCancelComponent= null;
	}
	/**
	 * Implements <code>IProgressMonitor.setCanceled</code>.
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	public void setCanceled(boolean b) {
		fIsCanceled= b;
	}
	/**
	 * Sets the progress monitor part's font.
	 */
	public void setFont(Font font) {
		super.setFont(font);
		fLabel.setFont(font);
		fProgressIndicator.setFont(font);
	}
	/**
	 * @see IProgressMonitor#setTaskName(java.lang.String)
	 */
	public void setTaskName(String name) {
		fTaskName= name;
		updateLabel();
	}
	/**
	 * Implements <code>IProgressMonitor.subTask</code>.
	 * @see IProgressMonitor#subTask(java.lang.String)
	 */
	public void subTask(String name) {
		fSubTaskName= name;
		updateLabel();
	}

	/**
	 * Updates the label with the current task and subtask names.
	 */
	protected void updateLabel() {
		String text = fSubTaskName == null ? "" : fSubTaskName; //$NON-NLS-1$
		if (fTaskName != null && fTaskName.length() > 0) {
			text = JFaceResources.format("Set_SubTask", new Object[] {fTaskName, text});//$NON-NLS-1$
		}
		fLabel.setText(escapeMetaCharacters(text));
		//Force an update as we are in the UI Thread
		fLabel.update();
	}

	/**
	 * Implements <code>IProgressMonitor.worked</code>.
	 * @see IProgressMonitor#worked(int)
	 */
	public void worked(int work) {
		internalWorked(work);
	}
}
