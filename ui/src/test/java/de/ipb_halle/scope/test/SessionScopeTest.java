/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.scope.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.scope.SessionScopeContext;
import de.ipb_halle.scope.SessionScopeResetEvent;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SessionScopeTest {
    private Bean bean1;

    @Inject
    private Bean bean2;

    @Inject
    private OtherBean otherBean;

    @Inject
    private BeanManager beanManager;

    @Inject
    private Event<SessionScopeResetEvent> event;

    @BeforeEach
    public void before() {
        // This resets the context.
        ((SessionScopeContext) beanManager.getContext(SessionScoped.class))
                .reset();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return ShrinkWrap.create(WebArchive.class, "SessionScopeTest.war")
                // These bean classes are required because they act as producer.
                .addClass(Bean.class)
                .addClass(OtherBean.class)
                // This class is required because it observes a CDI event.
                .addClass(SessionScopeContext.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsResource("javax.enterprise.inject.spi.Extension",
                        "META-INF/services/javax.enterprise.inject.spi.Extension")
                .addClass(ApplicationScopedBean.class);
    }

    @Test
    public void test001_getBeans() {
        // You can get a bean instance programmatically
        bean1 = CDI.current().select(Bean.class).get();
        assertEquals("Hello World!", bean1.getHello());

        // ... or by injection
        assertEquals("Hello World!", bean2.getHello());

        // ... and these methods are equivalent.
        assertTrue(bean1 == bean2);

        // Also the (sub-)injected beans should be the same.
        assertTrue(bean1.getBean() == bean2.getBean());
        assertTrue(bean1.getBean() == otherBean);

        // All these operations act on one and the same bean instance.
        bean1.getBean().countUp();
        bean2.getBean().countUp();
        otherBean.countUp();
        assertEquals(3, otherBean.getCount());
    }

    @Test
    public void test002_resetSessionScope() {
        // The bean state was reset by before().
        assertEquals(0, bean2.getBean().getCount());

        bean2.getBean().countUp();

        // Reset the scope via the SessionScopeContext instance.
        ((SessionScopeContext) beanManager.getContext(SessionScoped.class))
                .reset();

        // The previous state was reset.
        assertEquals(0, bean2.getBean().getCount());

        bean2.getBean().countUp();

        // Reset the scope via an event.
        event.fire(new SessionScopeResetEvent());

        // The previous state was reset.
        assertEquals(0, bean2.getBean().getCount());
    }

    @Test
    public void test003_injectOtherScopes() {
        /*
         * It is possible to inject ApplicationScoped beans into SessionScoped
         * beans.
         */
        assertEquals("Hi!", bean2.getApplicationScopedBean().getMessage());

        /*
         * It is possible to inject SessionScoped beans into ApplicationScoped 
         * beans. Beware, this is not possible in JSF!!!
         */
        bean2.getApplicationScopedBean().getBean().countUp();
        assertEquals(1, otherBean.getCount());
    }
}