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
package org.jboss.dagger2cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.inject.Inject;

import dagger.Lazy;

/**
 * An adaptor for {@link Lazy} handle.
 *
 * @author Martin Kouba
 *
 * @param <T>
 */
public class LazyAdaptor<T> implements Lazy<T> {

    private final BeanManager beanManager;
    private final Bean<?> bean;
    private final Type requestedType;

    private volatile T instance;

    @Inject
    public LazyAdaptor(InjectionPoint injectionPoint, BeanManager beanManager) {
        this.beanManager = beanManager;
        ParameterizedType parameterizedType = (ParameterizedType) injectionPoint.getType();
        this.requestedType = parameterizedType.getActualTypeArguments()[0];
        // This will fail for unsatisfied or ambiguous dependency
        this.bean = beanManager.resolve(beanManager.getBeans(requestedType, injectionPoint.getQualifiers().toArray(new Annotation[] {})));
    }

    @SuppressWarnings("unchecked")
    public T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = (T) beanManager.getReference(bean, requestedType, beanManager.createCreationalContext(bean));
                }
            }
        }
        if (instance == null) {
            // Follow the Lazy.get() contract
            throw new NullPointerException();
        }
        return instance;
    }

}
