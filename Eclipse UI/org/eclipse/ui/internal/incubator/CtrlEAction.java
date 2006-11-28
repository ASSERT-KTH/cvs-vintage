package org.eclipse.ui.internal.incubator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.DeviceResourceException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.progress.ProgressManagerUtil;

/**
 * Experimental Action for search-based navigation to UI elements such as
 * editors, views, commands.
 * 
 */
public class CtrlEAction extends AbstractHandler {

	private IWorkbenchWindow window;

	protected String rememberedText;

	protected Map textMap = new HashMap();

	protected Map elementMap = new HashMap();

	private LinkedList previousPicksList = new LinkedList();

	protected AbstractProvider[] providers;
	protected Map providerMap;

	/**
	 * The constructor.
	 */
	public CtrlEAction() {
	}

	public Object execute(ExecutionEvent executionEvent) {

		window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}

		if (providers == null) {
			providers = new AbstractProvider[] { new PreviousPicksProvider(),
					new EditorProvider(), new ViewProvider(),
					new PerspectiveProvider(), new CommandProvider(),
					new ActionProvider(), new WizardProvider(),
					new PreferenceProvider() };

			providerMap = new HashMap();
			for (int i = 0; i < providers.length; i++) {
				providerMap.put(providers[i].getId(), providers[i]);
			}
		}

