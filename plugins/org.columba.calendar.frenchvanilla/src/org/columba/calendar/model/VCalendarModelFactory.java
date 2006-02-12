// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar.model;

import org.columba.calendar.parser.XCSDocumentParser;
import org.columba.calendar.parser.SyntaxException;
import org.jdom.Document;

public final class VCalendarModelFactory {

	public VCalendarModelFactory() {
		super();
	}

	public static Document marshall(ICalendarModel c) throws SyntaxException,
			IllegalArgumentException {

		if (c == null)
			throw new IllegalArgumentException("calendarmodel == null");

		XCSDocumentParser model = new XCSDocumentParser(c.getType());

		model.setId(c.getId());
		model.setDescription(c.getDescription());
		model.setSummary(c.getSummary());

		if ( c.getDtStart() != null)
			model.setDTStart(c.getDtStart());
		
		if ( c.getDtEnt() != null)
			model.setDTEnd(c.getDtEnt());
		
		if ( c.getDtStamp() != null)
		model.setDTStamp(c.getDtStamp());

		model.setEventClass(c.getEventClass());
		model.setLocation(c.getLocation());

		// TODO finish marshalling of all available properties
		return model.getDocument();

	}

	public static ICalendarModel unmarshall(Document document)
			throws SyntaxException, IllegalArgumentException {

		if (document == null)
			throw new IllegalArgumentException("document == null");

		XCSDocumentParser model = new XCSDocumentParser(document);

		BasicVCalendarModel c = null;
		if (model.getComponentType().equals(ICALENDAR.VEVENT))
			c = new VEventModel();
		else if (model.getComponentType().equals(ICALENDAR.VTODO))
			c = new VTodoModel();
		else
			throw new IllegalArgumentException("unknown component type");

		if ( model.getId() == null) 
			throw new IllegalArgumentException("id == null");
		
		c.setId((String) model.getId());
		c.setDescription(model.getDescription());
		c.setSummary(model.getSummary());

		if ( model.getDTStart() != null)
			c.setDtStart(model.getDTStart());
		
		if ( model.getDTEnd() != null )
			c.setDtEnt(model.getDTEnd());
		
		if ( model.getDTStamp() != null)
			c.setDtStamp(model.getDTStamp());

		c.setEventClass(model.getEventClass());
		c.setLocation(model.getLocation());

		// TODO finish unmarshalling of all available properties

		return c;
	}

	// public static VEventModel createVEvent(Document doc)
	// throws SyntaxException, InvocationException {
	// if (doc == null)
	// throw new InvocationException("doc == null");
	//
	// BasicDocumentModel model = new BasicDocumentModel(doc);
	//
	// VEventModel c = new VEventModel();
	// c.setId((String) model.getId());
	//
	// c.setDescription(model.getDescription());
	//
	// // TODO createVEvent
	// return null;
	// }
	//
	// public static Document persistVEvent(VEventModel vEventComponent)
	// throws SyntaxException, InvocationException {
	// // TODO persistVEvent
	// return null;
	// }
	//
	// public static VTodoModel createVTodo(Document doc) throws
	// SyntaxException,
	// InvocationException {
	// if (doc == null)
	// throw new InvocationException("doc == null");
	//
	// // TODO createVTodo
	// return null;
	// }
	//
	// public static Document persistVTodo(VTodoModel vTodoComponent)
	// throws SyntaxException, InvocationException {
	// // TODO persistVTodo
	// return null;
	// }
	//
	// public static VFreeBusyModel createVFreeBusy(Document doc)
	// throws SyntaxException, InvocationException {
	// if (doc == null)
	// throw new InvocationException("doc == null");
	//
	// // TODO createVFreeBusy
	// return null;
	// }
}
