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
package org.eclipse.kapua.service.authentication;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.service.authentication.credential.CredentialSubjectType;

/**
 * Username and password {@link LoginCredentials} definition.
 * 
 * @since 1.0.0
 * 
 */
@XmlRootElement(name = "usernamePasswordCredentials")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = { "username", "password" }, factoryClass = AuthenticationXmlRegistry.class, factoryMethod = "newUsernamePasswordCredentials")
public interface UsernamePasswordCredentials extends LoginCredentials {

    /**
     * Returns the {@link CredentialSubjectType}.
     * 
     * @return The {@link CredentialSubjectType}.
     * @since 1.0.0
     */
    public CredentialSubjectType getSubjectType();

    /**
     * Sets the {@link CredentialSubjectType}.
     * 
     * @param subjectType
     *            The {@link CredentialSubjectType}.
     * @since 1.0.0
     */
    public void setSubjectType(CredentialSubjectType subjectType);

    /**
     * Returns the username.
     * 
     * @return The username.
     * @since 1.0.0
     */
    public String getUsername();

    /**
     * Set the username
     * 
     * @param username
     * @since 1.0.0
     */
    public void setUsername(String username);

    /**
     * Returns the password.
     * 
     * @return The password.
     * @since 1.0.0
     */
    @XmlJavaTypeAdapter(StringToCharArrayAdapter.class)
    public char[] getPassword();

    /**
     * Sets the password.
     * 
     * @param password
     *            The password.
     * @since 1.0.0
     */
    public void setPassword(char[] password);
}
