/*
 * DefaultInputHandler.java - Default implementation of an input handler
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 1999, 2003 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.gjt.sp.jedit.gui;

//{{{ Imports
import javax.swing.KeyStroke;
import java.awt.event.*;
import java.awt.Toolkit;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.gjt.sp.jedit.*;
import org.gjt.sp.util.Log;
//}}}

/**
 * The default input handler. It maps sequences of keystrokes into actions
 * and inserts key typed events into the text area.
 * @author Slava Pestov
 * @version $Id: DefaultInputHandler.java,v 1.28 2003/06/18 19:36:17 spestov Exp $
 */
public class DefaultInputHandler extends InputHandler
{
	//{{{ DefaultInputHandler constructor
	/**
	 * Creates a new input handler with no key bindings defined.
	 * @param view The view
	 */
	public DefaultInputHandler(View view)
	{
		super(view);

		bindings = currentBindings = new Hashtable();
	} //}}}

	//{{{ DefaultInputHandler constructor
	/**
	 * Creates a new input handler with the same set of key bindings
	 * as the one specified. Note that both input handlers share
	 * a pointer to exactly the same key binding table; so adding
	 * a key binding in one will also add it to the other.
	 * @param copy The input handler to copy key bindings from
	 * @param view The view
	 */
	public DefaultInputHandler(View view, DefaultInputHandler copy)
	{
		super(view);

		bindings = currentBindings = copy.bindings;
	} //}}}

	//{{{ addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 * @since jEdit 4.2pre1
	 */
	public void addKeyBinding(String keyBinding, String action)
	{
		_addKeyBinding(keyBinding,(Object)action);
	} //}}}

	//{{{ addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 */
	public void addKeyBinding(String keyBinding, EditAction action)
	{
		_addKeyBinding(keyBinding,(Object)action);
	} //}}}

