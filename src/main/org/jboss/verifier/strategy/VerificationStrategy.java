package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.VerificationStrategy
 * Copyright (C) 2000  Juha Lindfors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: VerificationStrategy.java,v 1.3 2000/06/03 17:49:32 juha Exp $
 */


// standard imports
import java.util.Iterator;


// non-standard class dependencies
import org.gjt.lindfors.pattern.Strategy;


/**
 * << DESCRIBE THE CLASS HERE >>
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.3 $
 * @since  	JDK 1.3
 */
public interface VerificationStrategy extends Strategy {

    /*
     * Does the entity checks
     */
    abstract void checkEntities(Iterator entities);
    
    /*
     * Checks the sessions
     */
    abstract void checkSessions(Iterator sessions);
    
    /*
     * Checks the message driven beans (EJB 2.0 only).
     */
    abstract void checkMessageDriven(Iterator sessions);
    
}

