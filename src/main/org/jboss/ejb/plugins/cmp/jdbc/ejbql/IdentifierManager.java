package org.jboss.ejb.plugins.cmp.jdbc.ejbql;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;

public class IdentifierManager {
   private final Map pathElements = new Hashtable();
   private final Map identifiersByPathElement = new Hashtable();
   private final Map tableAliases = new Hashtable();
   private final Map relationTableAliases = new Hashtable();
   private int aliasCount = 1;
   private String aliasHeaderPrefix;
   private String aliasHeaderSuffix;
   private int aliasMaxLength;
   
   public IdentifierManager(JDBCTypeMappingMetaData typeMapping) {
      aliasHeaderPrefix = typeMapping.getAliasHeaderPrefix();
      aliasHeaderSuffix = typeMapping.getAliasHeaderSuffix();
      aliasMaxLength = typeMapping.getAliasMaxLength();
   }

   public IdentifierManager(IdentifierManager target) {
      pathElements.putAll(target.pathElements);
      identifiersByPathElement.putAll(target.identifiersByPathElement);
      tableAliases.putAll(target.tableAliases);
      relationTableAliases.putAll(target.relationTableAliases);
      
      aliasCount = target.aliasCount;
      aliasHeaderPrefix = target.aliasHeaderPrefix;
      aliasHeaderSuffix = target.aliasHeaderSuffix;
      aliasMaxLength = target.aliasMaxLength;
   }

   public AbstractSchema getAbstractSchema(String identifier) {
      PathElement pathElement = getPathElement(identifier);
      if(pathElement instanceof AbstractSchema) {
         return (AbstractSchema)pathElement;
      }
      return null;
   }

   public AbstractSchema getExistingAbstractSchema(String identifier) {
      PathElement pathElement = getExistingPathElement(identifier);
      if(pathElement instanceof AbstractSchema) {
         return (AbstractSchema)pathElement;
      } else {
         throw new IllegalArgumentException("Path element with identifier is not an instance of AbstractSchema: " +
            " identifier=" + identifier + 
            ", pathElement=" + pathElement);
      }
   }

   public CMRField getCMRField(String path) {
      PathElement pathElement = getPathElement(path);
      if(pathElement instanceof CMRField) {
         return (CMRField)pathElement;
      } 
      return null;
   }
   
   public CMRField getExistingCMRField(String path) {
      PathElement pathElement = getExistingPathElement(path);
      if(pathElement instanceof CMRField) {
         return (CMRField)pathElement;
      } else {
         throw new IllegalArgumentException("Path element at path is not an instance of CMRField: " +
            " path=" + path + 
            ", pathElement=" + pathElement);
      }
   }
   
   public EntityPathElement getEntityPathElement(String path) {
      PathElement pathElement = getPathElement(path);
      if(pathElement instanceof EntityPathElement) {
         return (EntityPathElement)pathElement;
      } 
      return null;
   }
   
   public EntityPathElement getExistingEntityPathElement(String path) {
      PathElement pathElement = getExistingPathElement(path);
      if(pathElement instanceof EntityPathElement) {
         return (EntityPathElement)pathElement;
      } else {
         throw new IllegalArgumentException("Path element at path is not an instance of EntityPathElement: " +
            " path=" + path + 
            ", pathElement=" + pathElement);
      }
   }
   
   public CMPField getCMPField(String path) {
      PathElement pathElement = getPathElement(path);
      if(pathElement instanceof CMPField) {
         return (CMPField)pathElement;
      } 
      return null;
   }
   
   public CMPField getExistingCMPField(String path) {
      PathElement pathElement = getExistingPathElement(path);
      if(pathElement instanceof CMPField) {
         return (CMPField)pathElement;
      } else {
         throw new IllegalArgumentException("Path element at path is not an instance of CMPField: " +
            " path=" + path + 
            ", pathElement=" + pathElement);
      }
   }
   
   public PathElement getPathElement(String path) {
      return (PathElement)pathElements.get(path);
   }
   
   public PathElement getExistingPathElement(String path) {
      PathElement pathElement = (PathElement)pathElements.get(path);
      if(pathElement != null) {
         return pathElement;
      }
      throw new IllegalArgumentException("Unknown path: "+path);
   }
   
   public boolean isKnownPath(String path) {
      return pathElements.containsKey(path);
   }
   
