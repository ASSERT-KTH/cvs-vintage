/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.keys;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.keys.CharacterKey;
import org.eclipse.ui.keys.KeySequence;
import org.eclipse.ui.keys.KeyStroke;
import org.eclipse.ui.keys.NaturalKey;
import org.eclipse.ui.keys.ParseException;

/**
 * A wrapper around the SWT text widget that traps literal key presses and 
 * converts them into key sequences for display.  There are two types of key
 * strokes that are displayed: complete and incomplete.  A complete key stroke
 * is one with a natural key, while an incomplete one has no natural key.  
 * Incomplete key strokes are only displayed until they are made complete or
 * their component key presses are released.
 */
public final class KeySequenceText {

	/** 
	 * The special integer value for the maximum number of strokes indicating
	 * that an infinite number should be allowed.
	 */
	public static final int INFINITE = -1;
	/** An empty string instance for use in clearing text values. */
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The text of the key sequence -- containing only the complete key strokes. */
	private KeySequence keySequence = KeySequence.getInstance();
	/** The maximum number of key strokes permitted in the sequence. */
	private int maxStrokes = INFINITE;
	/** The incomplete key stroke, if any. */
	private KeyStroke temporaryStroke = null;
	/** The text widget that is wrapped for this class. */
	private final Text text;
	/** The listener that makes sure that the text widget remains up-to-date
	 * with regards to external modification of the text (e.g., cut & pasting).
	 */
	private final UpdateSequenceListener updateSequenceListener = new UpdateSequenceListener();

	/**
	 * Constructs an instance of <code>KeySequenceTextField</code> with the 
	 * widget that will be containing the text field.  The font is set based on
	 * this container.
	 * 
	 * @param composite The container widget; must not be <code>null</code>.
	 */
	public KeySequenceText(final Composite composite) {
		// Set up the text field.
		text = new Text(composite, SWT.BORDER);

		/* TODO doug: pls. investigate. works until one backspaces to an empty text field, at which point the font gets changed somehow 
		if ("carbon".equals(SWT.getPlatform())) {
			// don't worry about this font name here, it is the official menu font and point size on the mac.
			final Font font = new Font(text.getDisplay(), "Lucida Grande", 13, SWT.NORMAL); //$NON-NLS-1$
		
			text.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					font.dispose();
				}
			});	
			
			text.setFont(font);		
		}
		*/

		// Add the key listener.
		final Listener keyFilter = new KeyTrapListener();
		text.addListener(SWT.KeyUp, keyFilter);
		text.addListener(SWT.KeyDown, keyFilter);

		// Add the traversal listener.
		text.addTraverseListener(new FocusTrapListener());

		// Add an internal modify listener.
		text.addModifyListener(updateSequenceListener);
	}

	/**
	 * Adds a listener for modification of the text component.
	 * @param modifyListener The listener that is to be added; must not be
	 * <code>null</code>.
	 */
	public final void addModifyListener(final ModifyListener modifyListener) {
		text.addModifyListener(modifyListener);
	}

	/**
	 * Clears the text field and resets all the internal values.
	 */
	public final void clear() {
		keySequence = null;
		temporaryStroke = null;
		text.setText(EMPTY_STRING);
	}

	/**
	 * An accessor for the <code>KeySequence</code> that corresponds to the 
	 * current state of the text field.  This includes incomplete strokes.
	 * @return The key sequence representation; never <code>null</code>.
	 */
	public final KeySequence getKeySequence() {
		return keySequence;
	}
	
	/**
	 * An accessor for the <code>KeyStroke</code> that is currently held as an
	 * incomplete stroke (i.e., one without a natural key).
	 * @return The incomplete stroke; may be <code>null</code> if there are no
	 * incomplete strokes.
	 */
	final KeyStroke getTemporaryStroke() {
		return temporaryStroke;
	}
	
