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
package org.eclipse.kapua.service.authorization.access;

import java.security.Permissions;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.eclipse.kapua.model.KapuaEntityCreator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.id.KapuaIdAdapter;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.subject.SubjectType;

/**
 * {@link AccessInfo} creator definition.<br>
 * It is used to assign a set of {@link Domain}s and {@link Permission}s to the referenced {@link User}.<br>
 * 
 * @since 1.0.0
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "subjectType",
        "subjectId",
        "roleIds",
        "permissions" }, factoryClass = AccessInfoXmlRegistry.class, factoryMethod = "newAccessInfoCreator")
public interface AccessInfoCreator extends KapuaEntityCreator<AccessInfo> {

    /**
     * Returns the {@link SubjectType} of this {@link AccessInfoCreator}.
     * 
     * @return The {@link SubjectType} of this {@link AccessInfoCreator}.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectType")
    public SubjectType getSubjectType();

    /**
     * Sets the {@link SubjectType} of this {@link AccessInfoCreator}.
     * 
     * @param subjectType
     *            The {@link SubjectType} of this {@link AccessInfoCreator}.
     * @since 1.0.0
     */
    public void setSubjectType(SubjectType subjectType);

    /**
     * Return the {@link AccessInfoCreator} subject id.
     * 
     * @return The {@link AccessInfoCreator} subject id.
     * @since 1.0.0
     */
    @XmlElement(name = "subjectId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public KapuaId getSubjectId();

    /**
     * Sets the {@link AccessInfoCreator} subject id.
     * 
     * @param subjectId
     *            The {@link AccessInfoCreator} subject id.
     * @since 1.0.0
     */
    public void setSubjectId(KapuaId subjectId);

    /**
     * Sets the set of {@link Domain} ids to assign to the {@link AccessInfo} created entity.
     * It up to the implementation class to make a clone of the set or use the given set.
     * 
     * @param roleIds
     *            The set of {@link Domain} ids.
     * @since 1.0.0
     */
    public void setRoleIds(Set<KapuaId> roleIds);

    /**
     * Gets the set of {@link Domain} ids added to this {@link AccessInfoCreator}.
     * The implementation must return the reference of the set and not make a clone.
     * 
     * @return The set of {@link Domain} ids.
     * @since 1.0.0
     */
    @XmlElementWrapper(name = "roleIds")
    @XmlElement(name = "roleId")
    @XmlJavaTypeAdapter(KapuaIdAdapter.class)
    public Set<KapuaId> getRoleIds();

    /**
     * Sets the set of {@link Permissions} to assign to the {@link AccessInfo} created entity.
     * It up to the implementation class to make a clone of the set or use the given set.
     * 
     * @param permissions
     *            The set of {@link Permissions}.
     * @since 1.0.0
     */
    public void setPermissions(Set<Permission> permissions);

    /**
     * Gets the set of {@link Permission} added to this {@link AccessInfoCreator}.
     * The implementation must return the reference of the set and not make a clone.
     * 
     * @return The set of {@link Permission}.
     * @since 1.0.0
     */
    @XmlElementWrapper(name = "permissions")
    @XmlElement(name = "permission")
    public <P extends Permission> Set<P> getPermissions();

}
