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
package org.eclipse.kapua.service.authentication.credential.shiro;

import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialListResult;
import org.eclipse.kapua.service.authentication.credential.CredentialQuery;
import org.eclipse.kapua.service.authentication.credential.CredentialSubjectType;
import org.eclipse.kapua.service.authentication.credential.CredentialType;

/**
 * {@link CredentialFactory} implementation.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class CredentialFactoryImpl implements CredentialFactory {

    @Override
    public CredentialCreatorImpl newCreator(
            KapuaId scopeId,
            CredentialSubjectType subjectType,
            KapuaId subjectId,
            CredentialType type,
            String key,
            String secret) {
        return new CredentialCreatorImpl(
                scopeId,
                subjectType,
                subjectId,
                type,
                key,
                secret);
    }

    @Override
    public CredentialListResult newCredentialListResult() {
        return new CredentialListResultImpl();
    }

    @Override
    public Credential newCredential() {
        return new CredentialImpl();
    }

    @Override
    public CredentialQuery newQuery(KapuaId scopeId) {
        return new CredentialQueryImpl(scopeId);
    }
}
