package org.jboss.ejb.plugins.cmp.ejbql;

public class EJBQLParser {

	public EJBQLParser() {
	}
	
	// EJB QL ::= select_clause from_clause [where_clause]
	public Parser ejbqlQuery() {
		Sequence a = new Sequence();
		a.add(selectClause());
		a.add(fromClause());
		a.add(new Optional(whereClause()));
		
		return a;
	}
	
	// from_clause ::=FROM identification_variable_declaration [, identification_variable_declaration]*
	protected Parser fromClause() {
		Sequence s = new Sequence();
		s.add(new Literal("FROM"));
		s.add(identificationVariableDeclaration());
		
		Sequence commaList = new Sequence();
		commaList.add(new Symbol(","));
		commaList.add(identificationVariableDeclaration());
		
		s.add(new Repetition(commaList));
		
		return s;
	}

	// identification_variable_declaration ::= collection_member_declaration | range_variable_declaration
	private Alternation identVarDec;
	protected Parser identificationVariableDeclaration() {
		if(identVarDec == null) {
			identVarDec = new Alternation();
			identVarDec.add(collectionMemberDeclaration());
			identVarDec.add(rangeVariableDeclaration());
		}
		return identVarDec;
	}

	// collection_member_declaration ::=IN (collection_valued_path_expression) [AS ] identifier
	private Sequence colMemDec;
	protected Parser collectionMemberDeclaration() {
		if(colMemDec == null) {
			colMemDec = new Sequence();
			colMemDec.add(new Literal("IN"));
			colMemDec.add(new Symbol("("));
			colMemDec.add(collectionValuedPathExpression());
			colMemDec.add(new Symbol(")"));
			colMemDec.add(new Optional(new Literal("AS")));
			colMemDec.add(identifier());
		}
		return colMemDec;
	}

	// range_variable_declaration ::= abstract_schema_name [AS ] identifier
	private Sequence rngVarDec;
	protected Parser rangeVariableDeclaration() {
		if(rngVarDec == null) {
			rngVarDec = new Sequence();
			rngVarDec.add(abstractSchemaName());
			//rngVarDec.add(new Optional(new Literal("AS")));
			rngVarDec.add(identifier());
		}
		return rngVarDec;
	}

	// single_valued_path_expression ::= {single_valued_navigation | identification_variable}.cmp_field | single_valued_navigation
	private Alternation singleValPathExp;
	protected Parser singleValuedPathExpression() {
		if(singleValPathExp == null) {
			
			// {single_valued_navigation | identification_variable}
			Alternation navOrIdent = new Alternation();
			navOrIdent.add(singleValuedNavigation());
			navOrIdent.add(identificationVariable());
			
			// {above}.cmp_field
			Sequence cmpFieldExp = new Sequence();
			cmpFieldExp.add(navOrIdent);
			cmpFieldExp.add(new Symbol("."));
			cmpFieldExp.add(cmpField());
			
			singleValPathExp = new Alternation();
			singleValPathExp.add(cmpFieldExp);
			singleValPathExp.add(singleValuedNavigation());
		}
		return singleValPathExp;
	}

	// single_valued_navigation ::= identification_variable.[single_valued_cmr_field.]* single_valued_cmr_field
	private Sequence singleValNav;
	protected Parser singleValuedNavigation() {
		if(singleValNav == null) {
			singleValNav = new Sequence();
			singleValNav.add(identificationVariable());
			singleValNav.add(new Symbol("."));
			
			Sequence nav = new Sequence();
			nav.add(singleValuedCmrField());
			nav.add(new Symbol("."));

			singleValNav.add(new Repetition(nav));
			singleValNav.add(singleValuedCmrField());
		}
		return singleValNav;
	}

