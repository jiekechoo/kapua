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
package org.eclipse.kapua.service.authentication.token;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.model.KapuaUpdatableEntity;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialSubjectType;

/**
 * {@link AccessToken} entity.<br>
 * The {@link AccessToken} entity is returned to the caller of a successful login. <br>
 * Then all subsequent action on the system must be performed providing the {@link AccessToken#getTokenId()}
 * instead of using the {@link Credential} used during login.<br>
 * The {@link AccessToken} also acts as a session tracker as all action performed on the system are associated with a
 * {@link AccessToken#getTokenId()}.
 * 
 * 
 * @since 1.0.0
 *
 */
@XmlRootElement(name = "accessToken")
@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(propOrder = {
        "subjectType", //
        "subjectId", //
        "tokenId", //
        "expiresOn",//
})// , //
  // factoryClass = AccessTokenXmlRegistry.class, factoryMethod = "newAccessToken")
public interface AccessToken extends KapuaUpdatableEntity {

    static public final String TYPE = "accessToken";

    default public String getType() {
        return TYPE;
    }

    /**
     * Returns the {@link CredentialSubjectType} of this {@link AccessToken}.
     * 
     * @return The {@link CredentialSubjectType} of this {@link AccessToken}.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectType")
    public CredentialSubjectType getSubjectType();

    /**
     * Sets the {@link CredentialSubjectType} of this {@link AccessToken}.
     * 
     * @param subjectType
     *            The {@link CredentialSubjectType} of this {@link AccessToken}.
     * @since 1.0.0
     */
    public void setSubjectType(CredentialSubjectType subjectType);

    /**
     * Returns the {@link AccessToken} subject id.
     * 
     * @return The {@link AccessToken} subject id.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getSubjectId();

    /**
     * Sets the {@link AccessToken} subject id.
     * 
     * @param subjectId
     *            The {@link AccessToken} subject id.
     * @since 1.0.0
     */
    public void setSubjectId(KapuaId subjectId);

    /**
     * Return the token identifier
     * 
     * @return the token identifier
     * @since 1.0
     */
    @XmlElement(name = "tokenId")
    public String getTokenId();

    /**
     * Sets the token id
     * 
     * @param tokenId
     *            The token id.
     * @since 1.0
     */
    public void setTokenId(String tokenId);

    /**
     * Gets the expire date of this token.
     * 
     * @since 1.0
     */
    @XmlElement(name = "expiresOn")
    public Date getExpiresOn();

    /**
     * Sets the expire date of this token.
     * 
     * @param expiresOn
     *            The expire date of this token.
     * @since 1.0
     */
    public void setExpiresOn(Date expiresOn);

}