	//{{{ removeKeyBinding() method
	/**
	 * Removes a key binding from this input handler. This is not yet
	 * implemented.
	 * @param keyBinding The key binding
	 */
	public void removeKeyBinding(String keyBinding)
	{
		Hashtable current = bindings;

		StringTokenizer st = new StringTokenizer(keyBinding);
		while(st.hasMoreTokens())
		{
			String keyCodeStr = st.nextToken();
			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(keyCodeStr);
			if(keyStroke == null)
				return;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = ((Hashtable)o);
				else if(o != null)
				{
					// we have binding foo
					// but user asks to remove foo bar?
					current.remove(keyStroke);
					return;
				}
				else
				{
					// user asks to remove non-existent
					return;
				}
			}
			else
				current.remove(keyStroke);
		}
	} //}}}

	//{{{ removeAllKeyBindings() method
	/**
	 * Removes all key bindings from this input handler.
	 */
	public void removeAllKeyBindings()
	{
		bindings.clear();
	} //}}}

	//{{{ getKeyBinding() method
	/**
	 * Returns either an edit action, or a hashtable if the specified key
	 * is a prefix.
	 * @param keyBinding The key binding
	 * @since jEdit 3.2pre5
	 */
	public Object getKeyBinding(String keyBinding)
	{
		Hashtable current = bindings;
		StringTokenizer st = new StringTokenizer(keyBinding);

		while(st.hasMoreTokens())
		{
			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(keyBinding);
			if(keyStroke == null)
				return null;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = (Hashtable)o;
				else
					return o;
			}
			else
			{
				return current.get(keyStroke);
			}
		}

		return null;
	} //}}}

	//{{{ isPrefixActive() method
	/**
	 * Returns if a prefix key has been pressed.
	 */
	public boolean isPrefixActive()
	{
		return bindings != currentBindings;
	} //}}}

	//{{{ keyPressed() method
	/**
	 * Handle a key pressed event. This will look up the binding for
	 * the key stroke and execute it.
	 */
	public void keyPressed(KeyEvent evt)
	{
		KeyEventTranslator.Key keyStroke = KeyEventTranslator.translateKeyEvent(evt);
		if(keyStroke == null)
			return;

		if(keyStroke.modifiers != null)
		{
			if(readNextChar != null)
			{
				if(keyStroke.key == KeyEvent.VK_ESCAPE)
				{
					readNextChar = null;
					view.getStatus().setMessage(null);
				}
				else if(keyStroke.key == KeyEvent.VK_TAB
					|| keyStroke.key == KeyEvent.VK_ENTER)
				{
					setCurrentBindings(bindings);
					invokeReadNextChar((char)keyStroke.key);
					repeatCount = 1;
					return;
				}
			}
			else
			{
				// ok even with no modifiers
			}
		}
		else
		{
			// no modifiers, so ditch certain events handled in
			// keyTyped
			if(keyStroke.key == KeyEvent.VK_SPACE
				|| keyStroke.key == KeyEvent.VK_ENTER
				|| keyStroke.key == KeyEvent.VK_TAB)
			{
				return;
			}
		}

		Object o = currentBindings.get(keyStroke);
		if(o == null)
		{
			// Don't beep if the user presses some
			// key we don't know about unless a
			// prefix is active. Otherwise it will
			// beep when caps lock is pressed, etc.
			if(currentBindings != bindings)
			{
				Toolkit.getDefaultToolkit().beep();
				// F10 should be passed on, but C+e F10
				// shouldn't
				repeatCount = 1;
				evt.consume();
				setCurrentBindings(bindings);
			}
		}

		if(readNextChar != null)
		{
			readNextChar = null;
			view.getStatus().setMessage(null);
		}

		if(o instanceof String)
		{
			setCurrentBindings(bindings);
			invokeAction((String)o);
			evt.consume();
		}
		else if(o instanceof EditAction)
		{
			setCurrentBindings(bindings);
			invokeAction((EditAction)o);
			evt.consume();
		}
		else if(o instanceof Hashtable)
		{
			setCurrentBindings((Hashtable)o);
			evt.consume();
		}

		if(o == null)
		{
			switch(evt.getKeyCode())
			{
				case KeyEvent.VK_NUMPAD0:   case KeyEvent.VK_NUMPAD1:
				case KeyEvent.VK_NUMPAD2:   case KeyEvent.VK_NUMPAD3:
				case KeyEvent.VK_NUMPAD4:   case KeyEvent.VK_NUMPAD5:
				case KeyEvent.VK_NUMPAD6:   case KeyEvent.VK_NUMPAD7:
				case KeyEvent.VK_NUMPAD8:   case KeyEvent.VK_NUMPAD9:
				case KeyEvent.VK_MULTIPLY:  case KeyEvent.VK_ADD:
				/* case KeyEvent.VK_SEPARATOR: */ case KeyEvent.VK_SUBTRACT:
				case KeyEvent.VK_DECIMAL:   case KeyEvent.VK_DIVIDE:
					KeyEventWorkaround.numericKeypadKey();
					break;
			}
		}
	} //}}}

	//{{{ keyTyped() method
	/**
	 * Handle a key typed event. This inserts the key into the text area.
	 */
	public void keyTyped(KeyEvent evt)
	{
		KeyEventTranslator.Key keyStroke = KeyEventTranslator.translateKeyEvent(evt);
		if(keyStroke == null)
			return;

		if(readNextChar != null)
		{
			setCurrentBindings(bindings);
			invokeReadNextChar(keyStroke.input);
			repeatCount = 1;
			return;
		}

		char input = keyStroke.input;

		switch(keyStroke.input)
		{
		case KeyEvent.VK_SPACE: /* == ' '  */
		case KeyEvent.VK_ENTER: /* == '\n' */
		case KeyEvent.VK_TAB:   /* == '\t' */
		System.err.println("special");
			keyStroke.key = keyStroke.input;
			keyStroke.input = '\0';
		}

		Object o = currentBindings.get(keyStroke);

		if(o instanceof Hashtable)
		{
			setCurrentBindings((Hashtable)o);
		}
		else if(o instanceof String)
		{
			setCurrentBindings(bindings);
			invokeAction((String)o);
		}
		else if(o instanceof EditAction)
		{
			setCurrentBindings(bindings);
			invokeAction((EditAction)o);
		}
		else
		{
			setCurrentBindings(bindings);
			userInput(input);
		}
	} //}}}

	//{{{ getSymbolicModifierName() method
	/**
	 * Returns a the symbolic modifier name for the specified Java modifier
	 * flag.
	 *
	 * @param mod A modifier constant from <code>InputEvent</code>
	 *
	 * @since jEdit 4.1pre3
	 */
	public static char getSymbolicModifierName(int mod)
	{
		return KeyEventTranslator.getSymbolicModifierName(mod);
	} //}}}

	//{{{ getModifierString() method
	/**
	 * Returns a string containing symbolic modifier names set in the
	 * specified event.
	 *
	 * @param evt The event
	 *
	 * @since jEdit 4.1pre3
	 */
	public static String getModifierString(InputEvent evt)
	{
		return KeyEventTranslator.getModifierString(evt);
	} //}}}

	//{{{ parseKeyStroke() method
	/**
	 * Converts a string to a keystroke. The string should be of the
	 * form <i>modifiers</i>+<i>shortcut</i> where <i>modifiers</i>
	 * is any combination of A for Alt, C for Control, S for Shift
	 * or M for Meta, and <i>shortcut</i> is either a single character,
	 * or a keycode name from the <code>KeyEvent</code> class, without
	 * the <code>VK_</code> prefix.
	 * @param keyStroke A string description of the key stroke
	 */
	public static KeyStroke parseKeyStroke(String keyStroke)
	{
		if(keyStroke == null)
			return null;
		int modifiers = 0;
		int index = keyStroke.indexOf('+');
		if(index != -1)
		{
			for(int i = 0; i < index; i++)
			{
				switch(Character.toUpperCase(keyStroke
					.charAt(i)))
				{
				case 'A':
					modifiers |= KeyEventTranslator.a;
					break;
				case 'C':
					modifiers |= KeyEventTranslator.c;
					break;
				case 'M':
					modifiers |= KeyEventTranslator.m;
					break;
				case 'S':
					modifiers |= KeyEventTranslator.s;
					break;
				}
			}
		}
		String key = keyStroke.substring(index + 1);
		if(key.length() == 1)
		{
			char ch = key.charAt(0);
			if(modifiers == 0)
				return KeyStroke.getKeyStroke(ch);
			else
			{
				return KeyStroke.getKeyStroke(Character.toUpperCase(ch),
					modifiers);
			}
		}
		else if(key.length() == 0)
		{
			Log.log(Log.ERROR,DefaultInputHandler.class,
				"Invalid key stroke: " + keyStroke);
			return null;
		}
		else
		{
			int ch;

			try
			{
				ch = KeyEvent.class.getField("VK_".concat(key))
					.getInt(null);
			}
			catch(Exception e)
			{
				Log.log(Log.ERROR,DefaultInputHandler.class,
					"Invalid key stroke: "
					+ keyStroke);
				return null;
			}

			return KeyStroke.getKeyStroke(ch,modifiers);
		}
	} //}}}

	//{{{ Private members

	// Stores prefix name in bindings hashtable
	private static Object PREFIX_STR = "PREFIX_STR";

	private Hashtable bindings;
	private Hashtable currentBindings;

	//{{{ setCurrentBindings() method
	private void setCurrentBindings(Hashtable bindings)
	{
		String prefixStr = (String)bindings.get(PREFIX_STR);
		if(prefixStr != null)
		{
			if(currentBindings != this.bindings)
			{
				//XXX this won't work past 2 levels of prefixing
				prefixStr = currentBindings.get(PREFIX_STR)
					+ " " + prefixStr;
			}

			view.getStatus().setMessage(prefixStr);
		}
		else
			view.getStatus().setMessage(null);

		currentBindings = bindings;
	} //}}}

	//{{{ _addKeyBinding() method
	/**
	 * Adds a key binding to this input handler. The key binding is
	 * a list of white space separated key strokes of the form
	 * <i>[modifiers+]key</i> where modifier is C for Control, A for Alt,
	 * or S for Shift, and key is either a character (a-z) or a field
	 * name in the KeyEvent class prefixed with VK_ (e.g., BACK_SPACE)
	 * @param keyBinding The key binding
	 * @param action The action
	 */
	public void _addKeyBinding(String keyBinding, Object action)
	{
		Hashtable current = bindings;

		StringTokenizer st = new StringTokenizer(keyBinding);
		while(st.hasMoreTokens())
		{
			String keyCodeStr = st.nextToken();
			KeyEventTranslator.Key keyStroke = KeyEventTranslator.parseKey(keyCodeStr);
			if(keyStroke == null)
				return;

			if(st.hasMoreTokens())
			{
				Object o = current.get(keyStroke);
				if(o instanceof Hashtable)
					current = (Hashtable)o;
				else
				{
					Hashtable hash = new Hashtable();
					hash.put(PREFIX_STR,keyCodeStr);
					o = hash;
					current.put(keyStroke,o);
					current = (Hashtable)o;
				}
			}
			else
				current.put(keyStroke,action);
		}
	} //}}}

	//}}}
}
