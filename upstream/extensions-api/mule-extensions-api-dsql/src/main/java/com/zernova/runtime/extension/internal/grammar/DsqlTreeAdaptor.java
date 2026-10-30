/*
 * Copyright 2023 Salesforce, Inc. All rights reserved.
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
/* ZERNOVA namespace refactor Copyright (c) 2026 ZerNova. SPDX-License-Identifier: CPAL-1.0. */
package com.zernova.runtime.extension.internal.grammar;



import com.zernova.runtime.extension.internal.dsql.ZernovaDsqlParser;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonTreeAdaptor;
import org.antlr.runtime.tree.TreeAdaptor;

/**
 * A DSQL specific implementation of {@link TreeAdaptor} used to create a tree.
 *
 * @since 1.0
 */
public class DsqlTreeAdaptor extends CommonTreeAdaptor {

  @Override
  public Object create(Token payload) {
    // TODO: Check if there is a better way to do this.
    Object retVal;
    if (payload != null) {
      switch (payload.getType()) {
        case ZernovaDsqlParser.SELECT: {
          retVal = new SelectDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.FROM: {
          retVal = new FromDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.WHERE: {
          retVal = new ExpressionDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.AND: {
          retVal = new AndDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.OR: {
          retVal = new OrDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.NOT: {
          retVal = new NotDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.OPENING_PARENTHESIS: {
          retVal = new OpeningParenthesesDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.COMPARATOR:
        case ZernovaDsqlParser.OPERATOR: {
          retVal = new OperatorDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.ORDER: {
          retVal = new OrderByDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.ASC: {
          retVal = new DirectionDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.DESC: {
          retVal = new DirectionDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.LIMIT: {
          retVal = new LimitDsqlNode(payload);
          break;
        }
        case ZernovaDsqlParser.OFFSET: {
          retVal = new OffsetDsqlNode(payload);
          break;
        }
        default: {
          retVal = new BaseDsqlNode(payload);
        }
      }
    } else {
      retVal = new BaseDsqlNode(payload);
    }
    return retVal;
  }

  @Override
  public Object errorNode(TokenStream input, Token start, Token stop,
                          RecognitionException e) {
    return new DsqlErrorNode(input, start, stop, e);
  }

}
