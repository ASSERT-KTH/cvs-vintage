/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejbql;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.jboss.cmp.query.CollectionRelation;
import org.jboss.cmp.query.Comparison;
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.Expression;
import org.jboss.cmp.query.Literal;
import org.jboss.cmp.query.NamedRelation;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.QueryNode;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.query.Relation;
import org.jboss.cmp.query.Parameter;
import org.jboss.cmp.query.Condition;
import org.jboss.cmp.query.ConditionExpression;
import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.schema.AbstractSchema;
import org.jboss.cmp.schema.AbstractType;

public class EJBQL20Compiler implements ParserVisitor
{
   private final AbstractSchema schema;
   private final EJBQL20Parser parser;

   public EJBQL20Compiler(AbstractSchema schema)
   {
      this.schema = schema;
      parser = new EJBQL20Parser(new StringReader(""));
   }

   public Query compile(String ejbql, AbstractType[] params) throws ParseException, CompileException
   {
      parser.ReInit(new StringReader(ejbql));
      ASTEJBQL rootNode = parser.EJBQL();

      return (Query) rootNode.jjtAccept(this, params);
   }

   public Object visit(SimpleNode node, Object data)
   {
      throw new IllegalStateException();
   }

   public Object visit(ASTEJBQL node, Object data) throws CompileException
   {
      Query query = new Query((AbstractType[]) data);

      // visit FROM first to define all the identification_variables
      ((VisitableNode) node.jjtGetChild(1)).jjtAccept(this, query);

      // now we can get the SELECT list
      ((VisitableNode) node.jjtGetChild(0)).jjtAccept(this, query);

      // and finally the WHERE clause
      if (node.jjtGetNumChildren() > 2)
      {
         ((VisitableNode) node.jjtGetChild(2)).jjtAccept(this, query);
      }
      return query;
   }

   public Object visit(ASTSelect node, Object data) throws CompileException
   {
      Query query = (Query) data;
      Projection projection = new Projection();
      projection.setDistinct(node.distinct);

      VisitableNode n = (VisitableNode) node.jjtGetChild(0);
      if (n instanceof ASTPath)
      {
         Path nav = (Path) n.jjtAccept(this, query);
         if (nav.isCollection())
         {
            throw new CompileException("Cannot SELECT collection, path is:" + nav);
         }
         projection.addChild(nav);
      }
      else if (n instanceof ASTIdentifier)
      {
         ASTIdentifier idNode = (ASTIdentifier) n;
         NamedRelation relation = query.getRelation(idNode.getName());
         if (relation == null)
         {
            throw new CompileException("Unknown identification variable: " + idNode.getName());
         }
         projection.addChild(new Path(relation));
      }
      else
      {
         throw new IllegalStateException();
      }
      query.setProjection(projection);
      return data;
   }

   public Object visit(ASTFrom node, Object data) throws CompileException
   {
      return node.childrenAccept(this, data);
   }

   public Object visit(ASTIdDeclaration node, Object data) throws CompileException
   {
      Query query = (Query) data;
      ASTIdentifier idNode = (ASTIdentifier) node.jjtGetChild(1);
      String id = idNode.getName();
      if (schema.isClassNameInUse(id))
      {
         throw new CompileException("IdentificationVariable " + id + " matches ejb-name or abstract-schema-name");
      }
      if (query.getRelation(id) != null)
      {
         throw new CompileException("IdentificationVariable " + id + " already used");
      }

      NamedRelation relation;
      Node n = node.jjtGetChild(0);
      if (n instanceof ASTIdentifier)
      {
         ASTIdentifier rangeDecl = (ASTIdentifier) n;
         AbstractClass rangeClass = schema.getClassByName(rangeDecl.getName());
         if (rangeClass == null)
         {
            throw new CompileException("Unknown abstract-schema-name: " + rangeDecl.getName());
         }
         relation = new RangeRelation(id, rangeClass);
      }
      else if (n instanceof ASTPath)
      {
         ASTPath pathDecl = (ASTPath) n;
         Path nav = (Path) pathDecl.jjtAccept(this, data);
         if (nav.isCollection() == false)
         {
            throw new CompileException("Path does not end in a collection:" + nav);
         }
         relation = new CollectionRelation(id, nav);
      }
      else
      {
         throw new IllegalStateException();
      }

      query.addAlias(relation);
      Relation left = query.getRelation();
      if (left != null)
      {
         query.setRelation(new CrossJoin(left, relation));
      }
      else
      {
         query.setRelation(relation);
      }
      return data;
   }

