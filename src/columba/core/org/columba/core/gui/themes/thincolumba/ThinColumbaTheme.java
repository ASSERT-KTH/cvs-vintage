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
package org.columba.core.gui.themes.thincolumba;

import javax.swing.ImageIcon;
import javax.swing.UIDefaults;
import javax.swing.plaf.ColorUIResource;

import org.columba.core.config.GuiItem;
import org.columba.core.config.ThemeItem;
import org.columba.core.gui.themes.DefaultCTheme;
import org.columba.core.gui.util.ImageLoader;

public class ThinColumbaTheme extends DefaultCTheme {

	private final static Integer ANTI_ALIASING_ENABLED = new Integer(1);
	private final static Integer ANTI_ALIASING_DISABLED = new Integer(0);
	private final static Integer ANTI_ALIASING_MESSAGE_ONLY = new Integer(2);

	private final ColorUIResource secondary1 =
		new ColorUIResource(148, 148, 148);
	private final ColorUIResource secondary2 =
		new ColorUIResource(160, 160, 160);
	private final ColorUIResource secondary3 =
		new ColorUIResource(213, 213, 213);

	private final ColorUIResource primary1 = new ColorUIResource(148, 148, 148);
	private final ColorUIResource primary2 = new ColorUIResource(150, 150, 150);
	private final ColorUIResource primary3 = new ColorUIResource(152, 152, 152);

	private final ColorUIResource foreground;
	private final ColorUIResource background;

	protected static final String thinPackage =
		"org.columba.core.gui.themes.thincolumba.";

	public ThinColumbaTheme(GuiItem item) {
		super(item);

		ThemeItem themeItem = new ThemeItem(item.getElement("theme"));

		foreground = new ColorUIResource(themeItem.getForeground());

		background = new ColorUIResource(themeItem.getBackground());

		// mainFont = mFont;
		// messageFont = eFont;
	}

	// menu border, labels
	protected ColorUIResource getPrimary1() {
		return primary1;
	}

	protected ColorUIResource getPrimary2() {
		return primary2;
	}

	protected ColorUIResource getPrimary3() {
		return primary3;
	}

	protected ColorUIResource getSecondary1() {
		return secondary1;
	}
	protected ColorUIResource getSecondary2() {
		return secondary2;
	}
	protected ColorUIResource getSecondary3() {
		return secondary3;
	}

	public String getName() {
		return "Thin Columba";
	}