	// collection_valued_path_expression ::= identification_variable.[single_valued_cmr_field.]*collection_valued_cmr_field
	private Sequence colValPathExp;
	protected Parser collectionValuedPathExpression() {
		if(colValPathExp == null) {
			colValPathExp = new Sequence();
			colValPathExp.add(identificationVariable());
			colValPathExp.add(new Symbol("."));
			
			Sequence path = new Sequence();
			path.add(singleValuedCmrField());
			path.add(new Symbol("."));

			colValPathExp.add(new Repetition(path));
			colValPathExp.add(collectionValuedCmrField());
		}
		return colValPathExp;
	}

	// select_clause ::=SELECT [DISTINCT ] {single_valued_path_expression | OBJECT (identification_variable)}
	private Sequence selClau;
	protected Parser selectClause() {
		if(selClau == null) {
			selClau = new Sequence();
			selClau.add(new Literal("SELECT"));
			selClau.add(new Optional(new Literal("DISTINCT")));
			
			Alternation target = new Alternation();
			target.add(singleValuedPathExpression());
			
			Sequence objectTarget = new Sequence();
			objectTarget.add(new Literal("OBJECT"));
			objectTarget.add(new Symbol("("));
			objectTarget.add(identificationVariable());
			objectTarget.add(new Symbol(")"));
			
			target.add(objectTarget);
			
			selClau.add(target);
		}
		return selClau;
	}

	// where_clause ::= WHERE conditional_expression
	private Sequence whrClau;
	protected Parser whereClause() {
		if(whrClau == null) {
			whrClau = new Sequence();
			whrClau.add(new Literal("WHERE"));
			whrClau.add(conditionalExpression());
		}
		return whrClau;
	}

	// conditional_expression ::= conditional_term | conditional_expression OR conditional_term
	// convert to elimiate infinite recursion
	// conditional_expression ::= conditional_term {OR conditional_term}*
	private Sequence condExp;
	protected Parser conditionalExpression() {
		if(condExp == null) {
			condExp = new Sequence();
			condExp.add(conditionalTerm());
			
			Sequence orExps = new Sequence();
			orExps.add(new Literal("OR"));
			orExps.add(conditionalTerm());
	
			condExp.add(new Repetition(orExps));
		}
		return condExp;
	}
	
	// conditional_term ::= conditional_factor | conditional_term AND conditional_factor
	// convert to elimiate infinite recursion
	// conditional_term ::= conditional_factor {AND conditional_factor}*
	private Sequence condTerm;
	protected Parser conditionalTerm() {
		if(condTerm == null) {
			condTerm = new Sequence();
			condTerm.add(conditionalFactor());
			
			Sequence andTerms = new Sequence();
			andTerms.add(new Literal("AND"));
			andTerms.add(conditionalFactor());
		
			condTerm.add(new Repetition(andTerms));
		}
		return condTerm;
	}
		
	// conditional_factor ::= [NOT ] conditional_test
	private Sequence condFactor;
	protected Parser conditionalFactor() {
		if(condFactor == null) {
			condFactor = new Sequence();
			condFactor.add(new Optional(new Literal("NOT")));
			condFactor.add(conditionalTest());
		}
		return condFactor;
	}
	
	// conditional_test :: = conditional_primary
	protected Parser conditionalTest() {
		return conditionalPrimary();
	}
	
	// conditional_primary ::= simple_cond_expression | (conditional_expression)
	private Alternation condPrimary;
	protected Parser conditionalPrimary() {
		if(condPrimary == null) {
			condPrimary = new Alternation();
			condPrimary.add(simpleCondExpression());
			
			Sequence parenExp = new Sequence();
			parenExp.add(new Symbol("("));
			parenExp.add(conditionalExpression());
			parenExp.add(new Symbol(")"));
			
			condPrimary.add(parenExp);
		}
		return condPrimary;
	}

