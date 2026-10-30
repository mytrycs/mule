/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.grammar;

import static java.lang.Integer.parseInt;

import com.zernova.runtime.extension.api.dsql.EntityType;
import com.zernova.runtime.extension.api.dsql.Expression;
import com.zernova.runtime.extension.api.dsql.Field;
import com.zernova.runtime.extension.api.dsql.QueryBuilder;
import com.zernova.runtime.extension.api.dsql.Value;
import com.zernova.runtime.extension.internal.dsql.DefaultQueryBuilder;
import com.zernova.runtime.extension.internal.dsql.ZernovaDsqlParser;
import com.zernova.runtime.extension.internal.expression.And;
import com.zernova.runtime.extension.internal.expression.FieldComparison;
import com.zernova.runtime.extension.internal.expression.Not;
import com.zernova.runtime.extension.internal.expression.Or;
import com.zernova.runtime.extension.internal.operator.BinaryOperator;
import com.zernova.runtime.extension.internal.value.BooleanValue;
import com.zernova.runtime.extension.internal.value.DateTimeValue;
import com.zernova.runtime.extension.internal.value.DateValue;
import com.zernova.runtime.extension.internal.value.IdentifierValue;
import com.zernova.runtime.extension.internal.value.IntegerValue;
import com.zernova.runtime.extension.internal.value.ZernovaExpressionValue;
import com.zernova.runtime.extension.internal.value.NullValue;
import com.zernova.runtime.extension.internal.value.NumberValue;
import com.zernova.runtime.extension.internal.value.StringValue;
import com.zernova.runtime.extension.internal.value.UnknownValue;

import java.util.List;
import java.util.Stack;

/**
 * Default implementation of {@link DsqlGrammarVisitor}.
 *
 * @since 1.0
 */
public class DefaultDsqlGrammarVisitor implements DsqlGrammarVisitor {

  private final QueryBuilder queryBuilder;
  private final Stack<Expression> expressions = new Stack<>();
  private int expressionLevel;

  public DefaultDsqlGrammarVisitor() {
    queryBuilder = DefaultQueryBuilder.getInstance();
    expressionLevel = 0;
  }

  @Override
  public void visit(BaseDsqlNode baseDsqlNode) {
    // Too generic. Empty on purpose.
  }

  @Override
  public void visit(SelectDsqlNode selectDsqlNode) {
    List<DsqlNode> children = selectDsqlNode.getChildren();
    for (final DsqlNode dsqlNode : children) {
      if (dsqlNode.getType() != ZernovaDsqlParser.IDENT
          && dsqlNode.getType() != ZernovaDsqlParser.STRING_LITERAL
          && dsqlNode.getType() != ZernovaDsqlParser.ASTERIX) {
        dsqlNode.accept(this);
      } else {
        String nodeText = dsqlNode.getText();
        if (dsqlNode.getType() == ZernovaDsqlParser.STRING_LITERAL) {
          nodeText = StringValue.fromLiteral(nodeText).getValue();
        }
        queryBuilder.addField(new Field(nodeText, "string"));
      }
    }
  }

  @Override
  public void visit(FromDsqlNode fromDsqlNode) {
    List<DsqlNode> children = fromDsqlNode.getChildren();

    for (final DsqlNode dsqlNode : children) {
      String text = getTextIfStringLiteral(dsqlNode);
      queryBuilder.setType(new EntityType(text));
    }
  }

  private String getTextIfStringLiteral(DsqlNode dsqlNode) {
    String text = dsqlNode.getText();
    if (dsqlNode.getType() == ZernovaDsqlParser.STRING_LITERAL) {
      return StringValue.fromLiteral(text).getValue();
    } else {
      return text;
    }
  }

  @Override
  public void visit(ExpressionDsqlNode expressionDsqlNode) {
    List<DsqlNode> children = expressionDsqlNode.getChildren();

    for (final DsqlNode dsqlNode : children) {
      int type = dsqlNode.getType();
      if (type == ZernovaDsqlParser.AND || type == ZernovaDsqlParser.OR
          || type == ZernovaDsqlParser.NOT) {
        dsqlNode.accept(this);
      } else if (type == ZernovaDsqlParser.OPERATOR || type == ZernovaDsqlParser.COMPARATOR) {
        final List<DsqlNode> operatorChildren = dsqlNode.getChildren();
        DsqlNode fieldNode = operatorChildren.get(0);
        String fieldName = getTextIfStringLiteral(fieldNode);
        final Field field = new Field(fieldName);
        final DsqlNode valueNode = operatorChildren.get(1);
        final Value value = buildValue(valueNode);
        final FieldComparison expression = new FieldComparison(getOperatorFor(dsqlNode.getText()), field, value);
        queryBuilder.setFilterExpression(expression);
      } else if (type == ZernovaDsqlParser.OPENING_PARENTHESIS) {
        dsqlNode.accept(this);
      }
    }
  }