   public boolean isIdentifierRegistered(String identifier) {
      if(identifier.indexOf(".")>=0) {
         throw new IllegalArgumentException("identifier is a path not an identifier: " + identifier);
      }
      return pathElements.containsKey(identifier);
   }
   
   public void registerIdentifier(CMRField collectionValuedCMRField, String identifier) {
      if(identifier.indexOf(".")>=0) {
         throw new IllegalArgumentException("identifier is a path not an identifier: " + identifier);
      }      
      if(collectionValuedCMRField == null) {
         throw new IllegalArgumentException("collectionValuedCMRField is null");
      }
      if(!collectionValuedCMRField.isCollectionValued()) {
         throw new IllegalArgumentException("collectionValuedCMRField is single valued");
      }
      addMapping(identifier, collectionValuedCMRField);
   }

   public void registerIdentifier(AbstractSchema abstractSchema, String identifier) {
      if(identifier.indexOf(".")>=0) {
         throw new IllegalArgumentException("identifier is a path not an identifier: " + identifier);
      }
      if(abstractSchema == null) {
         throw new IllegalArgumentException("abstractSchema is null");
      }
      addMapping(identifier, abstractSchema);
   }

   public void registerPath(PathElement pathElement, String path) {
      if(path.indexOf(".")<0) {
         throw new IllegalArgumentException("path is an identifier not a path: " + path);
      }
      if(pathElement == null) {
         throw new IllegalArgumentException("pathElement is null");
      }
      addMapping(path, pathElement);
   }
      
   public String getTableAlias(EntityPathElement pathElement) {
      if(tableAliases.containsKey(pathElement)) {
         return (String)tableAliases.get(pathElement);
      }
      
      StringBuffer buf = new StringBuffer();

      EntityPathElement parent = pathElement;
      while(parent != null) {
         // if this is not the first in loop prepend an underscore
         if(buf.length() > 0) {
            buf.insert(0, "_");
         }
         
         // try to get the identifier of this path
         String identifier = (String)identifiersByPathElement.get(parent);
         if(identifier != null) {
            // we got the final identifier
            buf.insert(0, identifier);         
            // add the prefix
            buf.insert(0, getNextAliasHeader());
            // add the alias to the map
            String alias = buf.toString();
            alias = alias.substring(0, Math.min(aliasMaxLength, alias.length()));
            tableAliases.put(pathElement, alias);
            
            return alias;
         } else if(pathElement instanceof AbstractSchema) {
            // when path element is an instance of abstract schema
            // we should get an identifier in the first iteration
            throw new IllegalStateException("No registered identifier for AbstractSchema: "+pathElement);
         }
         
         // prepend the parent's name
         buf.insert(0, parent.getName());
         
         // move up the parent chain
         parent = parent.getParent();
      }
      // should never happen...
      throw new IllegalStateException("No root identifier for path element: "+pathElement); 
   }
   
   public String getRelationTableAlias(CMRField cmrField) {
      if(relationTableAliases.containsKey(cmrField)) {
         return (String)tableAliases.get(cmrField);
      }
      
      String alias = getNextAliasHeader() + getTableAlias(cmrField.getParent()) + "_to_" + cmrField.getName();
      alias = alias.substring(0, Math.min(aliasMaxLength, alias.length()));
      relationTableAliases.put(alias, cmrField);
      return alias;
   }
   
   private String getNextAliasHeader() {
      StringBuffer buf = new StringBuffer(aliasHeaderPrefix.length() + 2 + aliasHeaderSuffix.length());
      buf.append(aliasHeaderPrefix).append(aliasCount++).append(aliasHeaderSuffix);
      return buf.toString();
   }
   
   public Set getUniqueEntityPathElements() {
      Set set = new HashSet();
      for(Iterator i = pathElements.values().iterator(); i.hasNext(); ) {
         Object o = i.next();
         if(o instanceof EntityPathElement) {
            set.add(o);
         }
      }
      return set;
   }
   
   public Set getUniqueCMRFields() {
      Set set = new HashSet();
      for(Iterator i = pathElements.values().iterator(); i.hasNext(); ) {
         Object o = i.next();
         if(o instanceof CMRField) {
            set.add(o);
         }
      }
      return set;
   }   

   private void addMapping(String path, PathElement pathElement) {
      pathElements.put(path, pathElement);
      
      // is this path just an identifier
      if(path.indexOf(".")<0) {
         identifiersByPathElement.put(pathElement, path);
      }
   }
}
