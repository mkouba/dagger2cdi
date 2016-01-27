/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.dagger2cdi.simple;

import org.jboss.dagger2cdi.Alpha;
import org.jboss.dagger2cdi.Bravo;
import org.jboss.dagger2cdi.Dagger2CdiExtension;
import org.jboss.dagger2cdi.Juicy;
import org.jboss.dagger2cdi.LazyAdaptor;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;

/**
 *
 * @author Martin Kouba
 */
public class Dagger2CdiSimpleTest {

    @Test
    public void testCoffeeMaker() {
        // Run with -Dorg.jboss.dagger2cdi.emulate="org\.jboss\.dagger2cdi\.simple.*"
        try (WeldContainer container = new Weld().disableDiscovery().addExtension(new Dagger2CdiExtension()).packages(CoffeeMaker.class, LazyAdaptor.class)
                .beanClasses(Alpha.class, Bravo.class, Juicy.class).initialize()) {
            container.select(CoffeeMaker.class).get().brew();
            container.select(Bravo.class, Juicy.Literal.INSTANCE).get().ping();
        }
    }

}
