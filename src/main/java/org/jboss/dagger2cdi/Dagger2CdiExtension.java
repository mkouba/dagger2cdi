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
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedConstructor;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedParameter;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanAttributes;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessBeanAttributes;
import javax.enterprise.inject.spi.WithAnnotations;
import javax.enterprise.util.AnnotationLiteral;

import dagger.Provides;

/**
 * TODO configure types to process (regex)
 * TODO take qualifiers into account
 *
 * @author Martin Kouba
 */
public class Dagger2CdiExtension implements Extension {

    @SuppressWarnings("serial")
    private final static AnnotationLiteral<Produces> PRODUCES_LITERAL = new AnnotationLiteral<Produces>() {
    };

    private final Set<Type> providerMethodsReturnTypes = new HashSet<>();

    <T> void observeProvidesMethods(@WithAnnotations(Provides.class) @Observes ProcessAnnotatedType<T> event) {
        // Add @Produces to each @Provides method
        event.setAnnotatedType(new WrappedType<T>(event.getAnnotatedType()));
    }

    <T> void observeBeanAttributes(@Observes ProcessBeanAttributes<T> event) {
        Annotated annotated = event.getAnnotated();
        if (!annotated.isAnnotationPresent(Provides.class)) {
            // If there is a bean with base type satisfied by a provider method, veto such a bean
            if (providerMethodsReturnTypes.contains(annotated.getBaseType())) {
                event.veto();
                return;
            }
            Set<Type> found = findProvidedTypes(event.getBeanAttributes().getTypes());
            if (!found.isEmpty()) {
                event.setBeanAttributes(new WrappedBeanAttributes<T>(event.getBeanAttributes(), found));
            }
        }
    }

    // TODO qualifiers etc.
    private Set<Type> findProvidedTypes(Set<Type> types) {
        Set<Type> found = new HashSet<>();
        for (Type type : types) {
            if (providerMethodsReturnTypes.contains(type)) {
                found.add(type);
            }
        }
        return found;
    }

    private class WrappedBeanAttributes<T> implements BeanAttributes<T> {

        private final BeanAttributes<T> delegate;

        private final Set<Type> types;

        /**
         *
         * @param delegate
         * @param provided
         */
        private WrappedBeanAttributes(BeanAttributes<T> delegate, Set<Type> provided) {
            this.delegate = delegate;
            this.types = new HashSet<>(delegate.getTypes());
            this.types.removeAll(provided);
        }

        @Override
        public Set<Type> getTypes() {
            return types;
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return delegate.getQualifiers();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return delegate.getScope();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return delegate.getStereotypes();
        }

        @Override
        public boolean isAlternative() {
            return delegate.isAlternative();
        }

    }

    private class WrappedType<X> implements AnnotatedType<X> {

        private final AnnotatedType<X> delegate;

        private final Set<AnnotatedMethod<? super X>> methods;

        /**
         *
         * @param delegate
         */
        WrappedType(AnnotatedType<X> delegate) {
            this.delegate = delegate;
            Set<AnnotatedMethod<? super X>> methods = new HashSet<>();
            for (AnnotatedMethod<? super X> method : delegate.getMethods()) {
                if (method.isAnnotationPresent(Provides.class)) {
                    methods.add(new WrappedMethod<>(method));
                    providerMethodsReturnTypes.add(method.getJavaMember().getGenericReturnType());
                } else {
                    methods.add(method);
                }
            }
            this.methods = methods;
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            return delegate.getAnnotation(annotationType);
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return delegate.getAnnotations();
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return delegate.isAnnotationPresent(annotationType);
        }

        @Override
        public Class<X> getJavaClass() {
            return delegate.getJavaClass();
        }

        @Override
        public Set<AnnotatedConstructor<X>> getConstructors() {
            return delegate.getConstructors();
        }

        @Override
        public Set<AnnotatedMethod<? super X>> getMethods() {
            return methods;
        }

        @Override
        public Set<AnnotatedField<? super X>> getFields() {
            return delegate.getFields();
        }

    }

    private class WrappedMethod<X> implements AnnotatedMethod<X> {

        private final AnnotatedMethod<X> delegate;

        private final Set<Annotation> annotations;

        private WrappedMethod(AnnotatedMethod<X> delegate) {
            this.delegate = delegate;
            this.annotations = new HashSet<>(delegate.getAnnotations());
            this.annotations.add(PRODUCES_LITERAL);
        }

        @Override
        public List<AnnotatedParameter<X>> getParameters() {
            return delegate.getParameters();
        }

        @Override
        public boolean isStatic() {
            return delegate.isStatic();
        }

        @Override
        public AnnotatedType<X> getDeclaringType() {
            return delegate.getDeclaringType();
        }

        @Override
        public Type getBaseType() {
            return delegate.getBaseType();
        }

        @Override
        public Set<Type> getTypeClosure() {
            return delegate.getTypeClosure();
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(annotationType)) {
                    return (T) annotation;
                }
            }
            return null;
        }

        @Override
        public Set<Annotation> getAnnotations() {
            return Collections.unmodifiableSet(annotations);
        }

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
            return getAnnotation(annotationType) != null;
        }

        @Override
        public Method getJavaMember() {
            return delegate.getJavaMember();
        }

    }

}
