package org.genericsystem.spring;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.genericsystem.kernel.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

//@RequestScoped
@Scope(value = "request")
// @Configuration
public class CacheRequestProvider {

	@Autowired
	private transient CacheSessionProvider cacheSessionProvider;

	@PostConstruct
	public void init() {
		cacheSessionProvider.getCurrentCache().start();
	}

	@Bean
	public Cache getCurrentCache() {
		return cacheSessionProvider.getCurrentCache();
	}

	@PreDestroy
	public void preDestroy() {
		cacheSessionProvider.getCurrentCache().stop();
	}

}
