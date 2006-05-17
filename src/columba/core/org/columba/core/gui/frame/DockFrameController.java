package org.columba.core.gui.frame;

import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IDockable;
import org.columba.api.gui.frame.IDock.REGION;
import org.columba.core.config.Config;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.docking.XMLPersister;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingConstants;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingPort;
import org.flexdock.docking.defaults.DefaultDockingPort;
import org.flexdock.docking.state.PersistenceException;

public abstract class DockFrameController extends DefaultFrameController
		implements IDock {

	private DefaultDockingPort dockingPort = new DefaultDockingPort();

	private ArrayList<IDockable> list = new ArrayList<IDockable>();

	/**
	 * @param viewItem
	 */
	public DockFrameController(ViewItem viewItem) {
		super(viewItem);

		initComponents();

	}

	/**
	 * @param id
	 */
	public DockFrameController(String id) {
		super(id);

		initComponents();

	}

	/**
	 * 
	 */
	private void initComponents() {

		dockingPort = new DefaultDockingPort();
		dockingPort.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	}

	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IFrameMediator#getContentPane()
	 */
	public JPanel getContentPane() {
		return dockingPort;
	}

	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IFrameMediator#loadPositions()
	 */
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
				restoreSuccess = true;
			}

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

	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IFrameMediator#savePositions()
	 */
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

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#isInitialized()
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IFrameMediator#isInitialized()
	 */
	public boolean isInitialized() {
		return false;
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#registerDockable(org.columba.api.gui.frame.IDockable)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#registerDockable(org.columba.api.gui.frame.IDockable)
	 */
	public void registerDockable(IDockable dockable) {

		list.add(dockable);

		dockingPort.dock(dockable.getView(), dockable.getId());
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#registerDockable(java.lang.String,
	 *      java.lang.String, javax.swing.JComponent, javax.swing.JPopupMenu)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#registerDockable(java.lang.String, java.lang.String, javax.swing.JComponent, javax.swing.JPopupMenu)
	 */
	public IDockable registerDockable(String id, String name, JComponent comp,
			JPopupMenu popup) {
		IDockable dockable = new FrameMediatorDockable(id, name, comp, popup);

		registerDockable(dockable);

		return dockable;
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#getDockableIterator()
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#getDockableIterator()
	 */
	public Iterator<IDockable> getDockableIterator() {
		return list.iterator();
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable,
	 *      org.columba.api.gui.frame.IDock.REGION, float)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable, org.columba.api.gui.frame.IDock.REGION, float)
	 */
	public void dock(IDockable dockable, REGION region, float percentage) {
		String regionString = convertRegion(region);

		DockingManager.dock(dockable.getView(), dockingPort, regionString,
				percentage);

	}

	/**
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable,
	 *      org.columba.api.gui.frame.IDock.REGION)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable, org.columba.api.gui.frame.IDock.REGION)
	 */
	public void dock(IDockable dockable, REGION region) {
		String regionString = convertRegion(region);

		DockingManager.dock((Component) dockable.getView(),
				(DockingPort) dockingPort, regionString);
	}

	/**
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable,
	 *      org.columba.api.gui.frame.IDockable,
	 *      org.columba.api.gui.frame.IDock.REGION, float)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#dock(org.columba.api.gui.frame.IDockable, org.columba.api.gui.frame.IDockable, org.columba.api.gui.frame.IDock.REGION, float)
	 */
	public void dock(IDockable dockable, IDockable parentDockable,
			REGION region, float percentage) {
		String regionString = convertRegion(region);

		DockingManager.dock(dockable.getView(), parentDockable.getView(),
				regionString, percentage);

	}

	/**
	 * @see org.columba.api.gui.frame.IDock#setSplitProportion(org.columba.api.gui.frame.IDockable,
	 *      float)
	 */
	/* (non-Javadoc)
	 * @see org.columba.api.gui.frame.IDock#setSplitProportion(org.columba.api.gui.frame.IDockable, float)
	 */
	public void setSplitProportion(IDockable dockable, float percentage) {
		DockingManager.setSplitProportion(dockable.getView(), percentage);
	}

	// convert region enum to flexdock string
	/**
	 * @param region
	 * @return
	 */
	private String convertRegion(REGION region) {
		String regionString = null;
		if (region == REGION.CENTER)
			regionString = DockingConstants.CENTER_REGION;
		else if (region == REGION.WEST)
			regionString = DockingConstants.WEST_REGION;
		else if (region == REGION.EAST)
			regionString = DockingConstants.EAST_REGION;
		else if (region == REGION.NORTH)
			regionString = DockingConstants.NORTH_REGION;
		else if (region == REGION.SOUTH)
			regionString = DockingConstants.SOUTH_REGION;
		else
			throw new IllegalArgumentException("unsupported region: "
					+ region.toString());
		return regionString;
	}

	/**
	 * Overwrite to specify default docking settings.
	 * <p>
	 * Make use of the <code>dock(..)</code> and
	 * <code>setSplitPropertion(..)</code> methods.
	 * <p>
	 * This method is called in case a formerly persisted state could not be
	 * loaded correctly or no persisted state is available yet.
	 */
	/**
	 * 
	 */
	public abstract void loadDefaultPosition();
}
