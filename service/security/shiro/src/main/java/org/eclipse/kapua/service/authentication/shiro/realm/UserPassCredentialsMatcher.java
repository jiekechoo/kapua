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
package org.eclipse.kapua.service.authentication.shiro.realm;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.eclipse.kapua.service.authentication.ApiKeyCredentials;
import org.eclipse.kapua.service.authentication.credential.Credential;
import org.eclipse.kapua.service.authentication.credential.CredentialType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * {@link ApiKeyCredentials} credential matcher implementation
 * 
 * @since 1.0
 * 
 */
public class UserPassCredentialsMatcher implements CredentialsMatcher {

    @SuppressWarnings("unused")
    private static final Logger s_logger = LoggerFactory.getLogger(UserPassCredentialsMatcher.class);

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authenticationToken, AuthenticationInfo authenticationInfo) {

        //
        // Token data
        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String tokenUsername = token.getUsername();
        String tokenPassword = new String(token.getPassword());

        //
        // Info data
        LoginAuthenticationInfo info = (LoginAuthenticationInfo) authenticationInfo;
        Credential infoCredential = (Credential) info.getCredentials();

        //
        // Match token with info
        boolean credentialMatch = false;
        if (tokenUsername.equals(infoCredential.getKey()) && //
                CredentialType.PASSWORD.equals(infoCredential.getCredentialType()) && //
                BCrypt.checkpw(tokenPassword, infoCredential.getSecret())) {
            credentialMatch = true;

            // FIXME: if true cache token password for authentication performance improvement
        }

        return credentialMatch;
    }

}
