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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.apache.shiro.codec.Base64;
import org.eclipse.kapua.KapuaEntityNotFoundException;
import org.eclipse.kapua.KapuaErrorCodes;
import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.KapuaRuntimeException;
import org.eclipse.kapua.commons.jpa.EntityManager;
import org.eclipse.kapua.commons.model.query.predicate.AndPredicate;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.commons.service.internal.AbstractKapuaService;
import org.eclipse.kapua.commons.util.ArgumentValidator;
import org.eclipse.kapua.commons.util.KapuaExceptionUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.locator.KapuaProvider;
import org.eclipse.kapua.model.id.KapuaId;
import org.eclipse.kapua.model.query.KapuaQuery;
import org.eclipse.kapua.model.query.predicate.KapuaPredicate;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialCreator;
import org.eclipse.kapua.service.authentication.credential.CredentialFactory;
import org.eclipse.kapua.service.authentication.credential.CredentialListResult;
import org.eclipse.kapua.service.authentication.credential.CredentialPredicates;
import org.eclipse.kapua.service.authentication.credential.CredentialQuery;
import org.eclipse.kapua.service.authentication.credential.CredentialService;
import org.eclipse.kapua.service.authentication.credential.CredentialSubjectType;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.eclipse.kapua.service.authentication.shiro.AuthenticationEntityManagerFactory;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSetting;
import org.eclipse.kapua.service.authentication.shiro.setting.KapuaAuthenticationSettingKeys;
import org.eclipse.kapua.service.authorization.AuthorizationService;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.permission.Actions;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;

/**
 * {@link CredentialService} implementation using JPA and SQL database.
 * 
 * @since 1.0.0
 */
@KapuaProvider
public class CredentialServiceImpl extends AbstractKapuaService implements CredentialService {

    private static final String SHA1PRNG = "SHA1PRNG";
    private static final Domain credentialDomain = new CredentialDomain();

    public CredentialServiceImpl() {
        super(AuthenticationEntityManagerFactory.getInstance());
    }

    @Override
    public Credential create(CredentialCreator credentialCreator)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(credentialCreator, "credentialCreator");
        ArgumentValidator.notNull(credentialCreator.getScopeId(), "credentialCreator.scopeId");
        ArgumentValidator.notNull(credentialCreator.getType(), "credentialCreator.type");

        switch (credentialCreator.getType()) {
        case PASSWORD:
            ArgumentValidator.notEmptyOrNull(credentialCreator.getKey(), "credentialCreator.key");
            ArgumentValidator.notEmptyOrNull(credentialCreator.getPlainSecret(), "credentialCreator.plainSecret");
            break;
        case API_KEY:
        case JWT:
        default:
            break;
        }

        //
        // Check access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.write, credentialCreator.getScopeId()));

        //
        // Generate credentials key and secret in case of API_KEY creator.
        switch (credentialCreator.getType()) {
        case API_KEY:
            generateApiKey(credentialCreator);
            break;
        case PASSWORD:
        case JWT:
        default:
            break;
        }

