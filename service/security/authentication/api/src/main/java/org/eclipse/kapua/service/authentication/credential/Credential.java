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
package org.eclipse.kapua.service.authentication.credential;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.model.KapuaUpdatableEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;

/**
 * {@link Credential} definition.<br>
 * {@link Credential} entity is used to manage credentials that can be used to access the platform.
 * 
 * @since 1.0.0
 */
@XmlRootElement(name = "credential")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "subjectType", //
        "subjectId", //
        "credentialType", //
        "key",//
        "secret",//
}, //
        factoryClass = CredentialXmlRegistry.class, factoryMethod = "newCredential")
public interface Credential extends KapuaUpdatableEntity {

    public static final String TYPE = "credential";

    public default String getType() {
        return TYPE;
    }

    /**
     * Returns the {@link CredentialSubjectType} of this {@link Credential}.
     * 
     * @return The {@link CredentialSubjectType} of this {@link Credential}.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectType")
    public CredentialSubjectType getSubjectType();

    /**
     * Sets the {@link CredentialSubjectType} of this {@link Credential}.
     * 
     * @param subjectType
     *            The {@link CredentialSubjectType} of this {@link Credential}.
     * @since 1.0.0
     */
    public void setSubjectType(CredentialSubjectType subjectType);

    /**
     * Returns the {@link Credential} subject id.
     * 
     * @return The {@link Credential} subject id.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getSubjectId();

    /**
     * Sets the {@link Credential} subject id.
     * 
     * @param subjectId
     *            The {@link Credential} subject id.
     * @since 1.0.0
     */
    public void setSubjectId(KapuaId subjectId);

    /**
     * Returns the {@link Credential} type.
     * 
     * @return The {@link Credential} type.
     * @since 1.0.0
     */
    @XmlElement(name = "credentialType")
    public CredentialType getCredentialType();

    /**
     * Sets the {@link CredentialType}.
     * 
     * @param type
     *            The {@link CredentialType}.
     * @since 1.0.0
     */
    public void setCredentialType(CredentialType type);

    /**
     * Returns the {@link Credential} key.
     * 
     * @return The {@link Credential} key.
     * @since 1.0.0
     */
    @XmlElement(name = "key")
    public String getKey();

    /**
     * Sets the {@link Credential} key
     * 
     * @param key
     *            The {@link Credential} key
     * @since 1.0.0
     */
    public void setKey(String key);

    /**
     * Returns the {@link Credential} secret.
     * 
     * @return The {@link Credential} secret.
     * @since 1.0.0
     */
    @XmlElement(name = "secret")
    public String getSecret();

    /**
     * Sets the {@link Credential} secret.
     * 
     * @param secret
     *            The {@link Credential} secret.
     * @since 1.0.0
     */
    public void setSecret(String secret);

}