	/** 
	 * An accessor for the underlying text widget used by this entry field.
	 * @return The <code>Text</code> instance; never <code>null</code>.
	 */
	final Text getText() {
		return text;
	}

	/**
	 * Tests whether the current key sequence has a stroke with no natural key.
	 * @return <code>true</code> is there is an incomplete stroke; 
	 * <code>false</code> otherwise.
	 */
	public final boolean hasIncompleteStroke() {
		return (temporaryStroke != null);
	}

	/**
	 * Checks whether the given key stroke is a temporary key stroke or not.
	 * @param keyStroke The key stroke to check for completion; may be
	 * <code>null</code>, which results in <code>false</code>.
	 * @return <code>true</code> if the key stroke has no natural key; 
	 * <code>false</code> otherwise.
	 */
	static final boolean isComplete(final KeyStroke keyStroke) {
		if (keyStroke != null) {
			final NaturalKey naturalKey = keyStroke.getNaturalKey();

			if (naturalKey instanceof CharacterKey) {
				final CharacterKey characterKey = (CharacterKey) naturalKey;
				return (characterKey.getCharacter() != '\0');
			} else
				return true;
		}

		return false;
	}

	/**
	 * A mutator for the enabled state of the wrapped widget.
	 * @param enabled Whether the text field should be enabled.
	 */
	public final void setEnabled(final boolean enabled) {
		text.setEnabled(enabled);
	}

	/**
	 * <p>
	 * A mutator for the key sequence and incomplete stroke stored within this
	 * widget.  This does some checking to see if the incomplete stroke is 
	 * really incomplete; if it is complete, then it is rolled into the key 
	 * sequence.  The text and caret position are updated.
	 * </p>
	 * <p>
	 * All sequences are limited to maxStrokes number of strokes in length.
	 * If there are already that number of strokes, then it does not show
	 * incomplete strokes, and does not keep track of them.
	 * </p>
	 *   
	 * @param newKeySequence The new key sequence for this widget; may be
	 * <code>null</code> if none.
	 * @param incompleteStroke The new incomplete stroke for this widget; may be
	 * <code>null</code> or incomplete -- both conditions are dealt with.
	 */
	public final void setKeySequence(final KeySequence newKeySequence, final KeyStroke incompleteStroke) {
		// Figure out whether the stroke should be rolled in.
		if (isComplete(incompleteStroke)) {
			if (newKeySequence == null) {
				// This is guaranteed to be possible by setMaxStrokes
				keySequence = KeySequence.getInstance(incompleteStroke);
			} else {
				final List keyStrokes = new ArrayList(newKeySequence.getKeyStrokes());
				keyStrokes.add(incompleteStroke);
				if (maxStrokes != INFINITE) {
					final int keyStrokesSize = keyStrokes.size();
					for (int i = keyStrokesSize - 1; i >= maxStrokes; i--) {
						keyStrokes.remove(i);
					}
				}
				keySequence = KeySequence.getInstance(keyStrokes);
			}
			temporaryStroke = null;
		} else {
			if ((newKeySequence != null) && (maxStrokes != INFINITE)) {
				final List untrimmedKeyStrokes = newKeySequence.getKeyStrokes();
				final int keyStrokesSize = untrimmedKeyStrokes.size();
				if (keyStrokesSize > maxStrokes) {
					final List keyStrokes = new ArrayList(untrimmedKeyStrokes);
					for (int i = keyStrokesSize - 1; i >= maxStrokes; i--) {
						keyStrokes.remove(i);
					}
					keySequence = KeySequence.getInstance(keyStrokes);
					temporaryStroke = null;
				} else if (keyStrokesSize == maxStrokes) {
					keySequence = newKeySequence;
					temporaryStroke = null;
				} else {
					keySequence = newKeySequence;
					temporaryStroke = incompleteStroke;
				}
			} else {
				keySequence = newKeySequence;
				temporaryStroke = incompleteStroke;
			}
		}

		/* Create a dummy (and rather invalid) sequence to get localized display
		 * formatting
		 */
		final KeySequence dummySequence;
		if (keySequence == null) {
			if (temporaryStroke == null) {
				dummySequence = KeySequence.getInstance();
			} else {
				dummySequence = KeySequence.getInstance(temporaryStroke);
			}
		} else {
			final List keyStrokes = new ArrayList(keySequence.getKeyStrokes());
			if (temporaryStroke != null) {
				keyStrokes.add(temporaryStroke);
			}
			dummySequence = KeySequence.getInstance(keyStrokes);
		}
		
		// We need to update the text, but we don't need to synchronize.
		text.removeModifyListener(updateSequenceListener);
		text.setText(/* TODO "carbon".equals(SWT.getPlatform()) ? KeySupport.formatCarbon(dummySequence) : */
		dummySequence.format());
		text.addModifyListener(updateSequenceListener);

		// Update the caret position.
		text.setSelection(text.getText().length());
	}

