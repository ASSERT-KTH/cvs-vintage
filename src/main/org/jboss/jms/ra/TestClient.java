/*
 * Copyright (c) 2001 Peter Antman Tim <peter.antman@tim.se>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jboss.jms.ra;

import javax.jms.*;
import javax.naming.InitialContext;

import org.jboss.jms.ra.client.*;
/**
 * TestClient for stand alone use. Basically verry uninteresting.
 *
 *
 * Created: Sun Apr 22 19:10:27 2001
 *
 * @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 * @version $Revision: 1.2 $
 */

public class TestClient {
    
    public TestClient() {
	
    }
    
    public static void main(String[] args) {
	try {
            JmsManagedConnectionFactory f = new JmsManagedConnectionFactory();
            f.setJmsProviderAdapter( new org.jboss.jms.jndi.JBossMQProvider());
            //f.setLogging("true");
            JmsConnectionFactory cf = (JmsConnectionFactory)f.createConnectionFactory();

	    //FIXME - how to get LocalTransaction for standalone usage?
	    TopicConnection con = cf.createTopicConnection();
            TopicSession ses = con.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
	    Topic topic = (Topic)new InitialContext().lookup("topic/testTopic");


	    TopicPublisher pub = ses.createPublisher(topic);

	    TextMessage m = ses.createTextMessage("Hello world!");
	    pub.publish(m);
	    ses.commit();

            ses.close();


        }catch(Exception ex) {
            System.err.println("Error: " + ex);
            ex.printStackTrace();
        }
    }
    
} // TestClient
