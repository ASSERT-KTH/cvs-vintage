package org.eclipse.ui.internal.keybindings;

/**
Copyright (c) 2002 IBM Corp.
All rights reserved.  This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
*/

import org.eclipse.ui.IMemento;

public final class KeyStroke implements Comparable {

	public final static String TAG = "keystroke";
	private final static String ATTRIBUTE_ACCELERATOR = "accelerator";

	public static KeyStroke create(int accelerator) {
		return new KeyStroke(accelerator);
	}

	public static KeyStroke[] create(int[] accelerators)
		throws IllegalArgumentException {
		if (accelerators == null)
			throw new IllegalArgumentException();
					
		KeyStroke[] keyStrokes = new KeyStroke[accelerators.length];
			
		for (int i = 0; i < accelerators.length; i++)
			keyStrokes[i] = create(accelerators[i]);
		
		return keyStrokes;			
	}

	public static KeyStroke read(IMemento memento)
		throws IllegalArgumentException {
		if (memento == null)
			throw new IllegalArgumentException();

		Integer accelerator = memento.getInteger(ATTRIBUTE_ACCELERATOR);
		
		if (accelerator == null)
			throw new IllegalArgumentException();
		
		return KeyStroke.create(accelerator.intValue());
	}

	public static void write(IMemento memento, KeyStroke keyStroke)
		throws IllegalArgumentException {
		if (memento == null || keyStroke == null)
			throw new IllegalArgumentException();
			
		memento.putInteger(ATTRIBUTE_ACCELERATOR, keyStroke.getAccelerator());
	}

	private int accelerator;

	private KeyStroke(int accelerator) {
		super();
		this.accelerator = accelerator;
	}

	public int getAccelerator() {
		return accelerator;
	}
	
	public int compareTo(Object object) {
		if (!(object instanceof KeyStroke))
			throw new ClassCastException();
			
		return accelerator - ((KeyStroke) object).accelerator;
	}
	
	public boolean equals(Object object) {
		return object instanceof KeyStroke && 
			accelerator == ((KeyStroke) object).accelerator;	
	}
}