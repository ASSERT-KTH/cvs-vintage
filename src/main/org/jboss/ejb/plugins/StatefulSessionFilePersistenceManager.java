/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;

import javax.ejb.Handle;
import javax.ejb.SessionBean;
import javax.ejb.RemoveException;
import javax.ejb.EJBException;

import org.jboss.ejb.Container;
import org.jboss.ejb.StatefulSessionContainer;
import org.jboss.ejb.StatefulSessionPersistenceManager;
import org.jboss.ejb.StatefulSessionEnterpriseContext;
import org.jboss.logging.Logger;


/**
*  StatefulSessionFilePersistenceManager
*
*  This class is one of the passivating plugins for JBoss.
*  It is fairly simple and can work from the file system from wich JBoss is operating
*
*  @see <related>
*  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
*  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
*  @author <a href="mailto:sebastien.alborini@m4x.org">Sebastien Alborini</a>
*  @version $Revision: 1.26 $
*/
public class StatefulSessionFilePersistenceManager
   implements StatefulSessionPersistenceManager
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
   private static Logger log = Logger.getLogger(StatefulSessionFilePersistenceManager.class);
    private StatefulSessionContainer con;
    
    private Method ejbActivate;
    private Method ejbPassivate;
    private Method ejbRemove;
    
    private File dir;
    
    private ArrayList fields;
    
    // Static --------------------------------------------------------
    private static long id = System.currentTimeMillis();
    
    // Constructors --------------------------------------------------
    
    // Public --------------------------------------------------------
    public void setContainer(Container c)
    {
        con = (StatefulSessionContainer)c;
    }
    
    public void init()
    throws Exception
    {
        
        // Find methods
        ejbActivate = SessionBean.class.getMethod("ejbActivate", new Class[0]);
        
        ejbPassivate = SessionBean.class.getMethod("ejbPassivate", new Class[0]);
        
        ejbRemove = SessionBean.class.getMethod("ejbRemove", new Class[0]);
        
        // Initialize the dataStore
        String ejbName = con.getBeanMetaData().getEjbName();
        
        // Base dir
        File homeDir = new File(System.getProperty("jboss.system.home"));
        File databaseDir = new File(homeDir, "db"+File.separator);
        File database = new File(databaseDir, "sessions");
        
        dir = new File(database, ejbName);
        
        dir.mkdirs();
        
        log.debug("Storing sessions for "+ejbName+" in:"+dir);
        
        // Clear dir of old files
        File[] sessions = dir.listFiles();
        for (int i = 0; i < sessions.length; i++)
        {
            sessions[i].delete();
        }
        log.debug(sessions.length + " old sessions removed");
        
        // Get fields of class
        Class beanClass = con.getBeanClass();
        fields = new ArrayList();
        while (!beanClass.equals(Object.class))
        {
            Field[] f = beanClass.getDeclaredFields();
            
            // Skip transient fields
            for (int i = 0; i < f.length; i++)
                if (!Modifier.isTransient(f[i].getModifiers()))
            {
                fields.add(f[i]);
            }
            
            beanClass = beanClass.getSuperclass();
        }
    }
    
    public void start()
    throws Exception
    {
    }
    
    public void stop()
    {
    }
    
    public void destroy()
    {
    }
    
    public void createSession(Method m, Object[] args, StatefulSessionEnterpriseContext ctx)
    throws Exception
    {
        
        // Set id
        ctx.setId(nextId());
        
        // Get methods
        try
        {
            Method createMethod = con.getBeanClass().getMethod("ejbCreate", m.getParameterTypes());
            
            // Call ejbCreate
            createMethod.invoke(ctx.getInstance(), args);
        
        } catch (IllegalAccessException e)
        {
            // Clear id
            ctx.setId(null);
            
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
        } catch (InvocationTargetException ite)
        {
            // Clear id
            ctx.setId(null);
            
            Throwable e = ite.getTargetException();
            if (e instanceof EJBException)
            {
                // Rethrow exception
                throw (EJBException)e;
            }
            else if (e instanceof RuntimeException)
            {
                // Wrap runtime exceptions
                throw new EJBException((Exception)e);
            }
            else if (e instanceof Exception)
            {
                // Remote, Create, or custom app. exception
                throw (Exception)e;
            }
            else
            {
                throw (Error)e;
            }
        }
        
        // Insert in cache
        ((StatefulSessionContainer)con).getInstanceCache().insert(ctx);
        
        // Create EJBObject
        if (con.getContainerInvoker() != null)
            ctx.setEJBObject(con.getContainerInvoker().getStatefulSessionEJBObject(ctx.getId()));
        // Create EJBLocalObject
        if (con.getLocalHomeClass() != null)
            ctx.setEJBLocalObject(con.getLocalContainerInvoker().getStatefulSessionEJBLocalObject(ctx.getId()));
    }
    
    public void activateSession(StatefulSessionEnterpriseContext ctx)
    throws RemoteException
    {
        try
        {
            ObjectInputStream in;
            
            
            // Load state
            in = new SessionObjectInputStream(ctx, new FileInputStream(getFile(ctx.getId())));
            
            ctx.setInstance(in.readObject());
            
            in.close();
            
            removePassivated(ctx.getId());
            
            // Call bean
            ejbActivate.invoke(ctx.getInstance(), new Object[0]);
        
        } catch (ClassNotFoundException e)
        {
            throw new ServerException("Could not activate", e);
        } catch (IOException e)
        {
            throw new ServerException("Could not activate", e);
        } catch (IllegalAccessException e)
        {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
        } catch (InvocationTargetException ite)
        {
            Throwable e = ite.getTargetException();
            if (e instanceof EJBException)
            {
                // Rethrow exception
                throw (EJBException)e;
            }
            else if (e instanceof RuntimeException)
            {
                // Wrap runtime exceptions
                throw new EJBException((Exception)e);
            }
            else if (e instanceof RemoteException)
            {
                // Remote, Create, or custom app. exception
                throw (RemoteException)e;
            }
            else
            {
                throw (Error)e;
            }
        }
    }
    
    public void passivateSession(StatefulSessionEnterpriseContext ctx)
    throws RemoteException
    {
        try
        {
            // Call bean
            ejbPassivate.invoke(ctx.getInstance(), new Object[0]);
            
            // Store state
            ObjectOutputStream out = new SessionObjectOutputStream(new FileOutputStream(getFile(ctx.getId())));
            
            out.writeObject(ctx.getInstance());
            
            out.close();
        
        } catch (IOException e)
        {
            throw new ServerException("Could not passivate", e);
        }catch (IllegalAccessException e)
        {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
        } catch (InvocationTargetException ite)
        {
            Throwable e = ite.getTargetException();
            if (e instanceof EJBException)
            {
                // Rethrow exception
                throw (EJBException)e;
            }
            else if (e instanceof RuntimeException)
            {
                // Wrap runtime exceptions
                throw new EJBException((Exception)e);
            }
            else if (e instanceof RemoteException)
            {
                // Remote, Create, or custom app. exception
                throw (RemoteException)e;
            }
            else
            {
                throw (Error)e;
            }
        }
    }
    
    public void removeSession(StatefulSessionEnterpriseContext ctx)
    throws RemoteException, RemoveException
    {
        // Call bean
        try
        {
            ejbRemove.invoke(ctx.getInstance(), new Object[0]);
        
        } catch (IllegalAccessException e)
        {
            // Throw this as a bean exception...(?)
            throw new EJBException(e);
        } catch (InvocationTargetException ite)
        {
            Throwable e = ite.getTargetException();
            if (e instanceof EJBException)
            {
                // Rethrow exception
                throw (EJBException)e;
            }
            else if (e instanceof RuntimeException)
            {
                // Wrap runtime exceptions
                throw new EJBException((Exception)e);
            }
            else if (e instanceof RemoveException)
            {
                throw (RemoveException)e;
            }
            else if (e instanceof RemoteException)
            {
                throw (RemoteException)e;
            }
            else
            {
                throw (Error)e;
            }
        }
    }
    
    public void removePassivated(Object key)
    {
        // OK also if the file does not exists
        getFile(key).delete();
    }
    
    // Z implementation ----------------------------------------------
    
    // Package protected ---------------------------------------------
    
    // Protected -----------------------------------------------------
    protected Long nextId()
    {
        return new Long(id++);
    }
    
    protected File getFile(Object key)
    {
        return new File(dir, key + ".ser");
    }
    
    // Private -------------------------------------------------------
    
    // Inner classes -------------------------------------------------
}