        //
        // Do create
        return entityManagerSession.onInsert(em -> {

            Credential credential = CredentialDAO.create(em, credentialCreator);

            //
            // Return only on creation the full key to the caller
            switch (credentialCreator.getType()) {
            case API_KEY:
                credential.setKey(credentialCreator.getKey() + credentialCreator.getPlainSecret());
                break;
            case PASSWORD:
            case JWT:
            default:
                break;
            }

            return credential;
        });
    }

    @Override
    public Credential update(Credential credential)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(credential, "credential");
        ArgumentValidator.notNull(credential.getId(), "credential.id");
        ArgumentValidator.notNull(credential.getScopeId(), "credential.scopeId");

        //
        // Check access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.write, credential.getScopeId()));

        return entityManagerSession.onTransactedResult(em -> {
            Credential currentCredential = CredentialDAO.find(em, credential.getId());

            if (currentCredential == null) {
                throw new KapuaEntityNotFoundException(Credential.TYPE, credential.getId());
            }

            // Passing attributes??
            return CredentialDAO.update(em, credential);
        });
    }

    @Override
    public Credential find(KapuaId scopeId, KapuaId credentialId)
            throws KapuaException {
        // Validation of the fields
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(credentialId, "credentialId");

        //
        // Check Access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.read, scopeId));

        return entityManagerSession.onResult(em -> CredentialDAO.find(em, credentialId));
    }

    @Override
    public CredentialListResult query(KapuaQuery<Credential> query)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.read, query.getScopeId()));

        return entityManagerSession.onResult(em -> CredentialDAO.query(em, query));
    }

    @Override
    public long count(KapuaQuery<Credential> query)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(query, "query");
        ArgumentValidator.notNull(query.getScopeId(), "query.scopeId");

        //
        // Check Access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.read, query.getScopeId()));

        return entityManagerSession.onResult(em -> CredentialDAO.count(em, query));
    }

    @Override
    public void delete(KapuaId scopeId, KapuaId credentialId)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(credentialId, "credential.id");
        ArgumentValidator.notNull(scopeId, "credential.scopeId");

        //
        // Check Access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.delete, scopeId));

        entityManagerSession.onTransactedAction(em -> {
            if (CredentialDAO.find(em, credentialId) == null) {
                throw new KapuaEntityNotFoundException(Credential.TYPE, credentialId);
            }

            CredentialDAO.delete(em, credentialId);
        });
    }

    @Override
    public CredentialListResult findBySubject(KapuaId scopeId, CredentialSubjectType subjectType, KapuaId subjectId, CredentialType type)
            throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(scopeId, "scopeId");
        ArgumentValidator.notNull(subjectType, "subjectType");
        ArgumentValidator.notNull(subjectId, "subjectId");

        //
        // Check Access
        KapuaLocator locator = KapuaLocator.getInstance();
        AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
        PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
        authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.read, scopeId));

        //
        // Build query
        CredentialQuery query = new CredentialQueryImpl(scopeId);

        KapuaPredicate subjectTypePredicate = new AttributePredicate<CredentialSubjectType>(CredentialPredicates.CREDENTIAL_SUBJECT_TYPE, subjectType);
        KapuaPredicate subjectIdPredicate = new AttributePredicate<KapuaId>(CredentialPredicates.CREDENTIAL_SUBJECT_ID, subjectId);

        AndPredicate andPredicate = new AndPredicate();
        andPredicate.and(subjectTypePredicate);
        andPredicate.and(subjectIdPredicate);

        if (type != null) {
            KapuaPredicate typePredicate = new AttributePredicate<CredentialType>(CredentialPredicates.TYPE, type);
            andPredicate.and(typePredicate);
        }

        query.setPredicate(andPredicate);

        //
        // Query and return result
        return query(query);
    }

    @Override
    public Credential findByKey(CredentialSubjectType subjectType, String apiKey) throws KapuaException {
        //
        // Argument Validation
        ArgumentValidator.notNull(subjectType, "subjectType");
        ArgumentValidator.notEmptyOrNull(apiKey, "apiKey");

        //
        // Do the find
        Credential credential = null;
        EntityManager em = AuthenticationEntityManagerFactory.getEntityManager();
        try {

            //
            // Build search query
            KapuaAuthenticationSetting setting = KapuaAuthenticationSetting.getInstance();
            int keyLength = setting.getInt(KapuaAuthenticationSettingKeys.AUTHENTICATION_CREDENTIAL_APIKEY_KEY_LENGTH);
            String apiKeyValue = apiKey.substring(0, keyLength);

            //
            // Build query
            AttributePredicate<CredentialSubjectType> subjectTypePredicate = new AttributePredicate<>(CredentialPredicates.CREDENTIAL_SUBJECT_TYPE, CredentialSubjectType.USER);
            AttributePredicate<CredentialType> typePredicate = new AttributePredicate<>(CredentialPredicates.TYPE, subjectType);
            AttributePredicate<String> keyPredicate = new AttributePredicate<>(CredentialPredicates.KEY, apiKeyValue);

            AndPredicate andPredicate = new AndPredicate();
            andPredicate.and(subjectTypePredicate);
            andPredicate.and(typePredicate);
            andPredicate.and(keyPredicate);

            KapuaQuery<Credential> query = new CredentialQueryImpl();
            query.setPredicate(andPredicate);

            //
            // Query
            CredentialListResult credentialListResult = CredentialDAO.query(em, query);

            //
            // Parse the result
            if (credentialListResult != null && credentialListResult.getSize() == 1) {
                credential = credentialListResult.getItem(0);
            }

        } catch (Exception e) {
            throw KapuaExceptionUtils.convertPersistenceException(e);
        } finally {
            em.close();
        }

        //
        // Check Access
        if (credential != null) {
            KapuaLocator locator = KapuaLocator.getInstance();
            AuthorizationService authorizationService = locator.getService(AuthorizationService.class);
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            authorizationService.checkPermission(permissionFactory.newPermission(credentialDomain, Actions.read, credential.getId()));
        }

        return credential;
    }

    ///////////////////////////////////
    //
    // Private Methods
    //
    ///////////////////////////////////
    /**
     * Generates random strings value for a {@link CredentialType#API_KEY} {@link CredentialCreator}.<br>
     * It makes use of {@link SecureRandom} for the generation of the required random values.<br>
     * It is possible to manage {@link CredentialType#API_KEY} length via {@link KapuaAuthenticationSetting}.
     * 
     * @param credentialCreator
     *            The {@link CredentialCreator} to populate.
     * @since 1.0.0
     */
    private void generateApiKey(CredentialCreator credentialCreator) {

        // Get Secure Random instance
        SecureRandom random = null;
        try {
            random = SecureRandom.getInstance(SHA1PRNG);
        } catch (NoSuchAlgorithmException e) {
            throw new KapuaRuntimeException(KapuaErrorCodes.INTERNAL_ERROR, e, SHA1PRNG);
        }

        // Get Configuration values for key generation
        KapuaAuthenticationSetting setting = KapuaAuthenticationSetting.getInstance();
        int keyLength = setting.getInt(KapuaAuthenticationSettingKeys.AUTHENTICATION_CREDENTIAL_APIKEY_KEY_LENGTH);
        int secretLength = setting.getInt(KapuaAuthenticationSettingKeys.AUTHENTICATION_CREDENTIAL_APIKEY_SECRET_LENGTH);

        // Generate key value
        byte[] bKey = new byte[keyLength];
        random.nextBytes(bKey);
        String key = Base64.encodeToString(bKey);

        // Generate secret value
        byte[] bSecret = new byte[secretLength];
        random.nextBytes(bSecret);
        String plainSecret = Base64.encodeToString(bSecret);

        // Populate CredentialCreator object
        credentialCreator.setKey(key);
        credentialCreator.setPlainSecret(plainSecret);
    }

    @SuppressWarnings("unused")
    private long countExistingCredentials(KapuaId scopeId, CredentialSubjectType subjectType, KapuaId subjectId, CredentialType type)
            throws KapuaException {
        KapuaLocator locator = KapuaLocator.getInstance();
        CredentialFactory credentialFactory = locator.getFactory(CredentialFactory.class);

        AttributePredicate<CredentialSubjectType> subjectTypePredicate = new AttributePredicate<>(CredentialPredicates.CREDENTIAL_SUBJECT_TYPE, subjectType);
        AttributePredicate<KapuaId> subjectIdPredicate = new AttributePredicate<>(CredentialPredicates.CREDENTIAL_SUBJECT_ID, subjectId);
        AttributePredicate<CredentialType> typePredicate = new AttributePredicate<>(CredentialPredicates.TYPE, type);

        AndPredicate andPredicate = new AndPredicate();
        andPredicate.and(subjectTypePredicate);
        andPredicate.and(subjectIdPredicate);
        andPredicate.and(typePredicate);

        KapuaQuery<Credential> credentialQuery = credentialFactory.newQuery(scopeId);
        credentialQuery.setPredicate(andPredicate);

        return count(credentialQuery);
    }
}
