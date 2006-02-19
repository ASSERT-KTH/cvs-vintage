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
package org.columba.calendar.parser;

import org.columba.calendar.model.Event;
import org.columba.calendar.model.api.ICALENDAR;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.model.api.IComponent.TYPE;
import org.jdom.Document;

public final class VCalendarModelFactory {

	public VCalendarModelFactory() {
		super();
	}

	public static Document marshall(IComponent c) throws SyntaxException,
			IllegalArgumentException {

		if (c == null)
			throw new IllegalArgumentException("calendarmodel == null");

		XCSDocumentParser model = new XCSDocumentParser(c.getType());

		model.setId(c.getId());

		if (c.getType() == TYPE.EVENT) {
			IEvent event = (IEvent) c;
			model.setDescription(event.getDescription());
			model.setSummary(event.getSummary());

			if (event.getDtStart() != null)
				model.setDTStart(event.getDtStart());

			if (event.getDtEnt() != null)
				model.setDTEnd(event.getDtEnt());

			if (event.getDtStamp() != null)
				model.setDTStamp(event.getDtStamp());

			model.setEventClass(event.getEventClass());
			model.setLocation(event.getLocation());
			
			model.setCalendar(event.getCalendar());
		}

		// TODO finish marshalling of all available properties
		return model.getDocument();

	}

	public static IComponent unmarshall(Document document)
			throws SyntaxException, IllegalArgumentException {

		if (document == null)
			throw new IllegalArgumentException("document == null");

		XCSDocumentParser model = new XCSDocumentParser(document);

		if (model.getId() == null)
			throw new IllegalArgumentException("id == null");

		if (model.getComponentType().equals(ICALENDAR.VEVENT)) {
			IEvent event = new Event((String) model.getId());

			event.setDescription(model.getDescription());
			event.setSummary(model.getSummary());

			if (model.getDTStart() != null)
				event.setDtStart(model.getDTStart());

			if (model.getDTEnd() != null)
				event.setDtEnt(model.getDTEnd());

			if (model.getDTStamp() != null)
				event.setDtStamp(model.getDTStamp());

			event.setEventClass(model.getEventClass());
			event.setLocation(model.getLocation());

			event.setCalendar(model.getCalendar());
			
			// TODO finish unmarshalling of all available properties

			return event;
		}

		else
			throw new IllegalArgumentException("unknown component type");

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
