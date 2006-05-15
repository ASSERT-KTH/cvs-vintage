// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar.ui.dialog;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.ui.comp.CalendarPicker;
import org.columba.calendar.ui.comp.DatePicker;
import org.columba.calendar.ui.comp.TimePicker;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.SingleSideEtchedBorder;
import org.columba.core.gui.util.DialogHeaderPanel;
import org.columba.core.help.HelpManager;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;


public class EditEventDialog extends JDialog implements ActionListener {

	JTextField summaryTextField = new JTextField();

	JTextField locationTextField = new JTextField();

	JTextArea descriptionTextArea = new JTextArea(3, 40);

	JComboBox classComboBox = new JComboBox();

	CalendarPicker calendarPicker = new CalendarPicker();

	JTextField categoriesTextField = new JTextField();

	JButton categoriesButton = new JButton();

	JCheckBox allDayCheckBox = new JCheckBox();

	TimePicker startTimePicker = new TimePicker();

	TimePicker endTimePicker = new TimePicker();

	JCheckBox alarmCheckBox = new JCheckBox();

	JComboBox alarmComboBox = new JComboBox();

	JButton alarmButton = new JButton();

	DatePicker startDayDatePicker;

	DatePicker endDayDatePicker;

	boolean success = false;

	private IEvent model;

	public EditEventDialog(JFrame parentFrame, IEvent model) {
		super(parentFrame, true);

		this.model = model;

		startDayDatePicker = new DatePicker();

		endDayDatePicker = new DatePicker();

		alarmComboBox.addItem("15 minutes before");
		alarmComboBox.addItem("1 hour before");
		alarmComboBox.addItem("1 day before");

		classComboBox.addItem("Private");
		classComboBox.addItem("Public");
		classComboBox.addItem("Confidential");

		descriptionTextArea.setEnabled(false);
		categoriesButton.setEnabled(false);
		categoriesTextField.setEnabled(false);
		allDayCheckBox.setEnabled(false);
		classComboBox.setEnabled(false);
		
		categoriesButton.setEnabled(false);
		alarmButton.setEnabled(false);
		alarmCheckBox.setEnabled(false);
		alarmComboBox.setEnabled(false);
		
		setLayout(new BorderLayout());
		getContentPane().add(
				new DialogHeaderPanel("New Appointment",
						"Edit Appointment Properties", ImageLoader
								.getIcon(IconKeys.USER)),
				BorderLayout.NORTH);
		getContentPane().add(createPanel(), BorderLayout.CENTER);
		getContentPane().add(createButtonPanel(), BorderLayout.SOUTH);

		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		updateComponents(true);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

	}