	public void addCustomEntriesToTable(UIDefaults table) {
		Object map[] =
			{
				"ButtonUI",
				thinPackage + "ThinButtonUI",
				"CheckBoxUI",
				thinPackage + "ThinCheckBoxUI",
				"CheckBoxMenuItemUI",
				thinPackage + "ThinCheckBoxMenuItemUI",
				"ComboBoxUI",
				thinPackage + "ThinComboBoxUI",
				"DesktopIconUI",
				thinPackage + "ThinDesktopIconUI",
				"EditorPaneUI",
				thinPackage + "ThinEditorPaneUI",
				"FileChooserUI",
				thinPackage + "ThinFileChooserUI",
				"InternalFrameUI",
				thinPackage + "ThinInternalFrameUI",
				"LabelUI",
				thinPackage + "ThinLabelUI",
				"MenuUI",
				thinPackage + "ThinMenuUI",
				"MenuBarUI",
				thinPackage + "ThinMenuBarUI",
				"MenuItemUI",
				thinPackage + "ThinMenuItemUI",
				"PasswordFieldUI",
				thinPackage + "ThinPasswordFieldUI",
				"ProgressBarUI",
				thinPackage + "ThinProgressBarUI",
				"PopupMenuSeparatorUI",
				thinPackage + "ThinPopupMenuSeparatorUI",
				"RadioButtonUI",
				thinPackage + "ThinRadioButtonUI",
				"RadioButtonMenuItemUI",
				thinPackage + "ThinRadioButtonMenuItemUI",
				"ScrollBarUI",
				thinPackage + "ThinScrollBarUI",
				"ScrollPaneUI",
				thinPackage + "ThinScrollPaneUI",
				"SplitPaneUI",
				thinPackage + "ThinSplitPaneUI",
				"SliderUI",
				thinPackage + "ThinSliderUI",
				"SeparatorUI",
				thinPackage + "ThinSeparatorUI",
				"TabbedPaneUI",
				thinPackage + "ThinTabbedPaneUI",
				"TextAreaUI",
				thinPackage + "ThinTextAreaUI",
				"TextFieldUI",
				thinPackage + "ThinTextFieldUI",
				"TextPaneUI",
				thinPackage + "ThinTextPaneUI",
				"ToggleButtonUI",
				thinPackage + "ThinToggleButtonUI",
				"ToolBarUI",
				thinPackage + "ThinToolBarUI",
				"ToolTipUI",
				thinPackage + "ThinToolTipUI",
				"ListUI",
				thinPackage + "ThinListUI",
				"TableUI",
				thinPackage + "ThinTableUI",
				"TreeUI",
				thinPackage + "ThinTreeUI" };
		table.putDefaults(map);

		// antialiasing==0 -> no antialiasing
		// antialiasing==1 -> antialiasing of everything
		// antialiasing==2 -> antialiasing for message-component only
		table.put("antialiasing", ANTI_ALIASING_DISABLED);

		table.put("Tree.selectionBackground", background);
		table.put("Table.selectionBackground", background);
		table.put("TextField.selectionBackground", background);
		table.put("TextArea.selectionBackground", background);
		table.put("List.selectionBackground", background);

		table.put("Tree.selectionForeground", foreground);
		table.put("Table.selectionForeground", foreground);
		table.put("TextField.selectionForeground", foreground);
		table.put("TextArea.selectionForeground", foreground);
		table.put("List.selectionForeground", foreground);

		table.put("Menu.selectionBackground", background);
		table.put("MenuItem.selectionBackground", background);
		table.put("CheckBoxMenuItem.selectionBackground", background);
		table.put("RadioButtonMenuItem.selectionBackground", background);

		table.put("Menu.selectionForeground", foreground);
		table.put("MenuItem.selectionForeground", foreground);
		table.put("CheckBoxMenuItem.selectionForeground", foreground);
		table.put("RadioButtonMenuItem.selectionForeground", foreground);

		/*
		table.put(
			"Menu.selectionBackground",
			new ColorUIResource(235, 235, 235));
		table.put(
			"MenuItem.selectionBackground",
			new ColorUIResource(235, 235, 235));
		table.put(
			"CheckBoxMenuItem.selectionBackground",
			new ColorUIResource(235, 235, 235));
		table.put(
			"RadioButtonMenuItem.selectionBackground",
			new ColorUIResource(235, 235, 235));
		*/

		// set soft yellow background
		table.put("ToolTip.background", new ColorUIResource(255, 255, 197));
		Object toolTipBorder =
			new UIDefaults.ProxyLazyValue(
				"javax.swing.plaf.BorderUIResource$LineBorderUIResource",
				new Object[] { new ColorUIResource(0, 0, 0)});

		table.put("ToolTip.border", toolTipBorder);

		table.put(
			"SplitPane.border",
			"javax.swing.plaf.metal.MetalBorders$TableHeaderBorder");
		table.put("SplitPane.dividerSize", new Integer(5));
		table.put("SplitPaneDivider.border", null);

		table.put("StatusBar.border", new ThinStatusBarBorder());

		table.put("ScrollBar.width", new Integer(17));

		table.put("MenuBar.border", new ThinDefaultBorder());

		table.put("ToolBar.border", new ThinDefaultBorder());
		
		// default for Metal is 8, which is too big
		table.put("TextField.tabSize", new Integer(4));
		table.put("TextArea.tabSize", new Integer(4));
		table.put("TextPane.tabSize", new Integer(4));
		
		/*
		table.put("Menu.checkIcon", new ImageIcon("") );
		table.put("MenuItem.checkIcon", new ImageIcon(""));
		*/

		table.put("CheckBoxMenuItem.checkIcon", new ImageIcon(""));
		table.put("RadioButtonMenuItem.checkIcon", new ImageIcon(""));

		table.put(
			"MenuItem.acceleratorForeground",
			getAcceleratorSelectedForeground());
		table.put(
			"MenuItem.acceleratorSelectionForeground",
			getAcceleratorSelectedForeground());

		table.put(
			"OptionPane.errorIcon",
			ImageLoader.getImageIcon("stock_dialog_error_48.png"));
		table.put(
			"OptionPane.informationIcon",
			ImageLoader.getImageIcon("stock_dialog_info_48.png"));
		table.put(
			"OptionPane.warningIcon",
			ImageLoader.getImageIcon("stock_dialog_warning_48.png"));
		table.put(
			"OptionPane.questionIcon",
			ImageLoader.getImageIcon("stock_dialog_question_48.png"));

		/*
		table.put(
			"CheckBoxMenuItem.border",
			BorderFactory.createEmptyBorder(1, 1, 1, 1));
		table.put(
			"RadioButtonMenuItem.border",
			BorderFactory.createEmptyBorder(1, 1, 1, 1));
		table.put(
			"MenuItem.border",
			BorderFactory.createEmptyBorder(1, 1, 1, 1));
		table.put("Menu.border", BorderFactory.createEmptyBorder(1,1,1,1));
		*/
		table.put("CheckBoxMenuItem.borderPainted", Boolean.FALSE);
		table.put("RadioButtonMenuItem.borderPainted", Boolean.FALSE);
		table.put("MenuItem.borderPainted", Boolean.FALSE);
		table.put("Menu.borderPainted", Boolean.FALSE);

		/*
		table.put(
			"SplitPaneUI",
			"org.columba.core.gui.themes.thincolumba.ThinColumbaSplitPaneUI");
		
		
		table.put(
			"ScrollBarUI",
			"org.columba.core.gui.themes.thincolumba.ThinColumbaScrollBarUI");
		
		
		table.put(
			"TreeUI",
			"org.columba.core.gui.themes.thincolumba.ThinColumbaTreeUI");
		
		table.put("MenuBar.border", new ThinColumbaMenuBarBorder());
		
		table.put("ToolBar.border", new ThinColumbaToolBarBorder());
		
		//table.put("MenuItemUI","org.columba.core.gui.themes.thincolumba.ThinColumbaMenuItemUI");
		
		
		table.put("Menu.selectionBackground", new ColorUIResource(235, 235, 235));
		table.put("MenuItem.selectionBackground", new ColorUIResource(235, 235, 235));
		table.put(
			"CheckBoxMenuItem.selectionBackground",
			new ColorUIResource(235, 235, 235));
		table.put(
			"RadioButtonMenuItem.selectionBackground",
			new ColorUIResource(235, 235, 235));
		
		table.put("ToolTip.background", new ColorUIResource(255, 255, 255));
		
		
		table.put("ScrollBar.width", new Integer(15));
		table.put("ScrollBar.allowsAbsolutePositioning", Boolean.FALSE);
		
		
		table.put("SplitPane.dividerSize", new Integer(5));
		table.put("SplitPane.border", null);
		
		table.put("Label.foreground", table.get("textText"));
		
		table.put("ScrollBar.minimumThumbSize", new java.awt.Dimension(15, 15));
		
		table.put("HeaderView.border", new ThinColumbaMessageHeaderBorder());
		
		
		
		table.put("ComboBox.listBackground", new ColorUIResource(255,255,255) );
		
		*/

	}

}