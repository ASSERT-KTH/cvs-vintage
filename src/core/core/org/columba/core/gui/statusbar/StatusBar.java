// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.statusbar;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.columba.core.command.TaskManager;
import org.columba.core.command.Worker;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.statusbar.event.WorkerStatusChangeListener;
import org.columba.core.gui.statusbar.event.WorkerStatusChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.action.BasicAction;
import org.columba.mail.util.MailResourceLoader;

public class StatusBar
	extends JComponent
	implements WorkerListChangeListener, ActionListener, WorkerStatusChangeListener {

	private JLabel label;
	private JProgressBar progressBar;

	//private JButton left, right;

	private Border border;
	private JPanel leftPanel;
	private JPanel rightPanel;
	private JPanel mainRightPanel;
	private JButton taskButton;
	private JPanel leftMainPanel;
	//private JButton progressButton;
	private JPanel middleRightPanel;
	private JPanel middleLeftPanel;
	private JPanel mainMiddlePanel;
	private JPanel rightRightPanel;
	//private JButton onlineButton;

	private int displayedWorkerIndex;
	private int workerListSize;
	private Worker displayedWorker;

	private TaskManager taskManager;

    private BasicAction cancelAction;
    private ImageSequenceTimer imageSequenceTimer;

	public StatusBar(TaskManager tm) 
	{
		taskManager = tm;
		tm.addWorkerListChangeListener(this);

		imageSequenceTimer = new ImageSequenceTimer();
		
		displayedWorkerIndex = 0;
		workerListSize = 0;
		label = new JLabel("");
		label.setAlignmentX(Component.LEFT_ALIGNMENT);

		/*
		progressButton = new JButton("");
		progressButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		progressButton.setRolloverEnabled(true);
		progressButton.setEnabled(false);
		progressButton.setToolTipText("Auto Mail Checking Status Notification");
		*/
		
		/*
		onlineButton = new JButton( ImageLoader.getSmallImageIcon("remotehost.png") );
		onlineButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		onlineButton.setRolloverEnabled(true);
		onlineButton.setEnabled(false);
		onlineButton.setToolTipText("Currently in ONLINE state");
		*/
		
		/*
		left = new JButton("<");
		left.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		left.setRolloverEnabled(true);
		left.setActionCommand("LEFT");
		left.setEnabled(false);

		right = new JButton(">");
		right.setEnabled(false);
		right.setRolloverEnabled(true);
		right.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		right.setActionCommand("RIGHT");
		*/
		progressBar = new JProgressBar(0, 100);
		progressBar.setAlignmentX(Component.RIGHT_ALIGNMENT);
		progressBar.setAlignmentY(Component.CENTER_ALIGNMENT);
		progressBar.setStringPainted(false);
		progressBar.setBorderPainted(false);
		progressBar.setValue(0);
		
		taskButton = new JButton("Tasks: 0");
		taskButton.setRolloverEnabled(true);
		taskButton.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		setLayout(new BorderLayout());

		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		
		leftMainPanel = new JPanel();
		leftMainPanel.setLayout(new BorderLayout());
		
		
		leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());
		leftMainPanel.add( leftPanel, BorderLayout.WEST );
		
		
		JPanel taskPanel = new JPanel();
		taskPanel.setLayout( new BorderLayout() );
		taskPanel.setBorder(BorderFactory.createEtchedBorder());
		
		Border b = taskPanel.getBorder();
		Border margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		/*
		Border b = taskPanel.getBorder();
		Border margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		taskPanel.setBorder( margin );
		*/
		
		taskPanel.add( taskButton, BorderLayout.CENTER );
		//leftPanel.setBorder(BorderFactory.createEtchedBorder());
		
		//leftPanel.add(taskPanel, BorderLayout.WEST);
		
		/*
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new BorderLayout() );
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		buttonPanel.add( left, BorderLayout.CENTER );
		buttonPanel.add( right, BorderLayout.EAST );
		Border b = buttonPanel.getBorder();
		Border margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		buttonPanel.setBorder( margin );

		leftPanel.add( buttonPanel, BorderLayout.CENTER );
		*/
		
		leftMainPanel.add(taskPanel, BorderLayout.WEST);

		JPanel labelPanel = new JPanel();
		labelPanel.setLayout( new BorderLayout() );
		labelPanel.setBorder(BorderFactory.createEtchedBorder());
		b = labelPanel.getBorder();
		margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		labelPanel.setBorder( margin );
		labelPanel.add( label, BorderLayout.CENTER );
		
		leftMainPanel.add( labelPanel, BorderLayout.CENTER );
		
		add( leftMainPanel, BorderLayout.CENTER );
		
		//rightPanel.setBorder(BorderFactory.createEtchedBorder());

		/*
		BoxLayout boxLayout = new BoxLayout(rightPanel, BoxLayout.X_AXIS);
		rightPanel.setLayout(boxLayout);

		rightPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		rightPanel.add(label);

		rightPanel.add(Box.createRigidArea(new Dimension(10, 0)));

		leftMainPanel.add(rightPanel, BorderLayout.CENTER);

		add(leftMainPanel, BorderLayout.CENTER);
		*/
		
		mainRightPanel = new JPanel();
		mainRightPanel.setLayout( new BorderLayout() );
		
		/*
		rightPanel = new JPanel();
		rightPanel.setLayout( new BorderLayout() );
		rightPanel.setBorder(BorderFactory.createEtchedBorder());
		rightPanel.add( progressButton, BorderLayout.CENTER );
		*/
		//rightPanel.add( onlineButton, BorderLayout.WEST );
		
		/*
		b = rightPanel.getBorder();
		margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		rightPanel.setBorder( margin );
		
		mainRightPanel.add( rightPanel, BorderLayout.CENTER );
		*/
		
		JPanel progressPanel = new JPanel();
		progressPanel.setLayout( new BorderLayout() );
		progressPanel.setBorder(BorderFactory.createEtchedBorder());
		b = progressPanel.getBorder();
		margin = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0,2,0,0 ),b);
		progressPanel.setBorder( margin );
		progressPanel.add( progressBar, BorderLayout.CENTER );
		
		//mainRightPanel.add( progressPanel, BorderLayout.WEST );
		
		add( progressPanel, BorderLayout.EAST );
		
		//mainRightPanel.setLayout(new BoxLayout(mainRightPanel, BoxLayout.X_AXIS));

		/*
		middleLeftPanel = new JPanel();

		middleLeftPanel.setLayout(new BorderLayout());
		middleLeftPanel.setBorder(BorderFactory.createEtchedBorder());

		middleLeftPanel.add(progressButton, BorderLayout.CENTER);

		mainMiddlePanel = new JPanel();
		BoxLayout boxLayout5 = new BoxLayout(mainMiddlePanel, BoxLayout.X_AXIS);
		mainMiddlePanel.setLayout(boxLayout5);

		mainMiddlePanel.add(middleLeftPanel);

		middleRightPanel = new JPanel();

		middleRightPanel.setLayout(new BorderLayout());

		middleRightPanel.setBorder(BorderFactory.createEtchedBorder());

		middleRightPanel.add(left, BorderLayout.WEST);
		middleRightPanel.add(
			Box.createRigidArea(new Dimension(2, 0)),
			BorderLayout.CENTER);
		middleRightPanel.add(right, BorderLayout.EAST);

		mainMiddlePanel.add(middleRightPanel);

		mainRightPanel.add(mainMiddlePanel);

		rightRightPanel = new JPanel();

		rightRightPanel.setLayout(new BorderLayout());
		rightRightPanel.setBorder(BorderFactory.createEtchedBorder());

		rightRightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		rightRightPanel.add(progressBar, BorderLayout.CENTER);

		mainRightPanel.add(rightRightPanel);

		add(mainRightPanel, BorderLayout.EAST);
		*/

		initActions();

	}

	public void displayTooltipMessage( String message ) {
		
	}

	/*
	public void enableMailCheck(boolean b)
	{
		if (b == true)
		{
			progressButton.setIcon(ImageLoader.getSmallImageIcon("send-receive.png"));
			progressButton.setEnabled(true);
		}
		else
		{
			progressButton.setIcon(ImageLoader.getSmallImageIcon("send-receive.png"));
			progressButton.setEnabled(false);
		}
	}
	*/

	protected void setTaskCount(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				taskButton.setText("Tasks: " + n);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setText(String s) {
		final String str = s;

		Runnable run = new Runnable() {
			public void run() {
				label.setText(str);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setMaximum(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(0);
				progressBar.setMaximum(n);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setMaximumAndValue(int v, int m) {
		final int max = m;
		final int val = v;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(val);
				progressBar.setMaximum(max);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}

	protected void setValue(int i) {
		final int n = i;

		Runnable run = new Runnable() {
			public void run() {
				progressBar.setValue(n);
			}
		};
		try {
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(run);
			else
				SwingUtilities.invokeLater(run);

		} catch (Exception ex) {
		}

	}


	protected void displayWorker(int index) {
		// set to default state
		setMaximumAndValue(100,100);
		setText("");		
		
		// now switch to worker
		Worker w = taskManager.get(index);

		setText(w.getDisplayText());
		setMaximumAndValue(w.getProgressBarValue(), w.getProgessBarMaximum());
		
		if( displayedWorker != null ) {
			displayedWorker.removeWorkerStatusChangeListener( this );
		}
		
		w.addWorkerStatusChangeListener(this);
		displayedWorker = w;
	}

	public void workerListChanged(WorkerListChangedEvent e) {

		if (e.getType() == WorkerListChangedEvent.SIZE_CHANGED) {
			workerListSize = e.getNewValue();

			setTaskCount(workerListSize);
			if (displayedWorkerIndex > workerListSize - 1)
				displayedWorkerIndex = workerListSize - 1;
			if( displayedWorkerIndex < 0 )
				displayedWorkerIndex = 0;
			
			if( (workerListSize > 0) && (e.getOldValue() == 0) ) {
				displayWorker(0);	
			}
			
			/*
			if( (workerListSize > 1) && (e.getOldValue() <= 1) ) {
				right.setEnabled( true );
			}
			*/
			
			if( workerListSize > 0 )
			{
				cancelAction.setEnabled(true);
				imageSequenceTimer.start();
			}
			else
			{
				cancelAction.setEnabled(false);			
				imageSequenceTimer.stop();
			}
		}

	}

	public void workerStatusChanged(WorkerStatusChangedEvent e) {
		switch (e.getType()) {

			case WorkerStatusChangedEvent.DISPLAY_TEXT_CHANGED :
				setText((String) e.getNewValue());
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_MAX_CHANGED :
				setMaximum(((Integer) e.getNewValue()).intValue());
				break;

			case WorkerStatusChangedEvent.PROGRESSBAR_VALUE_CHANGED :
				setValue(((Integer) e.getNewValue()).intValue());
				break;

			case WorkerStatusChangedEvent.FINISHED :
				if (workerListSize > 0)
					displayWorker(0);

		}
	}

	protected void initActions() {
		
        cancelAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_file_cancel"),
            MailResourceLoader.getString("menu","mainframe","menu_file_cancel"),
            "CANCEL_ACTION",
            ImageLoader.getSmallImageIcon("stock_stop-16.png"),
            ImageLoader.getImageIcon("stock_stop.png"),
            '0',
            null,false
            );

        cancelAction.setEnabled( false );
        cancelAction.addActionListener(this);
	}
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		/*
		if (command.equals(left.getActionCommand())) {
			displayedWorkerIndex--;
			if (displayedWorkerIndex == 0) {
				left.setEnabled(false);
			}
			if( displayedWorkerIndex < workerListSize-1 ) {
				right.setEnabled( true );
			}

			displayWorker(displayedWorkerIndex);

		} else if (command.equals(right.getActionCommand())) {
			displayedWorkerIndex++;
			if (displayedWorkerIndex == workerListSize - 1) {
				right.setEnabled(false);
			}
			if( displayedWorkerIndex > 0 ) {
				left.setEnabled( true );
			}
			displayWorker(displayedWorkerIndex);
		} else 
		*/
		if ( command.equals("CANCEL_ACTION") ) {
			displayedWorker.cancel();
		}
		
		

	}
	/**
	 * Returns the cancelAction.
	 * @return BasicAction
	 */
	public BasicAction getCancelAction() {
		return cancelAction;
	}

	/**
	 * Returns the imageSequenceTimer.
	 * @return ImageSequenceTimer
	 */
	public ImageSequenceTimer getImageSequenceTimer() {
		return imageSequenceTimer;
	}

}