	private JPanel createButtonPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		bottom.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		ButtonWithMnemonic cancelButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "cancel"));

		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		ButtonWithMnemonic okButton = new ButtonWithMnemonic(MailResourceLoader
				.getString("global", "ok"));

		//$NON-NLS-1$ //$NON-NLS-2$
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		getRootPane().setDefaultButton(okButton);

		ButtonWithMnemonic helpButton = new ButtonWithMnemonic(
				MailResourceLoader.getString("global", "help"));

		// associate with JavaHelp
		HelpManager.getInstance().enableHelpOnButton(helpButton,
				"configuring_columba");

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		buttonPanel.setLayout(new GridLayout(1, 3, 6, 0));
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(helpButton);

		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	private JPanel createPanel() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:NONE,FILL:DEFAULT:NONE,FILL:DEFAULT:GROW(1.0),FILL:DEFAULT:NONE",
				"3dlu,CENTER:DEFAULT:NONE,3dlu,FILL:DEFAULT:GROW(1.0),6dlu,CENTER:DEFAULT:NONE,3dlu,CENTER:DEFAULT:NONE,3dlu");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		PanelBuilder b = new PanelBuilder(formlayout1);
		b.setDefaultDialogBorder();

		b.addSeparator("General Information", cc.xywh(2, 2, 2, 1));

		b.add(createPanel1(), cc.xy(3, 4));

		b.addSeparator("Date and Time", cc.xywh(2, 6, 2, 1));

		b.add(createPanel2(), cc.xy(3, 8));

		return b.getPanel();
	}

	private JPanel createPanel1() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:GROW(1.0),3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE",
				"3dlu,CENTER:DEFAULT:NONE,3dlu,CENTER:DEFAULT:NONE,3dlu,CENTER:DEFAULT:NONE,3dlu,CENTER:DEFAULT:NONE,3dlu,FILL:DEFAULT:GROW(1.0)");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Summary:");
		jpanel1.add(jlabel1, new CellConstraints(2, 2, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		JLabel jlabel2 = new JLabel();
		jlabel2.setText("Location:");
		jpanel1.add(jlabel2, new CellConstraints(2, 4, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		JLabel jlabel3 = new JLabel();
		jlabel3.setText("Description:");
		jpanel1.add(jlabel3, new CellConstraints(2, 10, 1, 1,
				CellConstraints.DEFAULT, CellConstraints.TOP));

		JLabel jlabel4 = new JLabel();
		jlabel4.setText("Class:");
		jpanel1.add(jlabel4, new CellConstraints(2, 6, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		jpanel1.add(summaryTextField, cc.xywh(4, 2, 5, 1));

		jpanel1.add(locationTextField, cc.xywh(4, 4, 5, 1));

		JScrollPane jscrollpane1 = new JScrollPane();
		jscrollpane1.setViewportView(descriptionTextArea);
		jscrollpane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		jscrollpane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		jpanel1.add(jscrollpane1, cc.xywh(4, 10, 5, 1));

		jpanel1.add(classComboBox, cc.xy(4, 6));

		JLabel jlabel5 = new JLabel();
		jlabel5.setText("Calendar:");
		jpanel1.add(jlabel5, new CellConstraints(6, 6, CellConstraints.RIGHT,
				CellConstraints.DEFAULT));

		jpanel1.add(calendarPicker, cc.xy(8, 6));

		jpanel1.add(new JLabel("Categories:"), new CellConstraints(2, 8,
				CellConstraints.RIGHT, CellConstraints.DEFAULT));

		jpanel1.add(categoriesTextField, cc.xywh(4, 8, 3, 1));

		categoriesButton.setActionCommand("Categories...");
		categoriesButton.setText("Select ...");
		jpanel1.add(categoriesButton, cc.xy(8, 8));

		return jpanel1;
	}

	private JPanel createPanel2() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:GROW(1.0),3dlu,FILL:DEFAULT:GROW(1.0),3dlu,FILL:DEFAULT:NONE",
				"3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,3dlu,FILL:DEFAULT:NONE,3dlu");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		JLabel jlabel1 = new JLabel();
		jlabel1.setText("Start Time:");
		jpanel1.add(jlabel1, new CellConstraints(2, 2, 1, 1,
				CellConstraints.RIGHT, CellConstraints.DEFAULT));

		allDayCheckBox.setActionCommand("All Day");
		allDayCheckBox.setText("All Day");
		jpanel1.add(allDayCheckBox, cc.xy(8, 2));

		JLabel jlabel2 = new JLabel();
		jlabel2.setText("End Time:");
		jpanel1.add(jlabel2, new CellConstraints(2, 4, 1, 1,
				CellConstraints.RIGHT, CellConstraints.DEFAULT));

		jpanel1.add(startDayDatePicker, cc.xy(4, 2));

		jpanel1.add(endDayDatePicker, cc.xy(4, 4));

		jpanel1.add(startTimePicker, cc.xy(6, 2));

		jpanel1.add(endTimePicker, cc.xy(6, 4));

		alarmCheckBox.setActionCommand("Alarm");
		alarmCheckBox.setText("Alarm");
		jpanel1.add(alarmCheckBox, new CellConstraints(2, 6, 1, 1,
				CellConstraints.RIGHT, CellConstraints.DEFAULT));

		jpanel1.add(createPanel3(), cc.xywh(4, 6, 3, 1));

		return jpanel1;
	}

	private JPanel createPanel3() {
		JPanel jpanel1 = new JPanel();
		FormLayout formlayout1 = new FormLayout(
				"FILL:DEFAULT:GROW(1.0),3dlu,FILL:DEFAULT:NONE",
				"CENTER:DEFAULT:NONE");
		CellConstraints cc = new CellConstraints();
		jpanel1.setLayout(formlayout1);

		jpanel1.add(alarmComboBox, cc.xy(1, 1));

		alarmButton.setActionCommand("Customize...");
		alarmButton.setText("Customize...");
		jpanel1.add(alarmButton, cc.xy(3, 1));

		return jpanel1;
	}

	public boolean success() {
		return success;
	}

	public void updateComponents(boolean b) {
		if (b) {
			summaryTextField.setText(model.getSummary());
			locationTextField.setText(model.getLocation());
			categoriesTextField.setText(model.getCategories());
			descriptionTextArea.setText(model.getDescription());

			Calendar start = model.getDtStart();
			startDayDatePicker.setDate(start);
			startTimePicker.setTime(start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE));
			Calendar end = model.getDtEnt();
			endDayDatePicker.setDate(end);
			endTimePicker.setTime(end.get(Calendar.HOUR_OF_DAY), end.get(Calendar.MINUTE));
			
			String calendar = model.getCalendar();
			if ( calendar != null )
				calendarPicker.setSelectedItem(model.getCalendar());
			else
				calendarPicker.setSelectedIndex(0);
			
		} else {
			model.setSummary(summaryTextField.getText());
			model.setLocation(locationTextField.getText());
			model.setCategories(categoriesTextField.getText());
			model.setDescription(descriptionTextArea.getText());

			Calendar start = startDayDatePicker.getDate();
			int hour = startTimePicker.getHour();
			int minutes = startTimePicker.getMinutes();
			start.set(Calendar.HOUR_OF_DAY, hour);
			start.set(Calendar.MINUTE, minutes);
			model.setDtStart(start);
			
			Calendar end = endDayDatePicker.getDate();
			hour = endTimePicker.getHour();
			minutes = endTimePicker.getMinutes();
			end.set(Calendar.HOUR_OF_DAY, hour);
			end.set(Calendar.MINUTE, minutes);
			model.setDtEnt(end);

			
			String calendar = (String) calendarPicker.getSelectedItem();
			model.setCalendar(calendar);
			
			// update modification timestamp
			model.setDtStamp(Calendar.getInstance());
		}
	}

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("OK")) //$NON-NLS-1$
		{
			// check if the user entered valid data
			success = true;

			updateComponents(false);

			setVisible(false);
		} else if (action.equals("CANCEL")) {
			success = false;
			setVisible(false);
		}
	}

	/**
	 * @return Returns the model.
	 */
	public IEvent getModel() {
		return model;
	}

}
