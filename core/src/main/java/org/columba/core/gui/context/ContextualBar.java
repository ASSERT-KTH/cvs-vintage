package org.columba.core.gui.context;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.context.semantic.api.IContextEvent;
import org.columba.core.context.semantic.api.IContextListener;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.context.api.IContextualPanel;
import org.columba.core.gui.toolbar.ToolBarButtonFactory;
import org.columba.core.resourceloader.ImageLoader;

public class ContextualBar extends JPanel implements IContextListener {
	private JButton button;

	private IContextualPanel contextualPanel;

	private SearchAction action;

	private IFrameMediator mediator;

	public ContextualBar(IFrameMediator mediator,
			final IContextualPanel contextualPanel) {
		super();

		this.contextualPanel = contextualPanel;
		this.mediator = mediator;
		action = new SearchAction(mediator);

		button = ToolBarButtonFactory.createButton(action);
		button.setEnabled(true);

		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		add(button, BorderLayout.CENTER);

		
		mediator.getSemanticContext().addContextListener(this);
	}

	public void install(JToolBar toolbar) {
		if (toolbar == null)
			throw new IllegalArgumentException("toolbar");

		toolbar.add(button);
	}

	public void contextChanged(IContextEvent event) {
		if ( mediator.getSemanticContext().getValue() != null)
			action.setEnabled(true);
		else action.setEnabled(false);
	}
	
	class SearchAction extends AbstractColumbaAction {
		SearchAction(IFrameMediator mediator) {
			super(mediator, "What's related");
			putValue(SMALL_ICON, ImageLoader.getSmallIcon("system-search.png"));

			// large icon for toolbar
			putValue(LARGE_ICON, ImageLoader.getIcon("system-search.png"));

			putValue(AbstractColumbaAction.SHORT_DESCRIPTION, "What's related");
			putValue(AbstractColumbaAction.LONG_DESCRIPTION, "What's related");

			putValue(TOOLBAR_NAME, "What's related");

			setEnabled(false);
			setShowToolBarText(true);

		}

		public void actionPerformed(ActionEvent e) {
			contextualPanel.search();
		}

	}

	

}
