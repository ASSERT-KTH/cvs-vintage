//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.gui.statusbar;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.columba.core.config.Config;
import org.columba.core.config.ConfigPath;
import org.columba.core.config.ThemeItem;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.gui.util.ToolbarButton;
import org.columba.core.main.MainInterface;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class ImageSequenceTimer
	extends ToolbarButton
	implements ActionListener, WorkerListChangeListener {
	private javax.swing.Timer timer;
	private ImageIcon[] images;
	private ImageIcon restImage;
	private int frameNumber;
	private int frameCount;

	private Dimension scale;

	private static int DELAY = 100;

	private int imageWidth;
	private int imageHeight;

	public ImageSequenceTimer() {
		super();

		timer = new javax.swing.Timer(DELAY, this);
		timer.setInitialDelay(0);
		timer.setCoalesce(true);
		setMargin(new Insets(0, 0, 0, 0));
		setRolloverEnabled(true);
		setBorder(null);
		setContentAreaFilled(false);

		setRequestFocusEnabled(false);
		init();

		MainInterface.processor.getTaskManager().addWorkerListChangeListener(
			this);

	}

	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

	protected void initDefault() {
		frameCount = 60;
		frameNumber = 1;

		imageWidth = 36;
		imageHeight = 36;

		//setPreferredSize(new Dimension(imageWidth, imageHeight));
		/*
		setMinimumSize(new Dimension(width, height));
		*/
		//setMaximumSize(new Dimension(imageWidth, imageHeight));

		images = new ImageIcon[frameCount];

		for (int i = 0; i < frameCount; i++) {
			StringBuffer buf = new StringBuffer();

			if (i < 10)
				buf.append("00");
			if ((i >= 10) && (i < 100))
				buf.append("0");

			buf.append(Integer.toString(i));

			buf.append(".png");

			images[i] = ImageLoader.getImageIcon(buf.toString());
		}

		restImage = ImageLoader.getImageIcon("rest.png");

		setIcon(restImage);

	}

	protected void init() {
		ThemeItem item = Config.getOptionsConfig().getThemeItem();
		//String pulsator = item.getPulsator();
		String pulsator = "default";

		if (pulsator.toLowerCase().equals("default"))
			initDefault();
		else {
			try {
				File zipFile =
					new File(
						ConfigPath.getConfigDirectory()
							+ "/pulsators/"
							+ pulsator
							+ ".jar");

				String zipFileEntry =
					new String(pulsator + "/pulsator.properties");
				//System.out.println("zipfileentry:"+zipFileEntry );

				Properties properties =
					ImageLoader.loadProperties(zipFile, zipFileEntry);

				String frameCountStr = (String) properties.getProperty("count");
				frameCount = Integer.parseInt(frameCountStr);

				String widthStr = (String) properties.getProperty("width");
				imageWidth = Integer.parseInt(widthStr);

				String heightStr = (String) properties.getProperty("height");
				imageHeight = Integer.parseInt(heightStr);

				/*
				setPreferredSize(new Dimension(width, height));
				setMinimumSize(new Dimension(width, height));
				setMaximumSize(new Dimension(width, height));
				*/

				images = new ImageIcon[frameCount];
				for (int i = 0; i < frameCount; i++) {
					String istr = (new Integer(i)).toString();
					String image = (String) properties.getProperty(istr);

					zipFile =
						new File(
							ConfigPath.getConfigDirectory()
								+ "/pulsators/"
								+ pulsator
								+ ".jar");

					zipFileEntry = new String(pulsator + "/" + image);

					//System.out.println("zuifileentry:"+zipFileEntry);
					images[i] =
						new ImageIcon(
							ImageLoader.loadImage(zipFile, zipFileEntry));
				}

				String image = (String) properties.getProperty("rest");
				zipFileEntry = new String(pulsator + "/" + image);

				restImage =
					new ImageIcon(ImageLoader.loadImage(zipFile, zipFileEntry));
			} catch (Exception ex) {
				StringBuffer buf = new StringBuffer();
				buf.append("Error while loading pulsator icons!");
				JOptionPane.showMessageDialog(null, buf.toString());

				//Config.getOptionsConfig().getThemeItem().setPulsator("default");

				initDefault();
			}
		}

	}

	public void start() {
		if (!timer.isRunning())
			timer.start();
	}

	public void stop() {
		if (timer.isRunning())
			timer.stop();

		frameNumber = 0;

		setIcon(restImage);
	}

	public void actionPerformed(ActionEvent ev) {
		String action = ev.getActionCommand();

		frameNumber++;

		if (timer.isRunning())
			setIcon(new ImageIcon(images[frameNumber % frameCount].getImage()));
		else
			setIcon(restImage);

	}

	/* (non-Javadoc)
		 * @see org.columba.core.gui.statusbar.event.WorkerListChangeListener#workerListChanged(org.columba.core.gui.statusbar.event.WorkerListChangedEvent)
		 */
	public void workerListChanged(WorkerListChangedEvent e) {
		if (e.getNewValue() != 0)
			start();
		else
			stop();
	}

}