	// simple_cond_expression ::= comparison_expression | 
	// 	between_expression | like_expression |
	// 	in_expression | null_comparison_expression |
	// 	empty_collection_comparison_expression |
	// 	collection_member_expression
	private Alternation simpleCondExp;
	protected Parser simpleCondExpression() {
		if(simpleCondExp == null) {
			simpleCondExp = new Alternation();
			simpleCondExp.add(comparisonExpression());
			simpleCondExp.add(betweenExpression());
			simpleCondExp.add(likeExpression());
			simpleCondExp.add(inExpression());
			simpleCondExp.add(nullComparisonExpression());
			simpleCondExp.add(emptyCollectionComparisonExpression());
			simpleCondExp.add(collectionMemberExpression());
		}
		return simpleCondExp;
	}

	
	// between_expression ::= arithmetic_expression [NOT ]BETWEEN arithmetic_expression AND arithmetic_expression
	private Sequence betweenExp;
	protected Parser betweenExpression() {
		if(betweenExp == null) {
			betweenExp = new Sequence();
			betweenExp.add(arithmeticExpression());
			betweenExp.add(new Optional(new Literal("NOT")));
			betweenExp.add(new Literal("BETWEEN"));
			betweenExp.add(arithmeticExpression());
			betweenExp.add(new Literal("AND"));
			betweenExp.add(arithmeticExpression());			
		}
		return betweenExp;
	}
	
	// in_expression ::= single_valued_path_expression [NOT ]IN (string_literal [, string_literal]* )
	private Sequence inExp;
	protected Parser inExpression() {
		if(inExp == null) {
			inExp = new Sequence();
			inExp.add(singleValuedPathExpression());
			inExp.add(new Optional(new Literal("NOT")));
			inExp.add(new Literal("IN"));
			inExp.add(new Symbol("("));
			inExp.add(new StringLiteral());
			
			Sequence commaList =  new Sequence();
			commaList.add(new Symbol(","));
			commaList.add(new StringLiteral());
			
			inExp.add(new Repetition(commaList));
			
			inExp.add(new Symbol(")"));
		}
		return inExp;
	}
	
	// like_expression ::= single_valued_path_expression [NOT ]LIKE pattern_value [ESCAPE escape-character]
	private Sequence likeExp;
	protected Parser likeExpression() {
		if(likeExp == null) {
			likeExp = new Sequence();
			likeExp.add(singleValuedPathExpression());
			likeExp.add(new Optional(new Literal("NOT")));
			likeExp.add(new Literal("LIKE"));
			likeExp.add(patternValue());
			
			Sequence excSeq = new Sequence();
			excSeq.add(new Literal("ESCAPE"));
			excSeq.add(escapeCharacter());
			
			likeExp.add(excSeq);
		}
		return likeExp;
	}
	
	// null_comparison_expression ::= single_valued_path_expression IS [NOT ] NULL
	private Sequence nullComparisonExp;
	protected Parser nullComparisonExpression() {
		if(nullComparisonExp == null) {
			nullComparisonExp = new Sequence();
			nullComparisonExp.add(singleValuedPathExpression());
			nullComparisonExp.add(new Literal("IS"));
			nullComparisonExp.add(new Optional(new Literal("NOT")));
			nullComparisonExp.add(new Literal("NULL"));
		}
		return nullComparisonExp;
	}
	
	// empty_collection_comparison_expression ::= collection_valued_path_expression IS [NOT] EMPTY
	private Sequence emptyColCompExp;
	protected Parser emptyCollectionComparisonExpression() {
		if(emptyColCompExp == null) {
			emptyColCompExp = new Sequence();
			emptyColCompExp.add(collectionValuedPathExpression());
			emptyColCompExp.add(new Literal("IS"));
			emptyColCompExp.add(new Optional(new Literal("NOT")));
			emptyColCompExp.add(new Literal("EMPTY"));
		}
		return emptyColCompExp;
	}
	
	// collection_member_expression ::= single_valued_path_expression [NOT ]MEMBER [OF ] collection_valued_path_expression
	private Sequence colMemExp;
	protected Parser collectionMemberExpression() {
		if(colMemExp == null) {
			colMemExp = new Sequence();
			colMemExp.add(singleValuedPathExpression());
			colMemExp.add(new Optional(new Literal("NOT")));
			colMemExp.add(new Literal("MEMBER"));
			colMemExp.add(new Optional(new Literal("OF")));
			colMemExp.add(collectionValuedPathExpression());
		}
		return colMemExp;
	}

