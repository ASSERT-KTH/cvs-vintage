package test.jboss.minerva.factories;

import java.io.PrintWriter;
import java.sql.*;
import java.util.*;
import junit.framework.*;
import org.jboss.minerva.pools.*;
import org.jboss.minerva.factories.JDBCConnectionFactory;
import org.jboss.minerva.jdbc.*;
import test.jboss.testdb.*;

/**
 * Tests for JDBCConnectionFactory.  Currently a work in progress.  Uses the
 * test database driver so it doesn't rely on any particular real database
 * being present.
 * @version $Revision: 1.3 $
 * @author <a href="mailto:ammulder@alumni.princeton.edu">Aaron Mulder</a>
 */
public class JDBCFactoryTest extends TestCase {
    private final static int MAX_ITERATIONS = 10000;
    ObjectPool pool;
    private JDBCConnectionFactory factory;
    private String url;

    public JDBCFactoryTest(String s) {
        super(s);
        try {
            Class.forName("test.jboss.testdb.TestDBDriver");
        } catch(Throwable t) {
            t.printStackTrace();
        }
    }

    public void setUp() {
        factory = new JDBCConnectionFactory();
        url = "jdbc:testdb:test";
        pool = new ObjectPool();
    }

// Test parameters
    public void testParamURL() {
        factory.setConnectURL(url);
        assert(factory.getConnectURL().equals(url));
        factory.setConnectURL(url+url);
        assert(factory.getConnectURL().equals(url+url));
    }

    public void testParamProperties() {
        Properties props = new Properties();
        factory.setConnectProperties(props);
        assert(factory.getConnectProperties().equals(props));
        props = new Properties();
        props.setProperty("name", "value");
        factory.setConnectProperties(props);
        assert(factory.getConnectProperties().equals(props));
    }

    public void testParamUser() {
        factory.setUser("user");
        assert(factory.getUser().equals("user"));
        factory.setUser("other");
        assert(factory.getUser().equals("other"));
    }

    public void testParamPassword() {
        factory.setPassword("pw");
        assert(factory.getPassword().equals("pw"));
        factory.setPassword("password");
        assert(factory.getPassword().equals("password"));
    }

// Test initialization
    public void testNoParams() {
        try {
            factory.poolStarted(pool, null);
            fail("Factory started with no parameters!");
        } catch(IllegalStateException e) {}
    }

    public void testNoPool() {
        factory.setConnectURL(url);
        try {
            factory.poolStarted(null, new PrintWriter(System.out));
            fail("Factory started with null pool!");
        } catch(IllegalArgumentException e) {}
    }

    public void testCreateAndDelete() {
        factory.setConnectURL(url);
        factory.poolStarted(pool, null);
        Vector v = new Vector();
        for(int i = 0; i<MAX_ITERATIONS; i++) {
            Object o = factory.createObject();
            assert(o instanceof TestConnection);
            v.addElement(o);
        }
        for(int i = v.size()-1; i >= 0; i--) {
            try {
                assert(!((Connection)v.elementAt(i)).isClosed());
            } catch(SQLException e) {
                fail(e.getMessage());
            }
            factory.deleteObject(v.elementAt(i));
        }
        for(int i = v.size()-1; i >= 0; i--)
            try {
                assert(((Connection)v.elementAt(i)).isClosed());
            } catch(SQLException e) {
                fail(e.getMessage());
            }
        factory.poolClosing(pool);
    }

    public void testPrepareAndReturn() {
        factory.setConnectURL(url);
        factory.poolStarted(pool, null);
        Vector v = new Vector();
        HashMap map = new HashMap();
        for(int i = 0; i<MAX_ITERATIONS; i++) {
            Object o = factory.createObject();
            v.addElement(o);
        }
        for(int i = v.size()-1; i >= 0; i--) {
            Object src = v.elementAt(i);
            assert(src != null);
            Object trans = factory.prepareObject(src);
            assert(trans instanceof ConnectionInPool);
            map.put(trans, src);
        }
        assert(map.size() == v.size());
        for(Iterator it = new HashSet(map.keySet()).iterator(); it.hasNext();) {
            Object trans = it.next();
            Object orig = factory.returnObject(trans);
            assert(orig == map.get(trans));
            map.remove(trans);
        }
        assert(map.size() == 0);
        factory.poolClosing(pool);
    }
}