  private Value buildValue(DsqlNode node) {
    Value value;
    switch (node.getType()) {
      case ZernovaDsqlParser.DOUBLE_LITERAL:
        value = NumberValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.INTEGER_LITERAL:
        value = IntegerValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.BOOLEAN_LITERAL:
        value = BooleanValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.DATE_LITERAL:
        value = DateValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.DATE_TIME_LITERAL:
        value = DateTimeValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.NULL_LITERAL:
        value = new NullValue();
        break;
      case ZernovaDsqlParser.IDENT:
        value = IdentifierValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.MULE_EXPRESSION:
        value = ZernovaExpressionValue.fromLiteral(node.getText());
        break;
      case ZernovaDsqlParser.STRING_LITERAL:
        value = StringValue.fromLiteral(node.getText());
        break;
      default:
        value = UnknownValue.fromLiteral(node.getText());
        break;
    }
    return value;
  }

  @Override
  public void visit(AndDsqlNode andDsqlNode) {
    List<DsqlNode> children = andDsqlNode.getChildren();
    expressionLevel++;
    for (DsqlNode dsqlNode : children) {
      dsqlNode.accept(this);
    }
    expressionLevel--;
    Expression rightExpression = expressions.pop();
    Expression leftExpression = expressions.pop();
    And andExpression = new And(leftExpression, rightExpression);
    putExpression(andExpression);
  }

  private void putExpression(Expression expression) {
    if (expressionLevel == 0) {
      queryBuilder.setFilterExpression(expression);
    } else {
      expressions.push(expression);
    }
  }

  @Override
  public void visit(OrDsqlNode orDsqlNode) {
    List<DsqlNode> children = orDsqlNode.getChildren();
    expressionLevel++;
    for (DsqlNode dsqlNode : children) {
      dsqlNode.accept(this);
    }
    expressionLevel--;
    Expression rightExpression = expressions.pop();
    Expression leftExpression = expressions.pop();
    putExpression(new Or(leftExpression, rightExpression));
  }

  @Override
  public void visit(NotDsqlNode notDsqlNode) {
    List<DsqlNode> children = notDsqlNode.getChildren();
    expressionLevel++;
    for (DsqlNode dsqlNode : children) {
      dsqlNode.accept(this);
    }
    expressionLevel--;
    Expression expression = expressions.pop();
    putExpression(new Not(expression));
  }

  @Override
  public void visit(OperatorDsqlNode operatorDsqlNode) {
    List<DsqlNode> children = operatorDsqlNode.getChildren();
    DsqlNode fieldNode = children.get(0);
    Field field = new Field(getTextIfStringLiteral(fieldNode));
    DsqlNode dsqlNode = children.get(1);
    Value<?> value = buildValue(dsqlNode);
    putExpression(new FieldComparison(getOperatorFor(operatorDsqlNode.getText()), field, value));
  }

  @Override
  public void visit(OpeningParenthesesDsqlNode openingParenthesesDsqlNode) {
    openingParenthesesDsqlNode.getChildren().forEach(node -> node.accept(this));
  }

  private BinaryOperator getOperatorFor(String symbol) {
    return (BinaryOperator) QueryModelOperatorFactory.getInstance().getOperator(symbol);
  }

  @Override
  public void visit(OrderByDsqlNode orderByDsqlNode) {
    List<DsqlNode> children = orderByDsqlNode.getChildren();
    for (final DsqlNode dsqlNode : children) {
      String text = getTextIfStringLiteral(dsqlNode);
      if (!(dsqlNode instanceof DirectionDsqlNode)) {
        queryBuilder.addOrderByField(new Field(text));
      } else {
        queryBuilder.setDirection(QueryModelDirectionFactory.getInstance().getDirection(text.toLowerCase()));
      }
    }
  }

  @Override
  public void visit(LimitDsqlNode limitDsqlNode) {
    limitDsqlNode.getChildren().forEach(node -> queryBuilder.setLimit(parseInt(node.getText())));
  }

  @Override
  public void visit(OffsetDsqlNode offsetDsqlNode) {
    offsetDsqlNode.getChildren().forEach(node -> queryBuilder.setOffset(parseInt(node.getText())));
  }

  public QueryBuilder getQueryBuilder() {
    return queryBuilder;
  }
}