	// comparison_expression ::=
	// 	string_value { =|<>} string_expression |
	// 	boolean_value { =|<>} boolean_expression} |
	// 	datetime_value { = | <> | > | < } datetime_expression |
	// 	entity_bean_value { = | <> } entity_bean_expression |
	// 	arithmetic_value comparison_operator single_value_designator
	private Alternation compExp;
	protected Parser comparisonExpression() {
		if(compExp == null) {
			compExp = new Alternation();
			
			Sequence stringComp = new Sequence();
			stringComp.add(stringValue());
			stringComp.add(new Alternation().add(new Symbol("=")).add(new Symbol("<>")));
			stringComp.add(stringExpression());
			compExp.add(stringComp);
			
			Sequence booleanComp = new Sequence();
			booleanComp.add(booleanValue());
			booleanComp.add(new Alternation().add(new Symbol("=")).add(new Symbol("<>")));
			booleanComp.add(booleanExpression());
			compExp.add(booleanComp);
			
			Sequence datetimeComp = new Sequence();
			datetimeComp.add(datetimeValue());
			datetimeComp.add(new Alternation().add(new Symbol("=")).add(new Symbol("<>")).add(new Symbol(">")).add(new Symbol("<")));
			datetimeComp.add(datetimeExpression());
			compExp.add(datetimeComp);
			
			Sequence entityBeanComp = new Sequence();
			entityBeanComp.add(entityBeanValue());
			entityBeanComp.add(new Alternation().add(new Symbol("=")).add(new Symbol("<>")));
			entityBeanComp.add(entityBeanExpression());
			compExp.add(entityBeanComp);

			Sequence arithComp = new Sequence();
			arithComp.add(arithmeticValue());
			arithComp.add(comparisonOperator());
			arithComp.add(singleValueDesignator());
			compExp.add(arithComp);
		}
		return compExp;
	}

	// arithmetic_value ::= single_valued_path_expression | functions_returning_numerics
	private Alternation arithValue;
	protected Parser arithmeticValue() {
		if(arithValue == null) {
			arithValue = new Alternation();
			arithValue.add(singleValuedPathExpression());
			arithValue.add(functionsReturningNumerics());
		}
		return arithValue;
	}
	
	// single_value_designator ::= scalar_expression
	protected Parser singleValueDesignator() {
		return scalarExpression();
	}
	
	// comparison_operator ::=   = | > | >= | < | <= | <>
	private Alternation compOpp;
	protected Parser comparisonOperator() {
		if(compOpp == null) {
			compOpp = new Alternation();
			compOpp.add(new Symbol("="));
			compOpp.add(new Symbol(">"));
			compOpp.add(new Symbol(">="));
			compOpp.add(new Symbol("<"));
			compOpp.add(new Symbol("<="));
			compOpp.add(new Symbol("<>"));
		}
		return compOpp;
	}
	
	// scalar_expression ::= arithmetic_expression
	protected Parser scalarExpression() {
		return arithmeticExpression();
	}
	
	// arithmetic_expression ::= arithmetic_term | arithmetic_expression { + | - } arithmetic_term
	// convert to elimiate infinite recursion
	// arithmetic_expression ::= arithmetic_term {{ + | - } arithmetic_term}*
	private Sequence arithExp;
	protected Parser arithmeticExpression() {
		if(arithExp == null) {
			arithExp = new Sequence();
			arithExp.add(arithmeticTerm());
			
			Sequence addSeq = new Sequence();
			addSeq.add(new Alternation().add(new Symbol("+")).add(new Symbol("-")));
			addSeq.add(arithmeticTerm());
			
			arithExp.add(new Repetition(addSeq));
		}
		return arithExp;
	}
	
