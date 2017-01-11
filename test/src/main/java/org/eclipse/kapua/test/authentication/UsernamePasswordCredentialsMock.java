/*******************************************************************************
 * Copyright (c) 2011, 2016 Eurotech and/or its affiliates and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Eurotech - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.kapua.test.authentication;

import org.eclipse.kapua.service.authentication.UsernamePasswordCredentials;
import org.eclipse.kapua.service.authentication.credential.CredentialSubjectType;

public class UsernamePasswordCredentialsMock implements UsernamePasswordCredentials {

    private CredentialSubjectType subjectType;
    private String username;
    private char[] password;

    public UsernamePasswordCredentialsMock(CredentialSubjectType subjectType, String username, char[] password) {
        setSubjectType(subjectType);
        setUsername(username);
        setPassword(password);
    }

    @Override
    public CredentialSubjectType getSubjectType() {
        return subjectType;
    }

    @Override
    public void setSubjectType(CredentialSubjectType subjectType) {
        this.subjectType = subjectType;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public char[] getPassword() {
        return this.password;
    }

    @Override
    public void setPassword(char[] password) {
        this.password = password;
    }

}
