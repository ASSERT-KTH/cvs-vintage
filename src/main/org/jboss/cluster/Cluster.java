/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.cluster;

import java.io.*;
import java.net.*;
import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import javax.management.*;

import org.jboss.logging.Log;

/**
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class Cluster
   implements ClusterMBean, ClusterRemote, MBeanRegistration
{
   // Constants -----------------------------------------------------
   public static final String GROUP_ADDRESS = "224.0.0.1";
   public static final int PORT = 6789;
   public static final int HEARTBEAT_INTERVAL = 500;
   public static final int SWEEP_INTERVAL = 3000;
   public static final String DEFAULT_NAME = "Default";
   public static final int PACKET_LENGTH = 500;
    
   // Attributes ----------------------------------------------------
   HashMap nodes = new HashMap();
   boolean running = true;
   
   ClusterRemote master;
   
   MBeanServer server;
   
   String name = "Default";
   
   Log log = new Log("Cluster");
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void addNode(ClusterNode n)
   {
      nodes = (HashMap)nodes.clone();
      nodes.put(n.getNode(), n);
      log.log("Added "+n.getWhere() +" to cluster "+getName());
   }
   
   public void removeNode(ClusterNode n)
   {
      nodes = (HashMap)nodes.clone();
      nodes.remove(n.getNode());
      log.log("Removed "+n.getWhere() + " from cluster "+getName());
      
      if (n.getNode().equals(getMaster()))
      {
         setMaster(null);
         log.log("No cluster master");
      }
   }
   
   public ClusterNode getNode(ClusterRemote cr)
   {
      return (ClusterNode)nodes.get(cr);
   }
   
   public Iterator getNodes()
   {
      return nodes.values().iterator();
   }

   public ClusterRemote getMaster() { return master; }
   public synchronized void setMaster(ClusterRemote m) 
   { 
      master = m; 
      
      // Notify anyone waiting for master to be selected
      this.notifyAll(); 
   }
   
   public synchronized ClusterRemote getCurrentMaster() 
   { 
      // Wait for master to be set
      while (getMaster() == null)
      {
         try { this.wait(); } catch (InterruptedException e) {}
      }
      
      return getMaster();
   }
   
   public String getName() { return name; }

   public java.lang.Object invoke(ObjectName name,
                               java.lang.String actionName,
                               java.lang.Object[] params,
                               java.lang.String[] signature)
                        throws InstanceNotFoundException,
                               MBeanException,
                               ReflectionException
    {
       return server.invoke(name, actionName, params, signature);
    }
   
   // MBeanRegistration implementation ------------------------------
   public ObjectName preRegister(MBeanServer server, ObjectName name)
      throws java.lang.Exception
   {
      try
      {
         System.out.println("Cluster starting");
         
         this.server = server;
         
         if (name.getKeyProperty("name") != null)
            this.name = name.getKeyProperty("name");
            
         // Start cluster node
         UnicastRemoteObject.exportObject(this);
         
         Thread runner;
         runner = new Thread(new HeartBeat());
         runner.setPriority(Thread.MAX_PRIORITY);
         runner.start();
         runner = new Thread(new HeartBeatListener());
         runner.setPriority(Thread.NORM_PRIORITY);
         runner.start();
         runner = new Thread(new NodeSweeper());
         runner.setPriority(Thread.MIN_PRIORITY);
         runner.start();
         
         return name;
      } catch (Exception e)
      {
         e.printStackTrace();
         throw e;
      }
   }
   
   public void postRegister(java.lang.Boolean registrationDone) 
   {
      // Wait for master to be set
      getCurrentMaster();
      System.out.println("Cluster started");
   }
   
   public void preDeregister()
      throws java.lang.Exception 
   {
      running = false;
   }
   
   public void postDeregister() {}
    
   // Protected -----------------------------------------------------
   protected DatagramPacket createPacket(ClusterInfo ci, InetAddress group)
      throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream(PACKET_LENGTH);
      ObjectOutputStream out = new ObjectOutputStream(baos);
      out.writeObject(ci);
      out.close();
      byte[] bytes = baos.toByteArray();
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, group, PORT);
      
      return packet;
   }
  
   protected ClusterInfo createInfo(DatagramPacket dp)
      throws IOException, ClassNotFoundException
   {
      ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
      ObjectInputStream in = new ObjectInputStream(bais);
      return (ClusterInfo)in.readObject();
   }
   
   class HeartBeat
      implements Runnable
   {
      public void run()
      {
         try
         {
            MulticastSocket s = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            
            while(running)
            {
               DatagramPacket packet;
               packet = createPacket(new ClusterInfo((ClusterRemote)RemoteObject.toStub(Cluster.this), getName()), group);
               s.send(packet);
               
               try { Thread.sleep(HEARTBEAT_INTERVAL); } catch (InterruptedException e) {}
            }
         } catch (Exception e)
         {
            running = false;
            e.printStackTrace();
         }
      }
   }
   
   class HeartBeatListener
      implements Runnable
   {
      public void run()
      {
         try
         {
            MulticastSocket s = new MulticastSocket(PORT);
            InetAddress group = InetAddress.getByName(GROUP_ADDRESS);
            s.joinGroup(group);
            
            byte[] bytes = new byte[PACKET_LENGTH];
            while(running)
            {
               DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
               s.receive(packet);
               ClusterInfo ci = createInfo(packet);
               
               if (!ci.getName().equals(getName()))
                  continue; // Wrong cluster

               // Add or update node
               synchronized (nodes)
               {
                  ClusterNode current = getNode(ci.getNode());
                  ClusterNode node = new ClusterNode(packet.getAddress(), ci.getNode());
                  if (current == null)
                  {
                     addNode(node);
                  } else
                  {
                     current.setLastHeartBeat(System.currentTimeMillis());
                  }
               }
               
            }
         } catch (Exception e)
         {
            running = false;
            e.printStackTrace();
         }
      }
   }
   
   class NodeSweeper
      implements Runnable
   {
      public void run()
      {
         try
         {
            while(running)
            {
               try { Thread.sleep(SWEEP_INTERVAL); } catch (InterruptedException e) {}
               
               synchronized (nodes)
               {
                  Collection n = nodes.values();
                  Iterator enum = n.iterator();
                  long now = System.currentTimeMillis();
                  while(enum.hasNext())
                  {
                     ClusterNode node = (ClusterNode)enum.next();
                     
                     if (node.getLastHeartBeat() < (now-SWEEP_INTERVAL))
                     {
                        removeNode(node);
                     }
                  }
                  
                  // Check master
                  if (getMaster() == null)
                  {
                     // Check other nodes for master
                     Iterator nodes = getNodes();
                     search: while(nodes.hasNext())
                     {
                        ClusterNode node = (ClusterNode)nodes.next();
                        ClusterRemote nodeMaster = node.getNode().getMaster();
                        if (nodeMaster != null) // Is there a master elected?
                        {
                           if (getNode(nodeMaster) != null) // Do I know of this node?
                           {
                              log.log("Cluster master found");
                              setMaster(nodeMaster);
                              break search;
                           }
                        }
                     }
                     
                     // Set me as master if none was found
                     if (getMaster() == null)
                     {
                        log.log("Set as cluster master");
                        setMaster((ClusterRemote)RemoteObject.toStub(Cluster.this));
                     }
                  }
               }
            }
         } catch (Exception e)
         {
            e.printStackTrace();
            running = false;
         }
      }
   }
}

