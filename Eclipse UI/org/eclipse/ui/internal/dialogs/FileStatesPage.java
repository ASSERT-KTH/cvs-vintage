package org.eclipse.ui.internal.dialogs;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.MessageFormat;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.help.WorkbenchHelp;
import org.eclipse.ui.internal.IHelpContextIds;
import org.eclipse.ui.internal.WorkbenchMessages;
/**
 * The FileStatesPage is the page used to set the file states sizes for the workbench.
 */
public class FileStatesPage
	extends PreferencePage
	implements IWorkbenchPreferencePage, Listener {

	private static final long defaultFileStateLongevity = 7;	// 7 days
	private static final long defaultMaxFileStateSize = 1; // 1 Mb
	private static final int defaultMaxFileStates = 50;

	private static final String LONGEVITY_TITLE =
		WorkbenchMessages.getString("FileHistory.longevity"); //$NON-NLS-1$
	private static final String MAX_FILE_STATES_TITLE =
		WorkbenchMessages.getString("FileHistory.entries"); //$NON-NLS-1$
	private static final String MAX_FILE_STATE_SIZE_TITLE =
		WorkbenchMessages.getString("FileHistory.diskSpace"); //$NON-NLS-1$
	private static final String POSITIVE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.mustBePositive"); //$NON-NLS-1$
	private static final String INVALID_VALUE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.invalid"); //$NON-NLS-1$
	private static final String SAVE_ERROR_MESSAGE =
		WorkbenchMessages.getString("FileHistory.exceptionSaving"); //$NON-NLS-1$
	private static final String NOTE_MESSAGE =
		WorkbenchMessages.getString("FileHistory.restartNote"); //$NON-NLS-1$
	private static final String NOTE_LABEL = 
		WorkbenchMessages.getString("Preference.note"); //$NON-NLS-1$

	private static final int FAILED_VALUE = -1;

	//Set the length of the day as we have to convert back and forth
	private static final long DAY_LENGTH = 86400000;
	private static final long MEGABYTES = 1024 * 1024;

	private Text longevityText;
	private Text maxStatesText;
	private Text maxStateSizeText;
	
	//Choose a maximum to prevent OutOfMemoryErrors
	private int FILE_STATES_MAXIMUM = 10000;
	private long STATE_SIZE_MAXIMUM = 100;

/**
 * This method takes the string for the title of a text field and the value for the
 * text of the field.
 * @return org.eclipse.swt.widgets.Text
 * @param labelString java.lang.String
 * @param textValue java.lang.String
 * @param parent Composite 
 */
private Text addLabelAndText(String labelString, String textValue, Composite parent) {
	Label label = new Label(parent,SWT.LEFT);
	label.setText(labelString);
	label.setFont(parent.getFont());
	
	Text text = new Text(parent, SWT.LEFT | SWT.BORDER);
	GridData data = new GridData();
	text.addListener(SWT.Modify, this);
	data.horizontalAlignment = GridData.FILL;
	data.grabExcessHorizontalSpace = true;
	data.verticalAlignment = GridData.CENTER;
	data.grabExcessVerticalSpace = false;
	text.setLayoutData(data);
	text.setText(textValue);
	text.setFont(parent.getFont());
	return text;
}
/**
 * Recomputes the page's error state by validating all
 * the fields.
 */
private void checkState() {
	// Assume invalid if the controls not created yet
	if (longevityText == null || maxStatesText == null || maxStateSizeText == null) {
		setValid(false);
		return;
	}

	if (validateLongTextEntry(longevityText) == FAILED_VALUE) {
		setValid(false);
		return;
	}
	
	if (validateMaxFileStates() == FAILED_VALUE) {
		setValid(false);
		return;
	}
	
	if (validateMaxFileStateSize() == FAILED_VALUE) {
		setValid(false);
		return;
	}

	setValid(true);
	setErrorMessage(null);
}
/* 
* Create the contents control for the workspace file states.
* @returns Control
* @param parent Composite
*/

protected Control createContents(Composite parent) {

	WorkbenchHelp.setHelp(parent, IHelpContextIds.FILE_STATES_PREFERENCE_PAGE);
	Font font = parent.getFont();

	// button group
	Composite composite = new Composite(parent, SWT.NONE);
	GridLayout layout = new GridLayout();
	layout.numColumns = 2;
	composite.setLayout(layout);
	composite.setFont(parent.getFont());

	IWorkspaceDescription description = getWorkspaceDescription();

	//Get the current value and make sure we get at least one day out of it.
	long days = description.getFileStateLongevity() / DAY_LENGTH;
	if (days < 1)
		days = 1;

	long megabytes = description.getMaxFileStateSize() / MEGABYTES;
	if (megabytes < 1)
		megabytes = 1;

	this.longevityText =
		addLabelAndText(LONGEVITY_TITLE, String.valueOf(days), composite);
	this.maxStatesText =
		addLabelAndText(
			MAX_FILE_STATES_TITLE,
			String.valueOf(description.getMaxFileStates()),
			composite);
	this.maxStateSizeText =
		addLabelAndText(
			MAX_FILE_STATE_SIZE_TITLE,
			String.valueOf(megabytes),
			composite);

	checkState();
	
	//Create a spacing label to breakup the note from the fields
	Label spacer = new Label(composite, SWT.NONE);
	GridData spacerData = new GridData();
	spacerData.horizontalSpan = 2;
	spacer.setLayoutData(spacerData);
	
	createNoteLabel(composite);
	
	return composite;
}
/**
 * Get the Workspace this page is operating on.
 * @return org.eclipse.core.internal.resources.IWorkspace
 */
private IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
/**
 * Get the Workspace Description this page is operating on.
 * @return org.eclipse.core.resources.IWorkspaceDescription
 */
private IWorkspaceDescription getWorkspaceDescription() {
	return ResourcesPlugin.getWorkspace().getDescription();
}
/**
 * Sent when an event that the receiver has registered for occurs.
 *
 * @param event the event which occurred
 */
public void handleEvent(Event event) {
	checkState();
}
/**
 * Initializes this preference page for the given workbench.
 * <p>
 * This method is called automatically as the preference page is being created
 * and initialized. Clients must not call this method.
 * </p>
 *
 * @param workbench the workbench
 */
public void init(org.eclipse.ui.IWorkbench workbench) {}
/**
 * Performs special processing when this page's Defaults button has been pressed.
 * Reset the entries to thier default values.
 */
protected void performDefaults() {
	super.performDefaults();

	this.longevityText.setText(String.valueOf(defaultFileStateLongevity));
	this.maxStatesText.setText(String.valueOf(defaultMaxFileStates));
	this.maxStateSizeText.setText(String.valueOf(defaultMaxFileStateSize));

	checkState();
}
/** 
 * Perform the result of the OK from the receiver.
 */
public boolean performOk() {

	long longevityValue = validateLongTextEntry(longevityText);
	int maxFileStates = validateMaxFileStates();
	long maxStateSize = validateMaxFileStateSize();
	if (longevityValue == FAILED_VALUE
		|| maxFileStates == FAILED_VALUE
		|| maxStateSize == FAILED_VALUE)
		return false;

	IWorkspaceDescription description = getWorkspaceDescription();
	description.setFileStateLongevity(longevityValue * DAY_LENGTH);
	description.setMaxFileStates(maxFileStates);
	description.setMaxFileStateSize(maxStateSize * MEGABYTES);

	try {
		//As it is only a copy save it back in
		ResourcesPlugin.getWorkspace().setDescription(description);
	} catch (CoreException exception) {
		ErrorDialog.openError(
			getShell(),
			SAVE_ERROR_MESSAGE,
			exception.getMessage(),
			exception.getStatus());
		return false;
	}

	return true;

}
/**
 * Validate a text entry for an integer field. Return the result if there are
 * no errors, otherwise return -1 and set the entry field error. 
 * @return int
 */
private int validateIntegerTextEntry(Text text) {

	int value;
	
	try {
		value = Integer.parseInt(text.getText());
		
	} catch (NumberFormatException exception) {
		setErrorMessage(MessageFormat.format(INVALID_VALUE_MESSAGE, new Object[] {exception.getLocalizedMessage()}));
		return FAILED_VALUE;
	}

	//Be sure all values are non zero and positive
	if(value <= 0){
		setErrorMessage(POSITIVE_MESSAGE);
		return FAILED_VALUE;
	}

	return value;
}
/**
 * Validate a text entry for a long field. Return the result if there are
 * no errors, otherwise return -1 and set the entry field error. 
 * @return long
 */
private long validateLongTextEntry(Text text) {

	long value;
	
	try {
		value = Long.parseLong(text.getText());
		
	} catch (NumberFormatException exception) {
		setErrorMessage(MessageFormat.format(INVALID_VALUE_MESSAGE, new Object[] {exception.getLocalizedMessage()}));
		return FAILED_VALUE;
	}

	//Be sure all values are non zero and positive
	if(value <= 0){
		setErrorMessage(POSITIVE_MESSAGE);
		return FAILED_VALUE;
	}

	return value;
}

/**
 * Validate the maximum file states.
 * Return the value if successful, otherwise
 * return FAILED_VALUE.
 * Set the error message if it fails.
 * @return int
 */
private int validateMaxFileStates(){
	int maxFileStates = validateIntegerTextEntry(this.maxStatesText);
	if(maxFileStates == FAILED_VALUE)
		return maxFileStates;
		
	if(maxFileStates > FILE_STATES_MAXIMUM){
		setErrorMessage(WorkbenchMessages.format(
			"FileHistory.aboveMaxEntries", 
			new String[] {String.valueOf(FILE_STATES_MAXIMUM)}
			));
		return FAILED_VALUE;
	}

	return maxFileStates;
}

/**
 * Validate the maximum file state size.
 * Return the value if successful, otherwise
 * return FAILED_VALUE.
 * Set the error message if it fails.
 * @return long
 */
private long validateMaxFileStateSize(){
	long maxFileStateSize = validateLongTextEntry(this.maxStateSizeText);
	if(maxFileStateSize == FAILED_VALUE)
		return maxFileStateSize;
			
	if(maxFileStateSize > STATE_SIZE_MAXIMUM){
		setErrorMessage(WorkbenchMessages.format(
			"FileHistory.aboveMaxFileSize", 
			new String[] {String.valueOf(STATE_SIZE_MAXIMUM)}
			));
		return FAILED_VALUE;
	}

	return maxFileStateSize;
}

/** 
 * Create a label with a note that informs the user
 * that a restart is required for these changes to 
 * take effect.
 */
private void createNoteLabel(Composite parent){
	
	Composite messageComposite = new Composite(parent, SWT.NONE);
	GridLayout messageLayout = new GridLayout();
	messageLayout.numColumns = 2;
	messageLayout.marginWidth = 0;
	
	messageComposite.setLayout(messageLayout);
	GridData data = 
		new GridData(GridData.HORIZONTAL_ALIGN_FILL);
	data.horizontalSpan = 2;
	messageComposite.setLayoutData(data);
	messageComposite.setFont(parent.getFont());


	final Label noteLabel = new Label(messageComposite,SWT.BOLD);
	noteLabel.setText(NOTE_LABEL);
	noteLabel.setFont(JFaceResources.getBannerFont());
	noteLabel.setLayoutData(
		new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		
	final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent event) {
			if(JFaceResources.BANNER_FONT.equals(event.getProperty())) {
				noteLabel.setFont(JFaceResources.getFont(JFaceResources.BANNER_FONT));
			}
		}
	};
	JFaceResources.getFontRegistry().addListener(fontListener);
	noteLabel.addDisposeListener(new DisposeListener() {
		public void widgetDisposed(DisposeEvent event) {
			JFaceResources.getFontRegistry().removeListener(fontListener);
		}
	});
	
	Label messageLabel = new Label(messageComposite,SWT.WRAP);
	messageLabel.setText(NOTE_MESSAGE);
	messageLabel.setFont(parent.getFont());
}

}