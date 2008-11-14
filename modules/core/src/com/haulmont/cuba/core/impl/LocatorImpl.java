/*
 * Copyright (c) 2008 Haulmont Technology Ltd. All Rights Reserved.
 * Haulmont Technology proprietary and confidential.
 * Use is subject to license terms.

 * Author: Konstantin Krivopustov
 * Created: 03.11.2008 19:02:51
 * $Id$
 */
package com.haulmont.cuba.core.impl;

import com.haulmont.cuba.core.Locator;
import com.haulmont.cuba.core.PersistenceProvider;
import com.haulmont.cuba.core.TransactionAdapter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

public class LocatorImpl extends Locator
{
    private Context jndiContext;

    protected Context __getJndiContextImpl() {
        if (jndiContext == null) {
            try {
                jndiContext = new InitialContext();
            } catch (NamingException e) {
                throw new RuntimeException(e);
            }
        }
        return jndiContext;
    }

    protected Object __lookupLocal(String name) {
        Context ctx = __getJndiContextImpl();
        try {
            return ctx.lookup(name + "/local");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Object __lookupRemote(String name) {
        Context ctx = __getJndiContextImpl();
        try {
            return ctx.lookup(name + "/remote");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
    }

    protected TransactionAdapter __createTransaction() {
        Context ctx = __getJndiContextImpl();
        TransactionManager tm;
        try {
            tm = (TransactionManager) ctx.lookup("java:/TransactionManager");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }
        return new JtaTransactionAdapter(tm);
    }

}