	/**
	 * A mutator for the layout information associated with the wrapped widget.
	 * @param layoutData The layout information; must not be <code>null</code>.
	 */
	public final void setLayoutData(final Object layoutData) {
		text.setLayoutData(layoutData);
	}

	/**
	 * A mutator for the maximum number of strokes that are permitted in this
	 * widget at one time.
	 * @param maximumStrokes The maximum number of strokes; should be a positive
	 * integer or <code>INFINITE</code>.
	 */
	public final void setMaxStrokes(final int maximumStrokes) {
		if ((maximumStrokes > 0) || (maximumStrokes == INFINITE)) {
			maxStrokes = maximumStrokes;
		}
	}

	/**
	 * A traversal listener that blocks all traversal except for tabs and arrow
	 * keys.
	 */
	private final class FocusTrapListener implements TraverseListener {

		/**
		 * Handles the traverse event on the text field wrapped by this class.
		 * It swallows all traverse events example for tab and arrow key 
		 * navigation.  The other forms of navigation can be reached by tabbing
		 * off of the control.
		 * 
		 * @param event The trigger event; must not be <code>null</code>.
		 */
		public final void keyTraversed(final TraverseEvent event) {
			switch (event.detail) {
				case SWT.TRAVERSE_ESCAPE :
				case SWT.TRAVERSE_MNEMONIC :
				case SWT.TRAVERSE_NONE :
				case SWT.TRAVERSE_PAGE_NEXT :
				case SWT.TRAVERSE_PAGE_PREVIOUS :
				case SWT.TRAVERSE_RETURN :
					event.doit = false;
					break;
					
				case SWT.TRAVERSE_TAB_NEXT :
				case SWT.TRAVERSE_TAB_PREVIOUS :				
					// Check if modifiers other than just 'Shift' were down.
					if ((event.stateMask & (SWT.MODIFIER_MASK ^ SWT.SHIFT)) != 0) {
						// Modifiers other than shift were down.
						event.doit = false;
						break;
					}
					// fall through -- either no modifiers, or just shift.
				
				case SWT.TRAVERSE_ARROW_NEXT :
				case SWT.TRAVERSE_ARROW_PREVIOUS :
				default :
					// Let the traversal happen, but clear the incomplete stroke
					setKeySequence(getKeySequence(), null);
			}
		}
	}

