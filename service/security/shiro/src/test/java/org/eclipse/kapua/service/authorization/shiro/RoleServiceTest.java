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
package org.eclipse.kapua.service.authorization.shiro;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.permission.Actions;
import org.eclipse.kapua.service.authorization.permission.Permission;
import org.eclipse.kapua.service.authorization.permission.PermissionFactory;
import org.eclipse.kapua.service.authorization.role.Role;
import org.eclipse.kapua.service.authorization.role.RoleCreator;
import org.eclipse.kapua.service.authorization.role.RoleListResult;
import org.eclipse.kapua.service.authorization.role.RolePermission;
import org.eclipse.kapua.service.authorization.role.RolePermissionListResult;
import org.eclipse.kapua.service.authorization.role.RolePermissionService;
import org.eclipse.kapua.service.authorization.role.RoleQuery;
import org.eclipse.kapua.service.authorization.role.RoleService;
import org.eclipse.kapua.service.authorization.role.shiro.RoleCreatorImpl;
import org.eclipse.kapua.service.authorization.role.shiro.RolePredicates;
import org.eclipse.kapua.service.authorization.role.shiro.RoleQueryImpl;
import org.eclipse.kapua.test.KapuaTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class RoleServiceTest extends KapuaTest {

    public static String DEFAULT_FILTER = "ath*.sql";
    public static String DROP_FILTER = "ath*_drop.sql";

    private static final Domain testDomain = new TestDomain();

    KapuaEid scope = new KapuaEid(BigInteger.valueOf(random.nextLong()));

    // Database fixtures

    @BeforeClass
    public static void beforeClass() throws KapuaException {
        enableH2Connection();
        scriptSession(AuthorizationEntityManagerFactory.getInstance(), DEFAULT_FILTER);
    }

    @AfterClass
    public static void afterClass() throws KapuaException {
        scriptSession(AuthorizationEntityManagerFactory.getInstance(), DROP_FILTER);
    }

    // Tests

    @Test
    public void testCreate()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();

            // Create permission
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            Permission permission = permissionFactory.newPermission(testDomain, Actions.read, scope);
            Set<Permission> permissions = new HashSet<>();
            permissions.add(permission);

            // Create role
            RoleCreator roleCreator = new RoleCreatorImpl(scope);
            roleCreator.setName("test-" + new Date().getTime());
            roleCreator.setPermissions(permissions);

            //
            // Create
            RoleService roleService = locator.getService(RoleService.class);
            Role role = roleService.create(roleCreator);

            //
            // Assert
            assertNotNull(role);
            assertNotNull(role.getId());
            assertEquals(roleCreator.getScopeId(), role.getScopeId());
            assertEquals(roleCreator.getName(), role.getName());
            assertNotNull(role.getCreatedBy());
            assertNotNull(role.getCreatedOn());
            assertNotNull(role.getModifiedBy());
            assertNotNull(role.getModifiedOn());

            RolePermissionService rolePermissionService = locator.getService(RolePermissionService.class);
            RolePermissionListResult rolePermissionsListResult = rolePermissionService.findByRoleId(role.getScopeId(), role.getId());
            List<RolePermission> rolePermissions = rolePermissionsListResult.getItems();
            assertNotNull(rolePermissions);
            assertEquals(1, rolePermissions.size());

            RolePermission rolePermission = rolePermissions.iterator().next();
            assertNotNull(rolePermission);
            assertNotNull(rolePermission.getId());
            assertNotNull(rolePermission.getCreatedBy());
            assertNotNull(rolePermission.getCreatedOn());
            assertEquals(role.getId(), rolePermission.getRoleId());
            assertEquals(permission.getDomain(), rolePermission.getPermission().getDomain());
            assertEquals(permission.getAction(), rolePermission.getPermission().getAction());
            assertEquals(permission.getTargetScopeId(), rolePermission.getPermission().getTargetScopeId());

            return null;
        });
    }

    @Test
    public void testUpdate()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();

            // Create permission
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            Permission permission1 = permissionFactory.newPermission(testDomain, Actions.read, scope);
            Permission permission3 = permissionFactory.newPermission(testDomain, Actions.delete, scope);

            Set<Permission> permissions = new HashSet<>();
            permissions.add(permission1);
            permissions.add(permission3);

            RoleCreator roleCreator = new RoleCreatorImpl(scope);
            roleCreator.setName("test-" + new Date().getTime());
            roleCreator.setPermissions(permissions);

            RoleService roleService = locator.getService(RoleService.class);
            Role role = roleService.create(roleCreator);

            RolePermissionService rolePermissionService = locator.getService(RolePermissionService.class);
            RolePermissionListResult rolePermissionsListResult = rolePermissionService.findByRoleId(role.getScopeId(), role.getId());
            List<RolePermission> rolePermissions = rolePermissionsListResult.getItems();

            assertNotNull(role);
            assertEquals(roleCreator.getPermissions().size(), rolePermissions.size());

            //
            // Update
            role.setName("updated-" + new Date().getTime());

            Role roleUpdated1 = roleService.update(role);

            //
            // Assert
            assertNotNull(roleUpdated1);
            assertEquals(role.getScopeId(), roleUpdated1.getScopeId());
            assertEquals(role.getScopeId(), roleUpdated1.getScopeId());
            assertEquals(role.getName(), roleUpdated1.getName());
            assertEquals(role.getCreatedBy(), roleUpdated1.getCreatedBy());
            assertEquals(role.getCreatedOn(), roleUpdated1.getCreatedOn());
            assertEquals(role.getModifiedBy(), roleUpdated1.getModifiedBy());
            assertNotEquals(role.getModifiedOn(), roleUpdated1.getModifiedOn());

            return null;
        });
    }

    @Test
    public void testFind()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();

            // Create permission
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            Permission permission = permissionFactory.newPermission(testDomain, Actions.read, scope);

            Set<Permission> permissions = new HashSet<>();
            permissions.add(permission);

            // Create Role
            RoleCreator roleCreator = new RoleCreatorImpl(scope);
            roleCreator.setName("test-" + new Date().getTime());
            roleCreator.setPermissions(permissions);

            RoleService roleService = locator.getService(RoleService.class);
            Role role = roleService.create(roleCreator);

            assertNotNull(role);
            assertNotNull(role.getId());
            assertEquals(roleCreator.getScopeId(), role.getScopeId());

            //
            // Find
            Role roleFound = roleService.find(scope, role.getId());

            //
            // Assert
            assertNotNull(roleFound);
            assertEquals(role.getScopeId(), roleFound.getScopeId());
            assertEquals(role.getScopeId(), roleFound.getScopeId());
            assertEquals(role.getName(), roleFound.getName());
            assertEquals(role.getCreatedBy(), roleFound.getCreatedBy());
            assertEquals(role.getCreatedOn(), roleFound.getCreatedOn());
            assertEquals(role.getModifiedBy(), roleFound.getModifiedBy());
            assertEquals(role.getModifiedOn(), roleFound.getModifiedOn());
            return null;
        });
    }

    @Test
    public void testQueryAndCount()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();

            // Create permission
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            Permission permission = permissionFactory.newPermission(testDomain, Actions.read, scope);

            Set<Permission> permissions = new HashSet<>();
            permissions.add(permission);

            // Create role
            RoleCreator roleCreator = new RoleCreatorImpl(scope);
            roleCreator.setName("test-" + new Date().getTime());
            roleCreator.setPermissions(permissions);

            RoleService roleService = locator.getService(RoleService.class);
            Role role = roleService.create(roleCreator);

            assertNotNull(role);
            assertNotNull(role.getId());

            //
            // Query
            RoleQuery query = new RoleQueryImpl(scope);
            query.setPredicate(new AttributePredicate<String>(RolePredicates.ROLE_NAME, role.getName()));
            RoleListResult rolesFound = roleService.query(query);
            long rolesCount = roleService.count(query);

            //
            // Assert
            assertNotNull(rolesFound);
            assertEquals(1, rolesCount);
            assertEquals(1, rolesFound.getSize());

            Role roleFound = rolesFound.getItem(0);
            assertNotNull(roleFound);
            assertEquals(role.getScopeId(), roleFound.getScopeId());
            assertEquals(role.getScopeId(), roleFound.getScopeId());
            assertEquals(role.getName(), roleFound.getName());
            assertEquals(role.getCreatedBy(), roleFound.getCreatedBy());
            assertEquals(role.getCreatedOn(), roleFound.getCreatedOn());
            assertEquals(role.getModifiedBy(), roleFound.getModifiedBy());
            assertEquals(role.getModifiedOn(), roleFound.getModifiedOn());

            RolePermissionService rolePermissionService = locator.getService(RolePermissionService.class);
            RolePermissionListResult rolePermissionsListResult = rolePermissionService.findByRoleId(role.getScopeId(), role.getId());
            List<RolePermission> rolePermissions = rolePermissionsListResult.getItems();
            assertNotNull(rolePermissions);
            assertEquals(permissions.size(), rolePermissions.size());

            return null;
        });
    }

    @Test
    public void testDelete()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();

            // Create permission
            PermissionFactory permissionFactory = locator.getFactory(PermissionFactory.class);
            Permission permission = permissionFactory.newPermission(testDomain, Actions.read, scope);

            Set<Permission> permissions = new HashSet<>();
            permissions.add(permission);

            // Create role
            RoleCreator roleCreator = new RoleCreatorImpl(scope);
            roleCreator.setName("test-" + new Date().getTime());
            roleCreator.setPermissions(permissions);

            RoleService roleService = locator.getService(RoleService.class);
            Role role = roleService.create(roleCreator);

            assertNotNull(role);
            assertNotNull(role.getId());
            assertEquals(roleCreator.getScopeId(), role.getScopeId());

            Role roleFound = roleService.find(scope, role.getId());
            assertNotNull(roleFound);

            RolePermissionService rolePermissionService = locator.getService(RolePermissionService.class);
            RolePermissionListResult rolePermissionsListResult = rolePermissionService.findByRoleId(role.getScopeId(), role.getId());
            List<RolePermission> rolePermissions = rolePermissionsListResult.getItems();
            assertNotNull(rolePermissions);
            assertEquals(permissions.size(), rolePermissions.size());

            //
            // Delete
            roleService.delete(scope, role.getId());

            //
            // Assert
            roleFound = roleService.find(scope, role.getId());
            assertNull(roleFound);

            rolePermissionsListResult = rolePermissionService.findByRoleId(role.getScopeId(), role.getId());
            rolePermissions = rolePermissionsListResult.getItems();
            assertNotNull(rolePermissions);
            assertEquals(0, rolePermissions.size());

            return null;
        });
    }
}