	// arithmetic_term ::= arithmetic_factor | arithmetic_term { * | / } arithmetic_factor
	// convert to elimiate infinite recursion
	// arithmetic_term ::= arithmetic_factor {{ * | / } arithmetic_factor}*
	private Sequence arithTerm;
	protected Parser arithmeticTerm() {
		if(arithTerm == null) {
			arithTerm = new Sequence();
			arithTerm.add(arithmeticFactor());

			Sequence multSeq = new Sequence();
			multSeq.add(new Alternation().add(new Symbol("*")).add(new Symbol("/")));
			multSeq.add(arithmeticFactor());
			
			arithTerm.add(new Repetition(multSeq));
		}
		return arithTerm;
	}

	// arithmetic_factor ::= { + |- } arithmetic_primary
	private Sequence arithFactor;
	protected Parser arithmeticFactor() {
		if(arithFactor == null) {
			arithFactor = new Sequence();
			arithFactor.add(new Alternation().add(new Symbol("+")).add(new Symbol("-")));
			arithFactor.add(arithmeticPrimary());
		}
		return arithFactor;
	}
	
	// arithmetic_primary ::= single_valued_path_expression | literal | (arithmetic_expression) | input_parameter | functions_returning_numerics
	private Alternation arithPrim;
	protected Parser arithmeticPrimary() {
		if(arithPrim == null) {
			arithPrim = new Alternation();
			arithPrim.add(singleValuedPathExpression());
			arithPrim.add(new NumericLiteral());
			
			Sequence parenExp = new Sequence();
			parenExp.add(new Symbol("("));
			parenExp.add(arithmeticExpression());
			parenExp.add(new Symbol(")"));
			arithPrim.add(parenExp);
			
			arithPrim.add(new InputParameter());
			arithPrim.add(functionsReturningNumerics());
		}
		return arithPrim;
	}
	
	// string_value ::= single_valued_path_expression | functions_returning_strings
	private Alternation strValue;
	protected Parser stringValue() {
		if(strValue == null) {
			strValue = new Alternation();
			strValue.add(singleValuedPathExpression());
			strValue.add(functionsReturningStrings());
		}
		return strValue;
	}
	
	// string_expression ::= string_primary | input_expression
	private Alternation strExp;
	protected Parser stringExpression() {
		if(strExp == null) {
			strExp = new Alternation();
			strExp.add(stringPrimary());
			// Note: changed due from obvious typo in pfd2
			strExp.add(new InputParameter());
		}
		return strExp;
	}
		
	// string_primary ::= single_valued_path_expression | literal | (string_expression) | functions_returning_strings
	private Alternation strPrim;
	protected Parser stringPrimary() {
		if(strPrim == null) {
			strPrim = new Alternation();
			strPrim.add(singleValuedPathExpression());
			strPrim.add(new StringLiteral());
			
			Sequence parenExp = new Sequence();
			parenExp.add(new Symbol("("));
			parenExp.add(stringExpression());
			parenExp.add(new Symbol(")"));
			strPrim.add(parenExp);
			
			strPrim.add(functionsReturningStrings());
		}
		return strPrim;
	}
	
	// datetime_value ::= single_valued_path_expression
	protected Parser datetimeValue() {
		return singleValuedPathExpression();
	}
	
	// datetime_expression ::= datetime_value | input_parameter
	private Alternation dateExp;
	protected Parser datetimeExpression() {
		if(dateExp == null) {
			dateExp = new Alternation();
			dateExp.add(datetimeValue());
			dateExp.add(new InputParameter());
		}
		return dateExp;
	}
	
	// boolean_value ::= single_valued_path_expression
	protected Parser booleanValue() {
		return singleValuedPathExpression();
	}
	
	// boolean_expression ::= single_valued_path_expression | literal | input_parameter
	private Alternation boolExp;
	protected Parser booleanExpression() {
		if(boolExp == null) {
			boolExp = new Alternation();
			boolExp.add(singleValuedPathExpression());
			boolExp.add(new Literal("TRUE"));
			boolExp.add(new Literal("FALSE"));
			boolExp.add(new InputParameter());
		}
		return boolExp;
	}
	