	/**
	 * A key listener that traps incoming events and displays them in the 
	 * wrapped text field.  It has no effect on traversal operations.
	 */
	private final class KeyTrapListener implements Listener {
		/**
		 * Handles the key pressed and released events on the wrapped text 
		 * widget.  This makes sure to either add the pressed key to the 
		 * temporary key stroke, or complete the current temporary key stroke 
		 * and prompt for the next.  In the case of a key release, this makes 
		 * sure that the temporary stroke is correctly displayed -- 
		 * corresponding with modifier keys that may have been released.
		 * 
		 * @param event The triggering event; must not be <code>null</code>. 
		 */
		public final void handleEvent(final Event event) {
			if (event.type == SWT.KeyDown) {
				if ((event.character == SWT.BS) && (event.stateMask == 0)) {
					// Remove the last key stroke.
					if (hasIncompleteStroke()) {
						/* Remove the incomplete stroke.  This should not really
						 * be possible, but it is better to be safe than sorry.
						 */
						setKeySequence(getKeySequence(), null);
					} else {
						// Remove the last complete stroke.
						final KeySequence sequence = getKeySequence();
						final List keyStrokes = new ArrayList(sequence.getKeyStrokes());
						if (!keyStrokes.isEmpty()) {
							keyStrokes.remove(keyStrokes.size() - 1);
							setKeySequence(KeySequence.getInstance(keyStrokes), null);
						}
					}
				} else {
					// Handles the key pressed event.
					final int key = KeySupport.convertEventToAccelerator(event);
					final KeyStroke stroke = KeySupport.convertAcceleratorToKeyStroke(key);
					setKeySequence(getKeySequence(), stroke);
				}

			} else if (hasIncompleteStroke()) {
				/* Handles the key released event, which is only relevant if
				 * there is an incomplete stroke.
				 */
				/* Figure out the SWT integer representation of the remaining
				 * values.
				 */
				final Event mockEvent = new Event();
				if ((event.keyCode & SWT.MODIFIER_MASK) != 0) {
					// This key up is a modifier key being released.
					mockEvent.stateMask = event.stateMask - event.keyCode;
				} else {
					/* This key up is the other end of a key down that was
					 * trapped by the operating system.
					 */
					mockEvent.stateMask = event.stateMask;
				}

				/* Get a reasonable facsimile of the stroke that is still
				 * pressed.
				 */
				final int key = KeySupport.convertEventToAccelerator(mockEvent);
				final KeyStroke remainingStroke = KeySupport.convertAcceleratorToKeyStroke(key);

				if (remainingStroke.getModifierKeys().isEmpty()) {
					setKeySequence(getKeySequence(), null);
				} else {
					setKeySequence(getKeySequence(), remainingStroke);
				}

			}

			// Prevent the event from reaching the widget.
			event.doit = false;
		}
	}

	/**
     * A modification listener that makes sure that external events to this 
     * class (i.e., direct modification of the underlying text) do not break
     * this class' view of the world.
     */
	private final class UpdateSequenceListener implements ModifyListener {
		/**
		 * Handles the modify event on the underlying text widget.
         * @param event The triggering event; ignored.
         */
		public final void modifyText(final ModifyEvent event) {
			try {
				// The original sequence.
				final KeySequence originalSequence = getKeySequence();
				final List keyStrokes = new ArrayList(originalSequence.getKeyStrokes());
				if (getTemporaryStroke() != null) {
					keyStrokes.add(getTemporaryStroke());
				}
				final KeySequence sequenceFromStrokes = KeySequence.getInstance(keyStrokes);

				// The new sequence drawn from the text.
				final String contents = getText().getText();
				final KeySequence sequenceFromText = KeySequence.getInstance(contents);

				// Check to see if they're the same.
				if (!sequenceFromStrokes.equals(sequenceFromText)) {
					final List strokes = sequenceFromText.getKeyStrokes();
					final Iterator strokeItr = strokes.iterator();
					while (strokeItr.hasNext()) {
						// Make sure that it's a valid sequence.
						if (!isComplete((KeyStroke) strokeItr.next())) {
							setKeySequence(getKeySequence(), getTemporaryStroke());
							return;
						}
					}
					setKeySequence(sequenceFromText, null);
				}
			} catch (final ParseException e) {
				// Abort any cut/paste-driven modifications
				setKeySequence(getKeySequence(), getTemporaryStroke());
			}
		}
	}
}