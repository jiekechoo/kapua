package org.eclipse.kapua.service.authorization.shiro;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.kapua.KapuaException;
import org.eclipse.kapua.commons.model.id.KapuaEid;
import org.eclipse.kapua.commons.model.query.predicate.AttributePredicate;
import org.eclipse.kapua.commons.security.KapuaSecurityUtils;
import org.eclipse.kapua.locator.KapuaLocator;
import org.eclipse.kapua.service.authorization.domain.Domain;
import org.eclipse.kapua.service.authorization.domain.DomainCreator;
import org.eclipse.kapua.service.authorization.domain.DomainFactory;
import org.eclipse.kapua.service.authorization.domain.DomainListResult;
import org.eclipse.kapua.service.authorization.domain.DomainQuery;
import org.eclipse.kapua.service.authorization.domain.DomainService;
import org.eclipse.kapua.service.authorization.domain.shiro.DomainPredicates;
import org.eclipse.kapua.service.authorization.permission.Actions;
import org.eclipse.kapua.test.KapuaTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainServiceTest extends KapuaTest {

    public static String DEFAULT_FILTER = "ath*.sql";
    public static String DROP_FILTER = "ath*_drop.sql";

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
            DomainFactory domainFactory = locator.getFactory(DomainFactory.class);

            Set<Actions> domainActions = new HashSet<>();
            domainActions.add(Actions.read);
            domainActions.add(Actions.write);

            DomainCreator domainCreator = domainFactory.newCreator("test-" + System.currentTimeMillis(), DomainServiceTest.class.getName() + System.currentTimeMillis());
            domainCreator.setActions(domainActions);

            DomainService domainService = locator.getService(DomainService.class);
            Domain domain = domainService.create(domainCreator);

            assertNotNull(domain);
            assertNotNull(domain.getId());
            assertNotNull(domain.getCreatedOn());
            assertNotNull(domain.getCreatedBy());
            assertEquals(domainCreator.getName(), domain.getName());
            assertEquals(domainCreator.getServiceName(), domain.getServiceName());

            assertNotNull(domain.getActions());
            assertEquals(domainCreator.getActions().size(), domain.getActions().size());

            for (Actions a : domainCreator.getActions()) {
                assertTrue(domain.getActions().contains(a));
            }

            return null;
        });
    }

    @Test
    public void testFind()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();
            DomainFactory domainFactory = locator.getFactory(DomainFactory.class);

            Set<Actions> domainActions = new HashSet<>();
            domainActions.add(Actions.read);
            domainActions.add(Actions.write);

            DomainCreator domainCreator = domainFactory.newCreator("test-" + System.currentTimeMillis(), DomainServiceTest.class.getName() + System.currentTimeMillis());
            domainCreator.setActions(domainActions);

            DomainService domainService = locator.getService(DomainService.class);
            Domain domain = domainService.create(domainCreator);

            assertNotNull(domain);
            assertNotNull(domain.getId());

            Domain domainFound = domainService.find(null, domain.getId());

            assertNotNull(domainFound);
            assertEquals(domain.getId(), domainFound.getId());
            assertEquals(domain.getCreatedOn(), domainFound.getCreatedOn());
            assertEquals(domain.getCreatedBy(), domainFound.getCreatedBy());
            assertEquals(domain.getName(), domainFound.getName());
            assertEquals(domain.getServiceName(), domainFound.getServiceName());

            assertNotNull(domainFound.getActions());
            assertEquals(domain.getActions().size(), domainFound.getActions().size());

            for (Actions a : domain.getActions()) {
                assertTrue(domainFound.getActions().contains(a));
            }

            return null;
        });
    }

    @Test
    public void testQueryAndCount()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();
            DomainFactory domainFactory = locator.getFactory(DomainFactory.class);
            DomainService domainService = locator.getService(DomainService.class);

            long initialCount = domainService.count(domainFactory.newQuery());

            // Domain 1
            Set<Actions> domainActions = new HashSet<>();
            domainActions.add(Actions.read);
            domainActions.add(Actions.write);

            DomainCreator domainCreator = domainFactory.newCreator("test-" + System.currentTimeMillis(), DomainServiceTest.class.getName() + System.currentTimeMillis());
            domainCreator.setActions(domainActions);

            Domain domain1 = domainService.create(domainCreator);

            // Domain 2
            domainActions = new HashSet<>();
            domainActions.add(Actions.read);
            domainActions.add(Actions.write);

            domainCreator = domainFactory.newCreator("test-" + System.currentTimeMillis(), DomainServiceTest.class.getName() + System.currentTimeMillis());
            domainCreator.setActions(domainActions);

            Domain domain2 = domainService.create(domainCreator);

            //
            // Test query
            DomainQuery query = domainFactory.newQuery();
            DomainListResult result = domainService.query(query);
            long count = domainService.count(query);

            assertNotNull(result);
            assertEquals(initialCount + 2, count);
            assertEquals(count, result.getSize());

            //
            // Test name filtered query
            query = domainFactory.newQuery();

            query.setPredicate(new AttributePredicate<String>(DomainPredicates.NAME, domain1.getName()));
            result = domainService.query(query);
            count = domainService.count(query);

            assertNotNull(result);
            assertEquals(1, count);
            assertEquals(count, result.getSize());
            assertEquals(domain1, result.getItem(0));

            //
            // Test name filtered query
            query = domainFactory.newQuery();

            query.setPredicate(new AttributePredicate<String>(DomainPredicates.SERVICE_NAME, domain2.getServiceName()));
            result = domainService.query(query);
            count = domainService.count(query);

            assertNotNull(result);
            assertEquals(1, count);
            assertEquals(count, result.getSize());
            assertEquals(domain2, result.getItem(0));

            return null;
        });
    }

    @Test
    public void testDelete()
            throws Exception {

        KapuaSecurityUtils.doPriviledge(() -> {
            KapuaLocator locator = KapuaLocator.getInstance();
            DomainFactory domainFactory = locator.getFactory(DomainFactory.class);

            Set<Actions> domainActions = new HashSet<>();
            domainActions.add(Actions.read);
            domainActions.add(Actions.write);

            DomainCreator domainCreator = domainFactory.newCreator("test-" + System.currentTimeMillis(), DomainServiceTest.class.getName() + System.currentTimeMillis());
            domainCreator.setActions(domainActions);

            DomainService domainService = locator.getService(DomainService.class);
            Domain domain = domainService.create(domainCreator);
            assertNotNull(domain);

            Domain domainFoundBefore = domainService.find(null, domain.getId());
            assertNotNull(domainFoundBefore);

            domainService.delete(null, domain.getId());

            Domain domainFoundAfter = domainService.find(null, domain.getId());
            assertNull(domainFoundAfter);

            return null;
        });
    }

}
