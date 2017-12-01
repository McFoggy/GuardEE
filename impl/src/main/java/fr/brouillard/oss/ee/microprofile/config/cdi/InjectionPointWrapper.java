/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/GuardEE] (matthieu@brouillard.fr)
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
 */
package fr.brouillard.oss.ee.microprofile.config.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.InjectionPoint;

public class InjectionPointWrapper  {
	private final Collection<Annotation> additionalAnnotations;

	private InjectionPointWrapper() {
		additionalAnnotations = new ArrayList<>();
	}
	
	public static InjectionPointWrapper create() {
		return new InjectionPointWrapper();
	}
	public InjectionPointWrapper addQualifiers(Annotation...annotations) {
		this.additionalAnnotations.addAll(Arrays.asList(annotations));
		return this;
	}

	public InjectionPoint wrap(InjectionPoint delegate) {
		InjectionPointDelegate wrapper = new InjectionPointDelegate(delegate);
		wrapper.addQualifiers(additionalAnnotations);
		return wrapper;
	}

	private class InjectionPointDelegate implements InjectionPoint {
		private InjectionPoint delegate;
		private final Set<Annotation> qualifiers;

		private InjectionPointDelegate(InjectionPoint delegate) {
			this.delegate = delegate;
			qualifiers = new HashSet<>(delegate.getQualifiers());
		}
		
		@Override
		public Type getType() {
			return delegate.getType();
		}

		public void addQualifiers(Collection<Annotation> qualifiersToAdd) {
			this.qualifiers.addAll(qualifiersToAdd);
		}
		
		@Override
		public Set<Annotation> getQualifiers() {
			return qualifiers;
		}

		@Override
		public Bean<?> getBean() {
			return delegate.getBean();
		}

		@Override
		public Member getMember() {
			return delegate.getMember();
		}

		@Override
		public Annotated getAnnotated() {
			return delegate.getAnnotated();
		}

		@Override
		public boolean isDelegate() {
			return false;
		}

		@Override
		public boolean isTransient() {
			return delegate.isTransient();
		}
	}
}