		FilteringInfoPopup popup = new QuickAccessPopup(ProgressManagerUtil
				.getDefaultParent(), providers);
		popup.setInput(new Object());
		TreeItem[] rootItems = ((Tree) popup.getTreeViewer().getControl())
				.getItems();
		if (rootItems.length > 0)
			((Tree) popup.getTreeViewer().getControl())
					.setTopItem(rootItems[0]);
		popup.open();
		return null;
	}

	private final static class QuickAccessTreeSorter extends
			TreePathViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			AbstractElement element1 = (AbstractElement) e1;
			AbstractElement element2 = (AbstractElement) e2;
			String name1 = element1.getSortLabel();
			String name2 = element2.getSortLabel();
			return getComparator().compare(name1, name2);
		}

		public void sort(Viewer viewer, TreePath parentPath, Object[] elements) {
			if (parentPath == null) {
				return;
			}
			Object parent = parentPath.getLastSegment();
			if (parent instanceof AbstractProvider) {
				AbstractProvider provider = (AbstractProvider) parent;
				if (provider instanceof PreviousPicksProvider) {
					return;
				}
			}
			super.sort(viewer, parentPath, elements);
		}
	}

	/**
	 * @since 3.2
	 * 
	 */
	private final class QuickAccessPopup extends FilteringInfoPopup {

		private static final String TEXT_ARRAY = "textArray"; //$NON-NLS-1$
		private static final String TEXT_ENTRIES = "textEntries"; //$NON-NLS-1$
		private static final String ORDERED_PROVIDERS = "orderedProviders"; //$NON-NLS-1$
		private static final String ORDERED_ELEMENTS = "orderedElements"; //$NON-NLS-1$

		AbstractProvider[] providers;

		/**
		 * @param shell
		 * @param providers
		 */
		public QuickAccessPopup(Shell shell, AbstractProvider[] providers) {
			super(shell, SWT.RESIZE, SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE,
					false);
			this.providers = providers;
			restoreDialog();
			getTreeViewer()
					.setContentProvider(new MyContentProvider(providers));
		}

		protected TreeViewer createTreeViewer(Composite parent, int style) {
			TreeViewer viewer = new TreeViewer(parent, style);
			viewer.setLabelProvider(new MyLabelProvider());
			viewer.setComparator(new QuickAccessTreeSorter());
			return viewer;
		}

		protected boolean isMatchable(Object element) {
			return element instanceof AbstractElement;
		}

		protected void selectFirstMatch() {
			String text = getFilterText().getText();
			Object element = elementMap.get(text);
			if (element != null) {
				getTreeViewer().setSelection(new StructuredSelection(element),
						true);
				return;
			}
			super.selectFirstMatch();
		}

		protected String getMatchName(Object element) {
			if (element instanceof AbstractProvider) {
				AbstractProvider provider = (AbstractProvider) element;
				return provider.getName();
			} else if (element instanceof AbstractElement) {
				AbstractElement abstractElement = (AbstractElement) element;
				return abstractElement.getSortLabel();
			}
			return ""; //$NON-NLS-1$
		}

		protected Point getInitialSize() {
			if (!QuickAccessPopup.this.getPersistBounds()) {
				return new Point(300, 400);
			}
			return super.getInitialSize();
		}

		protected Point getInitialLocation(Point initialSize) {
			if (!QuickAccessPopup.this.getPersistBounds()) {
				Point size = new Point(300, 400);
				Rectangle parentBounds = QuickAccessPopup.this.getParentShell()
						.getBounds();
				int x = parentBounds.x + parentBounds.width / 2 - size.x / 2;
				int y = parentBounds.y + parentBounds.height / 2 - size.y / 2;
				return new Point(x, y);
			}
			return super.getInitialLocation(initialSize);
		}

		protected IDialogSettings getDialogSettings() {
			String sectionName = getId();
			IDialogSettings settings = WorkbenchPlugin.getDefault()
					.getDialogSettings();
			if (settings == null) {
				settings = WorkbenchPlugin.getDefault().getDialogSettings()
						.addNewSection(sectionName);
			}
			return settings;
		}

		protected String getId() {
			return "org.eclipse.ui.internal.incubator.ctrlE"; //$NON-NLS-1$
		}

		public boolean close() {
			rememberedText = getFilterText().getText();
			return super.close();
		}

		private void storeDialog(IDialogSettings dialogSettings) {
			String[] orderedElements = new String[previousPicksList.size()];
			String[] orderedProviders = new String[previousPicksList.size()];
			String[] textEntries = new String[previousPicksList.size()];
			ArrayList arrayList = new ArrayList();
			for (int i = 0; i < orderedElements.length; i++) {
				AbstractElement abstractElement = (AbstractElement) previousPicksList
						.get(i);
				ArrayList elementText = (ArrayList) textMap
						.get(abstractElement);
				Assert.isNotNull(elementText);
				orderedElements[i] = abstractElement.getId();
				orderedProviders[i] = abstractElement.getProvider().getId();
				arrayList.addAll(elementText);
				textEntries[i] = elementText.size() + ""; //$NON-NLS-1$
			}
			String[] textArray = (String[]) arrayList
					.toArray(new String[arrayList.size()]);
			dialogSettings.put(ORDERED_ELEMENTS, orderedElements);
			dialogSettings.put(ORDERED_PROVIDERS, orderedProviders);
			dialogSettings.put(TEXT_ENTRIES, textEntries);
			dialogSettings.put(TEXT_ARRAY, textArray);
		}

		protected void handleElementSelected(Object selectedElement) {
			IWorkbenchPage activePage = window.getActivePage();
			if (activePage != null) {
				if (selectedElement instanceof AbstractElement) {
					addPreviousPick(selectedElement);
					storeDialog(getDialogSettings());
					AbstractElement element = (AbstractElement) selectedElement;
					element.execute();
				}
			}
		}

		private void restoreDialog() {
			IDialogSettings dialogSettings = getDialogSettings();
			if (dialogSettings != null) {
				String[] orderedElements = dialogSettings
						.getArray(ORDERED_ELEMENTS);
				String[] orderedProviders = dialogSettings
						.getArray(ORDERED_PROVIDERS);
				String[] textEntries = dialogSettings.getArray(TEXT_ENTRIES);
				String[] textArray = dialogSettings.getArray(TEXT_ARRAY);
				elementMap = new HashMap();
				textMap = new HashMap();
				previousPicksList = new LinkedList();
				if (orderedElements != null && orderedProviders != null
						&& textEntries != null && textArray != null) {
					int arrayIndex = 0;
					for (int i = 0; i < orderedElements.length; i++) {
						AbstractProvider abstractProvider = (AbstractProvider) providerMap
								.get(orderedProviders[i]);
						int numTexts = Integer.parseInt(textEntries[i]);
						if (abstractProvider != null) {
							AbstractElement abstractElement = abstractProvider
									.getElementForId(orderedElements[i]);
							if (abstractElement != null) {
								ArrayList arrayList = new ArrayList();
								for (int j = arrayIndex; j < arrayIndex
										+ numTexts; j++) {
									arrayList.add(textArray[j]);
									elementMap.put(textArray[j],
											abstractElement);
								}
								textMap.put(abstractElement, arrayList);
								previousPicksList.add(abstractElement);
							}
						}
						arrayIndex += numTexts;
					}
				}
			}
		}

		public void setInput(Object information) {
			getTreeViewer().setAutoExpandLevel(2);
			getTreeViewer().setInput(information);
		}
	}

	private final class MyContentProvider implements ITreeContentProvider {
		private Object input;
		private final AbstractProvider[] providers;
		private HashMap elementMap;

		/**
		 * @param providers
		 */
		public MyContentProvider(AbstractProvider[] providers) {
			this.providers = providers;
			this.elementMap = new HashMap();
			for (int i = 0; i < providers.length; i++) {
				elementMap.put(providers[i], providers[i].getElements());
			}
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof AbstractProvider) {
				return (Object[]) elementMap.get(parentElement);
			}
			if (parentElement == input) {
				return providers;
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			return getChildren(element).length > 0;
		}

		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			this.input = newInput;
		}
	}

	private static final class MyLabelProvider extends LabelProvider {
		private LocalResourceManager resourceManager = new LocalResourceManager(
				JFaceResources.getResources());

		public Image getImage(Object element) {
			Image image = null;
			if (element instanceof AbstractProvider) {
				AbstractProvider provider = (AbstractProvider) element;
				image = findOrCreateImage(provider.getImageDescriptor());
			} else if (element instanceof AbstractElement) {
				image = findOrCreateImage(((AbstractElement) element)
						.getImageDescriptor());
			}
			if (image == null) {
				image = WorkbenchImages
						.getImage(IWorkbenchGraphicConstants.IMG_OBJ_ELEMENT);
			}
			return image;
		}

		/**
		 * @param imageDescriptor
		 * @return image, or null
		 * @throws DeviceResourceException
		 */
		private Image findOrCreateImage(ImageDescriptor imageDescriptor) {
			if (imageDescriptor == null) {
				return null;
			}
			Image image = (Image) resourceManager.find(imageDescriptor);
			if (image == null) {
				try {
					image = resourceManager.createImage(imageDescriptor);
				} catch (DeviceResourceException e) {
					WorkbenchPlugin.log(e);
				}
			}
			return image;
		}

		public void dispose() {
			resourceManager.dispose();
			resourceManager = null;
			super.dispose();
		}

		public String getText(Object element) {
			if (element instanceof AbstractProvider) {
				AbstractProvider provider = (AbstractProvider) element;
				return provider.getName();
			} else if (element instanceof AbstractElement) {
				AbstractElement abstractElement = (AbstractElement) element;
				return abstractElement.getLabel();
			}
			return super.getText(element);
		}
	}

	/**
	 * @param element
	 */
	private void addPreviousPick(Object element) {
		// previousPicks: add selected element to front
		// and remove existing so there are no duplicates
		previousPicksList.remove(element);
		previousPicksList.addFirst(element);

		// elementMap: add element with text as key
		Object replacedElement = elementMap.put(rememberedText, element);

		// textList: add rememberedText to arrayList for given element
		ArrayList textList = (ArrayList) textMap.get(element);
		if (textList == null) {
			textList = new ArrayList();
			textMap.put(element, textList);
		}
		if (!textList.contains(rememberedText)) {
			textList.add(rememberedText);
		}

		// and remove from other elements arrayList if exists
		if (replacedElement != null && !replacedElement.equals(element)) {
			textList = (ArrayList) textMap.get(replacedElement);
			if (textList != null) {
				textList.remove(rememberedText);
				if (textList.isEmpty()) {
					textMap.remove(replacedElement);
					previousPicksList.remove(replacedElement);
				}
			}
		}
	}

	/**
	 * @return the previously picked elements
	 */
	private AbstractElement[] getPreviousPicks() {
		return (AbstractElement[]) previousPicksList
				.toArray(new AbstractElement[previousPicksList.size()]);
	}

	private class PreviousPicksProvider extends AbstractProvider {

		public AbstractElement getElementForId(String id) {
			return null;
		}

		public AbstractElement[] getElements() {
			return getPreviousPicks();
		}

		public String getId() {
			return "org.eclipse.ui.previousPicks"; //$NON-NLS-1$
		}

		public ImageDescriptor getImageDescriptor() {
			return WorkbenchImages
					.getImageDescriptor(IWorkbenchGraphicConstants.IMG_OBJ_NODE);
		}

		public String getName() {
			return IncubatorMessages.CtrlEAction_Previous;
		}
	}
}