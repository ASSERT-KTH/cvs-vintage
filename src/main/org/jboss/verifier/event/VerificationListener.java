package org.jboss.verifier.event;

/*
 * Class org.jboss.verifier.event.VerificationListener
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
 * This package and its source code is available at www.gjt.org
 * $Id: VerificationListener.java,v 1.2 2000/08/20 20:48:08 juha Exp $
 *
 * You can reach the author by sending email to jpl@gjt.org or
 * directly to jplindfo@helsinki.fi.
 */


// standard imports
import java.util.EventListener;


// non-standard class dependencies


/**
 * Listener for verification events.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors
 * @version $Revision: 1.2 $
 * @since  	JDK 1.3
 */
public interface VerificationListener extends EventListener {

    public void beanChecked(VerificationEvent event);
    public void specViolation(VerificationEvent event);
    
}


