/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.ejb.plugins.cmp.ejbql.*;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.logging.Logger;

/**
 * Compiles EJB-QL and JBossQL into SQL using OUTER and INNER joins.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.3 $
 */
public final class EJBQLToSQL92Compiler
   implements QLCompiler, JBossQLParserVisitor
{
   private static final Logger log = Logger.getLogger(EJBQLToSQL92Compiler.class);

   // input objects
   private final Catalog catalog;
   private Class returnType;
   private Class[] parameterTypes;
   private JDBCReadAheadMetaData readAhead;

   // alias info
   private AliasManager aliasManager;
   private Map leftJoinPaths = new HashMap();
   private Map innerJoinPaths = new HashMap();
   private Map identifierToTable = new HashMap();

   // mapping metadata
   private JDBCTypeMappingMetaData typeMapping;
   private JDBCTypeFactory typeFactory;

   // output objects
   private boolean forceDistinct;
   private String sql;
   private int offsetParam;
   private int offsetValue;
   private int limitParam;
   private int limitValue;
   private JDBCStoreManager selectManager;
   private Object selectObject;
   private List inputParameters = new ArrayList();

   private List leftJoinCMRList = new ArrayList();
   private StringBuffer onFindCMRJoin;

   private boolean countCompositePk;

   public EJBQLToSQL92Compiler(Catalog catalog)
   {
      this.catalog = catalog;
   }

   public void compileEJBQL(String ejbql, Class returnType, Class[] parameterTypes, JDBCReadAheadMetaData readAhead)
      throws Exception
   {
      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser
      EJBQLParser parser = new EJBQLParser(new StringReader(""));

      try
      {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new StringBuffer()).toString();
      }
      catch(Exception e)
      {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      }
      catch(Error e)
      {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   public void compileJBossQL(String ejbql, Class returnType, Class[] parameterTypes, JDBCReadAheadMetaData readAhead)
      throws Exception
   {
      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser
      JBossQLParser parser = new JBossQLParser(new StringReader(""));

      try
      {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new StringBuffer()).toString();

         log.debug("ejbql: " + ejbql);
         log.debug("sql: " + sql);
      }
      catch(Exception e)
      {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      }
      catch(Error e)
      {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   private void reset()
   {
      returnType = null;
      parameterTypes = null;
      readAhead = null;
      inputParameters.clear();
      selectObject = null;
      selectManager = null;
      typeFactory = null;
      typeMapping = null;
      aliasManager = null;
      forceDistinct = false;
      limitParam = 0;
      limitValue = 0;
      offsetParam = 0;
      offsetValue = 0;
      leftJoinCMRList.clear();
      onFindCMRJoin = null;
      countCompositePk = false;
      leftJoinPaths.clear();
      innerJoinPaths.clear();
      identifierToTable.clear();
   }

   public String getSQL()
   {
      return sql;
   }

   public int getOffsetValue()
   {
      return offsetValue;
   }

   public int getOffsetParam()
   {
      return offsetParam;
   }

   public int getLimitValue()
   {
      return limitValue;
   }

   public int getLimitParam()
   {
      return limitParam;
   }

   public boolean isSelectEntity()
   {
      return selectObject instanceof JDBCEntityBridge;
   }

   public JDBCEntityBridge getSelectEntity()
   {
      return (JDBCEntityBridge) selectObject;
   }

   public boolean isSelectField()
   {
      return selectObject instanceof JDBCCMPFieldBridge;
   }

   public JDBCCMPFieldBridge getSelectField()
   {
      return (JDBCCMPFieldBridge) selectObject;
   }

   public SelectFunction getSelectFunction()
   {
      return (SelectFunction) selectObject;
   }

   public JDBCStoreManager getStoreManager()
   {
      return selectManager;
   }

   public List getInputParameters()
   {
      return inputParameters;
   }

   public List getLeftJoinCMRList()
   {
      return leftJoinCMRList;
   }

   public Object visit(SimpleNode node, Object data)
   {
      throw new RuntimeException(
         "Internal error: Found unknown node type in " +
         "EJB-QL abstract syntax tree: node=" + node
      );
   }

   private void setTypeFactory(JDBCTypeFactory typeFactory)
   {
      this.typeFactory = typeFactory;
      this.typeMapping = typeFactory.getTypeMapping();
      aliasManager = new AliasManager(
         typeMapping.getAliasHeaderPrefix(),
         typeMapping.getAliasHeaderSuffix(),
         typeMapping.getAliasMaxLength()
      );
   }

   private Class getParameterType(int index)
   {
      int zeroBasedIndex = index - 1;
      Class[] params = parameterTypes;
      if(zeroBasedIndex < params.length)
      {
         return params[zeroBasedIndex];
      }
      return null;
   }

   // verify that parameter is the same type as the entity
   private void verifyParameterEntityType(int number, JDBCEntityBridge entity)
   {
      Class parameterType = getParameterType(number);
      Class remoteClass = entity.getMetaData().getRemoteClass();
      Class localClass = entity.getMetaData().getLocalClass();
      if((localClass == null || !localClass.isAssignableFrom(parameterType)) &&
         (remoteClass == null || !remoteClass.isAssignableFrom(parameterType)))
      {
         throw new IllegalStateException(
            "Only like types can be compared: from entity=" +
            entity.getEntityName() + " to parameter type=" + parameterType
         );
      }
   }

   public Object visit(ASTEJBQL node, Object data)
   {
      Node select = node.jjtGetChild(0);
      Node from = node.jjtGetChild(1);

      // compile select
      StringBuffer selectClause = new StringBuffer(50);
      select.jjtAccept(this, selectClause);

      final int childrenTotal = node.jjtGetNumChildren();

      // compile where
      StringBuffer whereClause = null;
      if(childrenTotal > 2)
      {
         whereClause = new StringBuffer(20);
         Node where = node.jjtGetChild(2);
         where.jjtAccept(this, whereClause);
      }

      // compile from
      StringBuffer fromClause = new StringBuffer(30);
      from.jjtAccept(this, fromClause);

      // left-join
      for(Iterator iter = identifierToTable.entrySet().iterator(); iter.hasNext();)
      {
         final Map.Entry entry = (Map.Entry) iter.next();
         final String identifier = (String) entry.getKey();
         final String table = (String) entry.getValue();
         final String alias = aliasManager.getAlias(identifier);

         fromClause.append(table).append(' ').append(alias);
         join(alias, fromClause);

         if(iter.hasNext())
         {
            fromClause.append(SQLUtil.COMMA);
         }
      }

      // assemble sql
      StringBuffer sql = (StringBuffer) data;
      sql.append(selectClause)
         .append(fromClause);
      if(whereClause != null && whereClause.length() > 0)
      {
         sql.append(SQLUtil.WHERE).append(whereClause);
      }

      return data;
   }

   public Object visit(ASTOrderBy node, Object data)
   {
      log.debug("ASTOrderBy>");
      return data;
   }

   public Object visit(ASTOrderByPath node, Object data)
   {
      log.debug("ASTOrderByPath>");
      return data;
   }

   public Object visit(ASTLimitOffset node, Object data)
   {
      log.debug("ASTLimitOffset>");
      return data;
   }

   public Object visit(ASTSelect select, Object data)
   {
      log.debug("ASTSelect> started");

      StringBuffer sql = (StringBuffer) data;

      sql.append(SQLUtil.SELECT);
      if(select.distinct)
      {
         sql.append(SQLUtil.DISTINCT);
      }

      final Node child0 = select.jjtGetChild(0);
      if(child0 instanceof ASTPath)
      {
         ASTPath path = (ASTPath) child0;

         if(path.isCMPField())
         {
            throw new IllegalStateException("selecting CMP fields is not supported yet.");
         }
         else
         {
            JDBCEntityBridge selectEntity = (JDBCEntityBridge) path.getEntity();
            setTypeFactory(selectEntity.getManager().getJDBCTypeFactory());
            selectManager = selectEntity.getManager();
            selectObject = selectEntity;

            final String alias = aliasManager.getAlias(path.getPath());
            SQLUtil.getColumnNamesClause(
               selectEntity.getPrimaryKeyFields(),
               alias,
               sql
            );

            addLeftJoinPath(path);
         }
      }
      else
      {
         throw new IllegalStateException("Aggregate functions are not yet supported.");
      }

      log.debug("ASTSelect> finished");
      return data;
   }

   public Object visit(ASTWhere node, Object data)
   {
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTOr node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); ++i)
      {
         buf.append(SQLUtil.OR);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTWhereConditionalTerm node, Object data)
   {
      log.debug("Where term> started node");
      for(int i = 0; i < node.jjtGetNumChildren(); ++i)
      {
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      log.debug("Where term> finished");
      return data;
   }

   public Object visit(ASTAnd node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(SQLUtil.AND);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNot node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(SQLUtil.NOT);
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTConditionalParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTBetween node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.BETWEEN);
      node.jjtGetChild(1).jjtAccept(this, data);
      buf.append(SQLUtil.AND);
      node.jjtGetChild(2).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTIn node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.IN).append('(');
      node.jjtGetChild(1).jjtAccept(this, data);
      for(int i = 2; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(SQLUtil.COMMA);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      buf.append(')');
      return data;
   }

   public Object visit(ASTLike node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.LIKE);
      node.jjtGetChild(1).jjtAccept(this, data);
      if(node.jjtGetNumChildren() == 3)
      {
         buf.append(SQLUtil.ESCAPE);
         node.jjtGetChild(2).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNullComparison node, Object data)
   {
      StringBuffer sql = (StringBuffer) data;

      final Node child0 = node.jjtGetChild(0);
      if(child0 instanceof ASTPath)
      {
         ASTPath path = (ASTPath) child0;
         addLeftJoinPath(path);

         JDBCFieldBridge field = (JDBCFieldBridge) path.getField();

         if(field.getJDBCType() == null)
         {
            JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge) field;
            final JDBCCMPFieldBridge[] keyFields;
            if(cmrField.getRelationMetaData().isTableMappingStyle())
            {
               keyFields = cmrField.getTableKeyFields();
            }
            else
            {
               keyFields = (
                  cmrField.hasForeignKey()
                  ? cmrField.getForeignKeyFields()
                  : cmrField.getRelatedCMRField().getForeignKeyFields()
                  );
            }

            String alias = aliasManager.getAlias(path.getPath());
            SQLUtil.getIsNullClause(node.not, keyFields, alias, sql);
         }
         else
         {
            String alias = aliasManager.getAlias(path.getPath(path.size() - 2));
            SQLUtil.getIsNullClause(node.not, field, alias, sql);
         }
      }
      else if(child0 instanceof ASTParameter)
      {
         ASTParameter param = (ASTParameter) child0;
         Class type = getParameterType(param.number);

         QueryParameter queryParam = new QueryParameter(
            param.number - 1,
            false, // isPrimaryKeyParameter
            null, // field
            null, // parameter
            typeFactory.getJDBCTypeForJavaType(type)
         );
         inputParameters.add(queryParam);

         sql.append("? IS ");
         if(node.not)
         {
            sql.append(SQLUtil.NOT);
         }
         sql.append(SQLUtil.NULL);
      }
      else
      {
         throw new IllegalStateException("Unexpected node in IS NULL clause: " + node);
      }

      return data;
   }

   public Object visit(ASTIsEmpty node, Object data)
   {
      ASTPath path = (ASTPath) node.jjtGetChild(0);
      if(!path.isCMRField())
      {
         throw new IllegalStateException("IS EMPTY can be applied only to collection valued CMR field.");
      }

      addLeftJoinPath(path);

      StringBuffer sql = (StringBuffer) data;
      JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge) path.getCMRField();
      JDBCEntityBridge relatedEntity = cmrField.getRelatedJDBCEntity();
      String alias = aliasManager.getAlias(path.getPath());
      SQLUtil.getIsNullClause(node.not, relatedEntity.getPrimaryKeyFields(), alias, sql);

      return data;
   }

   public Object visit(ASTMemberOf node, Object data)
   {
      Node member = node.jjtGetChild(0);
      ASTPath colPath = (ASTPath) node.jjtGetChild(1);
      String colAlias = aliasManager.getAlias(colPath.getPath());
      JDBCEntityBridge colEntity = (JDBCEntityBridge) colPath.getEntity();

      addLeftJoinPath(colPath);

      StringBuffer sql = (StringBuffer) data;
      if(node.not)
      {
         sql.append('(').append(SQLUtil.NOT).append('(');
      }

      if(member instanceof ASTParameter)
      {
         ASTParameter toParam = (ASTParameter) member;
         verifyParameterEntityType(toParam.number, colEntity);
         inputParameters.addAll(QueryParameter.createParameters(toParam.number - 1, colEntity));

         SQLUtil.getWhereClause(colEntity.getPrimaryKeyFields(), colAlias, sql);
      }
      else if(member instanceof ASTPath)
      {
         ASTPath memberPath = (ASTPath) member;
         JDBCEntityBridge memberEntity = (JDBCEntityBridge) memberPath.getEntity();

         if(!memberEntity.equals(colEntity))
         {
            throw new IllegalStateException(
               "Member must be if the same type as the collection, got: member="
               + memberEntity.getEntityName()
               + ", collection=" + colEntity.getEntityName()
            );
         }

         String memberAlias = aliasManager.getAlias(memberPath.getPath());

         addLeftJoinPath(memberPath);

         SQLUtil.getSelfCompareWhereClause(memberEntity.getPrimaryKeyFields(), colAlias, memberAlias, sql);
      }
      else
      {
         throw new IllegalStateException(
            "Was expecting ASTPath or ASTParameter but got " + member.getClass().getName()
         );
      }

      if(node.not)
      {
         sql.append(')').append(SQLUtil.OR);
         SQLUtil.getIsNullClause(false, colEntity.getPrimaryKeyFields(), colAlias, sql);
         sql.append(')');
      }

      return data;
   }

   public Object visit(ASTStringComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTBooleanComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.jjtGetNumChildren() == 2)
      {
         buf.append(' ').append(node.opp).append(' ');
         node.jjtGetChild(1).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTDatetimeComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTValueClassComparison node, Object data)
   {
      throw new IllegalStateException("Value class comparison is not yet supported.");
   }

   public Object visit(ASTEntityComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      Node arg0 = node.jjtGetChild(0);
      Node arg1 = node.jjtGetChild(1);
      if(node.opp.equals(SQLUtil.NOT_EQUAL))
      {
         compareEntity(true, arg0, arg1, buf);
      }
      else
      {
         compareEntity(false, arg0, arg1, buf);
      }
      return data;
   }

   public Object visit(ASTArithmeticComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTPlusMinus node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(' ').append(node.opps.get(i - 1)).append(' ');
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTMultDiv node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(' ').append(node.opps.get(i - 1)).append(' ');
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNegation node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('-');
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTArithmeticParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTStringParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTConcat node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.CONCAT);
      Object[] args = childrenToStringArr(2, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTSubstring node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.SUBSTRING);
      Object[] args = childrenToStringArr(3, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTUCase node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.UCASE);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLCase node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LCASE);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLength node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LENGTH);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLocate node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LOCATE);
      Object[] args = new Object[3];
      args[0] = node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString();
      args[1] = node.jjtGetChild(1).jjtAccept(this, new StringBuffer()).toString();
      if(node.jjtGetNumChildren() == 3)
      {
         args[2] = node.jjtGetChild(2).jjtAccept(this, new StringBuffer()).toString();
      }
      else
      {
         args[2] = "1";
      }
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTAbs node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.ABS);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTSqrt node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.SQRT);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTMod node, Object data)
   {
      StringBuffer buf = (StringBuffer)data;
      JDBCFunctionMappingMetaData function = JDBCTypeMappingMetaData.MOD_FUNC;
      Object[] args = childrenToStringArr(2, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTAvg node, Object data)
   {
      log.debug("ASTAvg>");
      return data;
   }

   public Object visit(ASTMax node, Object data)
   {
      log.debug("ASTMax>");
      return data;
   }

   public Object visit(ASTMin node, Object data)
   {
      log.debug("ASTMin>");
      return data;
   }

   public Object visit(ASTSum node, Object data)
   {
      log.debug("ASTSum>");
      return data;
   }

   public Object visit(ASTCount node, Object data)
   {
      log.debug("ASTCount>");
      return data;
   }

   public Object visit(ASTPath node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      if(!node.isCMPField())
      {
         throw new IllegalStateException(
            "Can only visit cmp valued path node. "
            + "Should have been handled at a higher level."
         );
      }

      // make sure this is mapped to a single column
      switch(node.type)
      {
         case EJBQLTypes.ENTITY_TYPE:
         case EJBQLTypes.VALUE_CLASS_TYPE:
         case EJBQLTypes.UNKNOWN_TYPE:
            throw new IllegalStateException(
               "Can not visit multi-column path " +
               "node. Should have been handled at a higher level."
            );
      }

      addLeftJoinPath(node);
      JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge) node.getCMPField();
      String alias = aliasManager.getAlias(node.getPath(node.size() - 2));
      SQLUtil.getColumnNamesClause(cmpField, alias, buf);
      return data;
   }

   public Object visit(ASTAbstractSchema node, Object data)
   {
      throw new IllegalStateException(
         "Can not visit abstract schema node. "
         + " Should have been handled at a higher level."
      );
   }

   public Object visit(ASTIdentifier node, Object data)
   {
      log.debug("ASTIdentifier>");
      return data;
   }

   public Object visit(ASTParameter node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      Class type = getParameterType(node.number);

      // make sure this is mapped to a single column
      int ejbqlType = EJBQLTypes.getEJBQLType(type);
      if(ejbqlType == EJBQLTypes.ENTITY_TYPE
         ||
         ejbqlType == EJBQLTypes.VALUE_CLASS_TYPE ||
         ejbqlType == EJBQLTypes.UNKNOWN_TYPE)
      {
         throw new IllegalStateException(
            "Can not visit multi-column " +
            "parameter node. Should have been handled at a higher level."
         );
      }

      QueryParameter param = new QueryParameter(
         node.number - 1,
         false, // isPrimaryKeyParameter
         null, // field
         null, // parameter
         typeFactory.getJDBCTypeForJavaType(type)
      );
      inputParameters.add(param);
      buf.append('?');

      return data;
   }

   public Object visit(ASTExactNumericLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.literal);
      return data;
   }

   public Object visit(ASTApproximateNumericLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.literal);
      return data;
   }

   public Object visit(ASTStringLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.value);
      return data;
   }

   public Object visit(ASTBooleanLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      if(node.value)
      {
         buf.append(typeMapping.getTrueMapping());
      }
      else
      {
         buf.append(typeMapping.getFalseMapping());
      }
      return data;
   }

   public Object visit(ASTFrom from, Object data)
   {
      log.debug("ASTFrom> started.");

      StringBuffer sql = (StringBuffer) data;
      sql.append(SQLUtil.FROM);

      from.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < from.jjtGetNumChildren(); ++i)
      {
         from.jjtGetChild(i).jjtAccept(this, data);
      }

      log.debug("ASTFrom> finished.");
      return data;
   }

   public Object visit(ASTCollectionMemberDeclaration node, Object data)
   {
      ASTPath path = (ASTPath) node.jjtGetChild(0);

      // assign the same alias for path and identifier
      ASTIdentifier id = (ASTIdentifier) node.jjtGetChild(1);
      String alias = aliasManager.getAlias(id.identifier);
      aliasManager.addAlias(path.getPath(), alias);

      addInnerJoinPath(path);

      return data;
   }

   public Object visit(ASTRangeVariableDeclaration node, Object data)
   {
      ASTAbstractSchema schema = (ASTAbstractSchema) node.jjtGetChild(0);
      JDBCEntityBridge entity = (JDBCEntityBridge) schema.entity;
      ASTIdentifier id = (ASTIdentifier) node.jjtGetChild(1);
      declareTable(id.identifier, entity.getTableName());
      return data;
   }

   // Private

   private void compareEntity(boolean not, Node fromNode, Node toNode, StringBuffer buf)
   {
      buf.append('(');
      if(not)
      {
         buf.append(SQLUtil.NOT).append('(');
      }

      ASTPath fromPath = (ASTPath) fromNode;
      addLeftJoinPath(fromPath);
      String fromAlias = aliasManager.getAlias(fromPath.getPath());
      JDBCEntityBridge fromEntity = (JDBCEntityBridge) fromPath.getEntity();

      if(toNode instanceof ASTParameter)
      {
         ASTParameter toParam = (ASTParameter) toNode;

         // can only compare like kind entities
         verifyParameterEntityType(toParam.number, fromEntity);

         inputParameters.addAll(QueryParameter.createParameters(toParam.number - 1, fromEntity));

         SQLUtil.getWhereClause(fromEntity.getPrimaryKeyFields(), fromAlias, buf);
      }
      else
      {
         ASTPath toPath = (ASTPath) toNode;
         addLeftJoinPath(toPath);
         String toAlias = aliasManager.getAlias(toPath.getPath());
         JDBCEntityBridge toEntity = (JDBCEntityBridge) toPath.getEntity();

         // can only compare like kind entities
         if(!fromEntity.equals(toEntity))
         {
            throw new IllegalStateException(
               "Only like types can be "
               + "compared: from entity="
               + fromEntity.getEntityName()
               + " to entity=" + toEntity.getEntityName()
            );
         }

         SQLUtil.getSelfCompareWhereClause(fromEntity.getPrimaryKeyFields(), fromAlias, toAlias, buf);
      }

      if(not)
      {
         buf.append(')');
      }
      buf.append(')');
   }

   private void join(String alias, StringBuffer sql)
   {
      leftJoin(alias, sql);
      innerJoin(alias, sql);
   }

   private void leftJoin(String alias, StringBuffer sql)
   {
      Set paths = (Set) leftJoinPaths.get(alias);
      if(paths == null || paths.isEmpty())
      {
         return;
      }

      for(Iterator iter = paths.iterator(); iter.hasNext();)
      {
         ASTPath path = (ASTPath) iter.next();
         for(int i = 1; i < path.size(); ++i)
         {
            if(path.isCMRField(i))
            {
               final String curPath = path.getPath(i);
               final JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge) path.getCMRField(i);
               final JDBCEntityBridge joinEntity = cmrField.getRelatedJDBCEntity();
               final String joinAlias = aliasManager.getAlias(curPath);

               JDBCRelationMetaData relation = cmrField.getMetaData().getRelationMetaData();
               if(relation.isTableMappingStyle())
               {
                  String relTableAlias = aliasManager.getRelationTableAlias(curPath);
                  sql.append(" LEFT OUTER JOIN ")
                     .append(cmrField.getTableName())
                     .append(' ')
                     .append(relTableAlias)
                     .append(" ON ");
                  SQLUtil.getRelationTableJoinClause(cmrField, alias, relTableAlias, sql);

                  sql.append(" LEFT OUTER JOIN ")
                     .append(joinEntity.getTableName())
                     .append(' ')
                     .append(joinAlias)
                     .append(" ON ");
                  SQLUtil.getRelationTableJoinClause(cmrField.getRelatedCMRField(), joinAlias, relTableAlias, sql);
               }
               else
               {
                  sql.append(" LEFT OUTER JOIN ")
                     .append(joinEntity.getTableName())
                     .append(' ')
                     .append(joinAlias)
                     .append(" ON ");

                  SQLUtil.getJoinClause(cmrField, alias, joinAlias, sql);
               }

               join(joinAlias, sql);
            }
         }
      }
   }

   private void innerJoin(String alias, StringBuffer sql)
   {
      Set paths = (Set) innerJoinPaths.get(alias);
      if(paths == null || paths.isEmpty())
      {
         return;
      }

      for(Iterator iter = paths.iterator(); iter.hasNext();)
      {
         ASTPath path = (ASTPath) iter.next();
         for(int i = 1; i < path.size(); ++i)
         {
            if(path.isCMRField(i))
            {
               final String curPath = path.getPath(i);
               final JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge) path.getCMRField(i);
               final JDBCEntityBridge joinEntity = cmrField.getRelatedJDBCEntity();
               final String joinAlias = aliasManager.getAlias(curPath);

               JDBCRelationMetaData relation = cmrField.getMetaData().getRelationMetaData();
               if(relation.isTableMappingStyle())
               {
                  String relTableAlias = aliasManager.getRelationTableAlias(curPath);
                  sql.append(" INNER JOIN ")
                     .append(cmrField.getTableName())
                     .append(' ')
                     .append(relTableAlias)
                     .append(" ON ");
                  SQLUtil.getRelationTableJoinClause(cmrField, alias, relTableAlias, sql);

                  sql.append(" INNER JOIN ")
                     .append(joinEntity.getTableName())
                     .append(' ')
                     .append(joinAlias)
                     .append(" ON ");
                  SQLUtil.getRelationTableJoinClause(cmrField.getRelatedCMRField(), joinAlias, relTableAlias, sql);
               }
               else
               {
                  sql.append(" INNER JOIN ")
                     .append(joinEntity.getTableName())
                     .append(' ')
                     .append(joinAlias)
                     .append(" ON ");

                  SQLUtil.getJoinClause(cmrField, alias, joinAlias, sql);
               }

               join(joinAlias, sql);
            }
         }
      }
   }

   private void declareTable(String identifier, String table)
   {
      identifierToTable.put(identifier, table);
   }

   private void addLeftJoinPath(ASTPath path)
   {
      if(path.size() > 1 && path.isCMRField(1))
      {
         final String identifier = path.getPath(0);
         final String alias = aliasManager.getAlias(identifier);
         Set paths = (Set) leftJoinPaths.get(alias);
         if(paths == null)
         {
            paths = new HashSet();
            leftJoinPaths.put(alias, paths);
         }
         paths.add(path);

         log.debug("added left-join path: " + path.getPath() + ", " + identifier + "=" + alias);
      }
   }

   private void addInnerJoinPath(ASTPath path)
   {
      if(path.size() > 1 && path.isCMRField(1))
      {
         final String identifier = path.getPath(0);
         final String alias = aliasManager.getAlias(identifier);
         Set paths = (Set) innerJoinPaths.get(alias);
         if(paths == null)
         {
            paths = new HashSet();
            innerJoinPaths.put(alias, paths);
         }
         paths.add(path);

         log.debug("added inner-join path: " + path.getPath() + ", " + identifier + "=" + alias);
      }
   }

   private Object[] childrenToStringArr(int numChildren, Node node)
   {
      Object[] args = new Object[numChildren];
      for(int i = 0; i < numChildren; ++i)
      {
         args[i] = node.jjtGetChild(i).jjtAccept(this, new StringBuffer()).toString();
      }
      return args;
   }
}
