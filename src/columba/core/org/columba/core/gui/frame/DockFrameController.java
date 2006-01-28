package org.columba.core.gui.frame;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.columba.core.config.Config;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.XMLPersister;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.defaults.StandardBorderManager;
import org.flexdock.docking.state.PersistenceException;
import org.flexdock.perspective.Perspective;
import org.flexdock.plaf.common.border.ShadowBorder;

public abstract class DockFrameController extends DefaultFrameController
		//implements IDock
		{

	private DefaultDockingPort dockingPort = new DefaultDockingPort();

	public DockFrameController(ViewItem viewItem) {
		super(viewItem);

		initComponents();
		
		
		
		
	}

	

	public DockFrameController(String id) {
		super(id);

		initComponents();
		
		
	}

	private void initComponents() {
//		contentPanePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
//
//		contentPanePanel.setLayout(new BorderLayout());

		
		dockingPort = new DefaultDockingPort();
		dockingPort.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		
		//contentPanePanel.add(dockingPort, BorderLayout.CENTER);
		
		//perspective = new Perspective(getId(), getId()+" Perspective");
		
		//PerspectiveManager.getInstance().add(perspective);
		//initPerspective(perspective);

	}

	
	
	public JPanel getContentPane() {
		System.out.println();
		System.out.println("----->> comp count="
				+ dockingPort.getDockables().size());
		System.out.println();

		if (dockingPort.getComponentCount() == 0)
			loadDefaultPosition();

		
		
		return dockingPort;
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#dock(java.awt.Component,
	 *      java.lang.String)
	 */
	public void dock(Dockable component, String str) {
		dockingPort.dock(component, str);
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#setSplitProportion(java.awt.Component,
	 *      float)
	 */
	public void setSplitProportion(Dockable component, float propertion) {
		DockingManager.setSplitProportion(component, propertion);
		
		System.out.println("------> setSplitPropertion()");
	}

	/**
	 * Overwrite to specify default docking settings
	 */
	public abstract void loadDefaultPosition();

	public void loadPositions() {
		super.loadPositions();

		boolean restoreSuccess = false;
		// load docking settings from last user session
		File configDirectory = Config.getInstance().getConfigDirectory();
		File dockDirectory = new File(configDirectory, "flexdock");
		if (!dockDirectory.exists())
			dockDirectory.mkdir();
		String filename = getId() + ".xml";

		try {
			File file = new File(dockDirectory, filename);
			if (file.exists()) {
				new XMLPersister().load(new FileInputStream(file), dockingPort);
			}
			restoreSuccess = true;
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (PersistenceException e1) {
			e1.printStackTrace();
		}

		if (!restoreSuccess) {
			// make sure there is nothing within the root dockingport
			dockingPort.clear();

			loadDefaultPosition();
		}
	}

	public void savePositions() {
		super.savePositions();

		try {
			File configDirectory = Config.getInstance().getConfigDirectory();
			File dockDirectory = new File(configDirectory, "flexdock");
			if (!dockDirectory.exists())
				dockDirectory.mkdir();
			String filename = getId() + ".xml";
			File file = new File(dockDirectory, filename);
			
			new XMLPersister().store(new FileOutputStream(file), dockingPort);
		

		} catch (IOException e) {
			e.printStackTrace();
		} catch (PersistenceException e) {
			e.printStackTrace();
		}

	}

	public void initPerspective(Perspective p) {
		
	}

	
}