	// entity_bean_value ::= single_valued_path_expression | identification_variable
	private Alternation entBeanVal;
	protected Parser entityBeanValue() {
		if(entBeanVal == null) {
			entBeanVal = new Alternation();
			entBeanVal.add(singleValuedPathExpression());
			entBeanVal.add(identificationVariable());
		}
		return entBeanVal;
	}
	
	// entity_bean_expression ::= entity_bean_value | input_parameter
	private Alternation entBeanExp;
	protected Parser entityBeanExpression() {
		if(entBeanExp == null) {
			entBeanExp = new Alternation();
			entBeanExp.add(entityBeanValue());
			entBeanExp.add(new InputParameter());
		}
		return entBeanExp;
	}
		
	// functions_returning_strings ::= CONCAT (string_expression, string_expression) |
	// 	SUBSTRING (string_expression, arithmetic_expression, arithmetic_expression)
	private Alternation funRetStr;
	protected Parser functionsReturningStrings() {
		if(funRetStr == null) {
			funRetStr = new Alternation();
			
			Sequence concat = new Sequence();
			concat.add(new Literal("CONCAT"));
			concat.add(new Symbol("("));
			concat.add(stringExpression());
			concat.add(new Symbol(","));
			concat.add(stringExpression());
			concat.add(new Symbol(")"));
			funRetStr.add(concat);
			
			Sequence substring = new Sequence();
			substring.add(new Literal("SUBSTRING"));
			substring.add(new Symbol("("));
			substring.add(stringExpression());
			substring.add(new Symbol(","));
			substring.add(arithmeticExpression());
			substring.add(new Symbol(","));
			substring.add(arithmeticExpression());
			substring.add(new Symbol(")"));
			funRetStr.add(substring);
		}
		return funRetStr;
	}


	// functions_returning_numerics::=
	// 	LENGTH (string_expression) |
	// 	LOCATE (string_expression, string_expression[, arithmetic_expression]) |
	// 	ABS (arithmetic_expression) |
	// 	SQRT (arithmetic_expression)
	private Alternation funRetNum;
	protected Parser functionsReturningNumerics() {
		if(funRetNum == null) {
			funRetNum = new Alternation();
			
			Sequence length = new Sequence();
			length.add(new Literal("LENGTH"));
			length.add(new Symbol("("));
			length.add(stringExpression());
			length.add(new Symbol(")"));
			funRetNum.add(length);
			
			Sequence locate = new Sequence();
			locate.add(new Literal("LOCATE"));
			locate.add(new Symbol("("));
			locate.add(stringExpression());
			locate.add(new Symbol(","));
			locate.add(stringExpression());
				Sequence start = new Sequence();
				start.add(new Symbol(","));
				start.add(arithmeticExpression());
			locate.add(new Optional(start));
			locate.add(new Symbol(")"));
			funRetNum.add(locate);

			Sequence abs = new Sequence();
			abs.add(new Literal("ABS"));
			abs.add(new Symbol("("));
			abs.add(arithmeticExpression());
			abs.add(new Symbol(")"));
			funRetNum.add(abs);

			Sequence sqrt = new Sequence();
			sqrt.add(new Literal("SQRT"));
			sqrt.add(new Symbol("("));
			sqrt.add(arithmeticExpression());
			sqrt.add(new Symbol(")"));
			funRetNum.add(sqrt);
		}
		return funRetNum;
	}
	
	protected Parser abstractSchemaName() {
		return new Word();
	}

	protected Parser identifier() {
		return new Word();
	}

	protected Parser cmpField() {
		return new Word();
	}
	
	protected Parser singleValuedCmrField() {
		return new Word();
	}
	
	protected Parser collectionValuedCmrField() {
		return new Word();
	}
	
	protected Parser identificationVariable() {
		return new Word();
	}

	protected Parser patternValue() {
		return new Word();
	}

	protected Parser escapeCharacter() {
		return new Word();
	}
}
