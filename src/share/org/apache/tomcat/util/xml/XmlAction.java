package org.apache.tomcat.util.xml;



/** Each rule in Xml Mapper can invoke certain actions.
    An action implementation will be notified for each matching rule
    on start and end of the tag that matches.

    After all end actions are called, a special cleanup call will allow
    actions to remove temporary data.
*/
public abstract class XmlAction {
    public void start( SaxContext ctx) throws Exception {
    }
    
    public void end( SaxContext ctx) throws Exception {
    }
    
    public void cleanup( SaxContext ctx) throws Exception {
    }
}

