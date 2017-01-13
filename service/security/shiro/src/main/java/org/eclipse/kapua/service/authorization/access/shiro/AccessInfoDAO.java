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
package org.eclipse.kapua.service.authorization.access.shiro;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.jpa.EntityManager;
import org.eclipse.kapua.commons.service.internal.ServiceDAO;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.service.authorization.access.AccessInfo;
import org.eclipse.kapua.service.authorization.access.AccessInfoCreator;
import org.eclipse.kapua.service.authorization.access.AccessInfoListResult;
import org.eclipse.kapua.service.authorization.subject.SubjectFactory;

/**
 * {@link AccessInfo) DAO
 * 
 * @since 1.0.0
 *
 */
public class AccessInfoDAO extends ServiceDAO {

    /**
     * Creates and return new {@link AccessInfo)
     * 
     * @param em
     * @param creator
     * @return
     * @throws KapuaException
     * 
     * @since 1.0.0
     */
    public static AccessInfo create(EntityManager em, AccessInfoCreator creator)
            throws KapuaException {

        SubjectFactory subjectFactory = KapuaLocator.getInstance().getFactory(SubjectFactory.class);

        AccessInfo accessInfo = new AccessInfoImpl(creator.getScopeId());
        accessInfo.setSubject(subjectFactory.newSubject(creator.getSubjectType(), creator.getSubjectId()));
        return ServiceDAO.create(em, accessInfo);
    }

    /**
     * Find the {@link AccessInfo) by user {@link AccessInfo) identifier
     * 
     * @param em
     * @param accessInfoId
     * @return
     * @since 1.0.0
     */
    public static AccessInfo find(EntityManager em, KapuaId accessInfoId) {
        return em.find(AccessInfoImpl.class, accessInfoId);
    }

    /**
     * Delete the {@link AccessInfo) by {@link AccessInfo) identifier
     * 
     * @param em
     * @param accessInfoId
     * @since 1.0.0
     */
    public static void delete(EntityManager em, KapuaId accessInfoId) {
        ServiceDAO.delete(em, AccessInfoImpl.class, accessInfoId);
    }

    /**
     * Return the {@link AccessInfo) list matching the provided query
     * 
     * @param em
     * @param accessInfoQuery
     * @return
     * @throws KapuaException
     * @since 1.0.0
     */
    public static AccessInfoListResult query(EntityManager em, KapuaQuery<AccessInfo> accessInfoQuery)
            throws KapuaException {
        return ServiceDAO.query(em, AccessInfo.class, AccessInfoImpl.class, new AccessInfoListResultImpl(), accessInfoQuery);
    }

    /**
     * Return the {@link AccessInfo) count matching the provided query
     * 
     * @param em
     * @param accessInfoQuery
     * @return
     * @throws KapuaException
     * @since 1.0.0
     */
    public static long count(EntityManager em, KapuaQuery<AccessInfo> accessInfoQuery)
            throws KapuaException {
        return ServiceDAO.count(em, AccessInfo.class, AccessInfoImpl.class, accessInfoQuery);
    }

}