   public Object visit(ASTWhere node, Object data) throws CompileException
   {
      Query query = (Query) data;
      VisitableNode conditionNode = (VisitableNode) node.jjtGetChild(0);
      query.setFilter((QueryNode)conditionNode.jjtAccept(this, data));
      return null;
   }

   public Object visit(ASTOr node, Object data) throws CompileException
   {
      ConditionExpression expr = new ConditionExpression(ConditionExpression.OR);
      for (int i=0; i < node.jjtGetNumChildren(); i++)
      {
         Condition child = (Condition) ((VisitableNode)node.jjtGetChild(i)).jjtAccept(this, data);
         expr.addChild(child);
      }
      return expr;
   }

   public Object visit(ASTAnd node, Object data) throws CompileException
   {
      ConditionExpression expr = new ConditionExpression(ConditionExpression.AND);
      for (int i=0; i < node.jjtGetNumChildren(); i++)
      {
         Condition child = (Condition) ((VisitableNode)node.jjtGetChild(i)).jjtAccept(this, data);
         expr.addChild(child);
      }
      return expr;
   }

   public Object visit(ASTNot node, Object data) throws CompileException
   {
      ConditionExpression expr = new ConditionExpression(ConditionExpression.NOT);
      Condition child = (Condition) ((VisitableNode)node.jjtGetChild(0)).jjtAccept(this, data);
      expr.addChild(child);
      return expr;
   }

   private static final Map operatorMap = new HashMap();
   static {
      operatorMap.put("=", Comparison.EQUAL);
      operatorMap.put("<>", Comparison.NOTEQUAL);
      operatorMap.put("<", Comparison.LESSTHAN);
      operatorMap.put("<=", Comparison.LESSEQUAL);
      operatorMap.put(">", Comparison.GREATERTHAN);
      operatorMap.put(">=", Comparison.GREATEREQUAL);
   }

   public Object visit(ASTCondition node, Object data) throws CompileException
   {
      Token token = node.token;
      switch (token.kind)
      {
         case EJBQL20ParserConstants.COMPARISION_OPERATOR:
            String operator = (String) operatorMap.get(token.image);
            if (operator == null)
               throw new IllegalStateException("Unkown operator: "+token.image);

            Expression left = (Expression) ((VisitableNode)node.jjtGetChild(0)).jjtAccept(this, data);
            Expression right = (Expression) ((VisitableNode)node.jjtGetChild(1)).jjtAccept(this, data);
            int leftFamily = left.getType().getFamily();
            int rightFamily = right.getType().getFamily();

            switch (leftFamily)
            {
               case AbstractType.STRING:
                  if (rightFamily != AbstractType.STRING)
                     throw new CompileException("Type mismatch");
                  if (operator != Comparison.EQUAL && operator != Comparison.NOTEQUAL)
                     throw new CompileException("Invalid string comparison operator: "+operator);
                  break;
               case AbstractType.INTEGER:
               case AbstractType.FLOAT:
                  if (rightFamily != AbstractType.INTEGER && rightFamily != AbstractType.FLOAT)
                     throw new CompileException("Type mismatch");
                  break;
               case AbstractType.BOOLEAN:
                  if (rightFamily != AbstractType.BOOLEAN)
                     throw new CompileException("Type mismatch");
                  if (operator != Comparison.EQUAL && operator != Comparison.NOTEQUAL)
                     throw new CompileException("Invalid boolean comparison operator: "+operator);
                  break;
               case AbstractType.DATETIME:
                  if (rightFamily != AbstractType.DATETIME)
                     throw new CompileException("Type mismatch");
                  if (operator != Comparison.EQUAL &&
                        operator != Comparison.NOTEQUAL &&
                        operator != Comparison.LESSTHAN &&
                        operator != Comparison.GREATERTHAN)
                     throw new CompileException("Invalid datetime comparison operator: " + operator);
                  break;
               case AbstractType.OBJECT:
                  if (left instanceof Path)
                  {
                     Path path = (Path) left;
                     if (path.isCollection())
                        throw new CompileException("Invalid use of collection path: "+path);
                  }
                  if (operator != Comparison.EQUAL && operator != Comparison.NOTEQUAL)
                     throw new CompileException("Invalid entity bean comparison operator: "+operator);
                  break;
            }
            return new Comparison(left, operator, right);
         default:
            throw new CompileException("Unknown condition token "+token.image);
      }
   }

