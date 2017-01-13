/*******************************************************************************
 * Copyright (c) 2011, 2017 Eurotech and/or its affiliates and others
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
package org.eclipse.kapua.service.authentication.token.shiro;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.AbstractKapuaEntity;
import org.eclipse.kapua.commons.model.AbstractKapuaUpdatableEntity;
import org.eclipse.kapua.commons.model.id.IdGenerator;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authentication.token.AccessToken;
import org.eclipse.kapua.service.authorization.subject.Subject;
import org.eclipse.kapua.service.authorization.subject.shiro.SubjectImpl;

/**
 * Access token entity implementation.
 * 
 * @since 1.0
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name = "AccessToken")
@Table(name = "atht_access_token")
public class AccessTokenImpl extends AbstractKapuaUpdatableEntity implements AccessToken {

    private static final long serialVersionUID = -6003387376828196787L;

    @Embedded
    private SubjectImpl subject;

    @Basic
    @Column(name = "token_id", updatable = false, nullable = false)
    private String tokenId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "expires_on", nullable = false)
    private Date expiresOn;

    /**
     * Constructor
     */
    public AccessTokenImpl() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param scopeId
     * @param subject
     * @param tokenId
     * @param expiresOn
     */
    public AccessTokenImpl(KapuaId scopeId, Subject subject, String tokenId, Date expiresOn) {
        super(scopeId);
        setSubject(subject);
        setTokenId(tokenId);
        setExpiresOn(expiresOn);
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public void setSubject(Subject subject) {
        if (subject != null) {
            this.subject = new SubjectImpl(subject);
        } else {
            this.subject = null;
        }
    }

    @Override
    public String getTokenId() {
        return tokenId;
    }

    @Override
    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    @Override
    public Date getExpiresOn() {
        return expiresOn;
    }

    @Override
    public void setExpiresOn(Date expiresOn) {
        this.expiresOn = expiresOn;
    }

    /**
     * The {@link AbstractKapuaUpdatableEntity#prePersistAction} is overridden because the property {@link AbstractKapuaEntity#createdBy}
     * must be set to the current userId instead of the user in session, which is not set at the time of the creation of this {@link AccessToken}.
     * 
     * @since 1.0.0
     */
    @Override
    protected void prePersistsAction()
            throws KapuaException {
        this.id = new KapuaEid(IdGenerator.generate());
        this.createdBy = new KapuaEid(BigInteger.ONE);
        this.createdOn = new Date();
        this.modifiedBy = this.createdBy;
        this.modifiedOn = this.createdOn;
    }
}