   public Object visit(ASTPath node, Object data) throws CompileException
   {
      Query query = (Query) data;
      ASTIdentifier idVarNode = (ASTIdentifier) node.jjtGetChild(0);
      NamedRelation relation = query.getRelation(idVarNode.getName());
      if (relation == null)
      {
         throw new CompileException("Unknown identification-variable: " + idVarNode.getName());
      }
      Path navigation = new Path(relation);
      for (int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         if (i > 1 && navigation.isCollection())
         {
            throw new CompileException("Cannot navigate collection path:" + node.toString(i));
         }
         ASTIdentifier idNode = (ASTIdentifier) node.jjtGetChild(i);
         String name = idNode.getName();
         AbstractType type = navigation.getType();
         if (type instanceof AbstractClass)
         {
            AbstractClass clazz = (AbstractClass) type;
            AbstractAttribute attr = clazz.getAttributeByName(name);
            if (attr != null)
            {
               navigation.addStep(attr);
            }
            else
            {
               AbstractAssociationEnd end = clazz.getAssocationByName(name);
               if (end == null)
               {
                  throw new CompileException("Cannot resolve path:" + node.toString(i + 1));
               }
               navigation.addStep(end);
            }
         }
         else
         {
            throw new CompileException("Cannot navigate path, prefix is:" + node.toString(i + 1));
         }
      }
      return navigation;
   }

   public Object visit(ASTIdentifier node, Object data)
   {
      return null;
   }

   public Object visit(ASTInputParameter node, Object data) throws CompileException
   {
      Query query = (Query)data;
      AbstractType[] queryParams = query.getParameters();
      if (queryParams == null || queryParams.length <= node.id || node.id < 0)
         throw new CompileException("Invalid query parameter: "+(node.id+1));
      return new Parameter(query, node.id);
   }

   public Object visit(ASTLiteral node, Object data)
   {
      AbstractType type;
      Object value;
      String image = node.token.image;
      switch (node.token.kind)
      {
         case EJBQL20ParserConstants.INTEGER_LITERAL:
            type = schema.getBuiltinType(AbstractType.INTEGER);
            if (image.endsWith("l") || image.endsWith("L"))
               value = Long.decode(image.substring(0, image.length()-1));
            else
               value = Integer.decode(image);
            break;
         case EJBQL20ParserConstants.FLOATING_POINT_LITERAL:
            type = schema.getBuiltinType(AbstractType.FLOAT);
            value = Double.valueOf(image);
            break;
         case EJBQL20ParserConstants.STRING_LITERAL:
            type = schema.getBuiltinType(AbstractType.STRING);
            value = unEscape(image);
            break;
         case EJBQL20ParserConstants.BOOLEAN_LITERAL:
            type = schema.getBuiltinType(AbstractType.BOOLEAN);
            value = "true".equalsIgnoreCase(image) ? Boolean.TRUE : Boolean.FALSE;
            break;
         default:
            throw new IllegalStateException("Unknown literal: "+node.id);
      }
      return new Literal(type, value);
   }

   private String unEscape(String s)
   {
      StringBuffer buf = new StringBuffer(s.length()+16);
      for (int i=1; i < s.length()-1; i++)
      {
         char c = s.charAt(i);
         if (c == '\'' && s.charAt(i+1) == '\'')
            i++;
         buf.append(c);
      }
      return buf.toString();
   }
}
