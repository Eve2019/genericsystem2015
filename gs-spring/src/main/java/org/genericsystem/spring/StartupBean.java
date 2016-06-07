//package org.genericsystem.spring;
//
//import java.lang.reflect.Type;
//import java.util.Set;
//
//import javax.enterprise.context.ApplicationScoped;
//import javax.enterprise.event.Observes;
//import javax.enterprise.inject.Any;
//import javax.enterprise.inject.spi.AfterDeploymentValidation;
//import javax.enterprise.inject.spi.Bean;
//import javax.enterprise.inject.spi.BeanManager;
//import javax.enterprise.inject.spi.Extension;
//import javax.enterprise.util.AnnotationLiteral;
//
//import org.genericsystem.api.core.annotations.SystemGeneric;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
////@ApplicationScoped
//public class StartupBean implements Extension {
//
//	private final Logger log = LoggerFactory.getLogger(StartupBean.class);
//
//	public void onStartup(@Observes AfterDeploymentValidation event, BeanManager beanManager) {
//		log.info("------------------start initialization-----------------------");
//		UserClassesProvider userClasses = getBean(UserClassesProvider.class, beanManager);
//		@SuppressWarnings("serial")
//		Set<Bean<?>> beans = beanManager.getBeans(Object.class, new AnnotationLiteral<Any>() {
//		});
//		for (Bean<?> bean : beans) {
//			Type clazz = bean.getBeanClass();
//			if (clazz instanceof Class) {
//				Class<?> classToProvide = (Class<?>) clazz;
//				if (classToProvide.getAnnotation(SystemGeneric.class) != null) {
//					log.info("Generic System: providing " + classToProvide);
//					userClasses.addUserClasse(classToProvide);
//				}
//			}
//		}
//		// Start Engine after deployment
//		getBean(Engine.class, beanManager);
//		// EventLauncher eventLauncher = getBean(EventLauncher.class, beanManager);
//		// eventLauncher.launchStartEvent();
//		log.info("-------------------end initialization------------------------");
//	}
//
//	@SuppressWarnings("unchecked")
//	public static <T extends Object> T getBean(Class<T> clazz, BeanManager beanManager) {
//		Bean<?> bean = beanManager.resolve(beanManager.getBeans(clazz));
//		return (T) beanManager.getReference(bean, clazz, beanManager.createCreationalContext(bean));
//	}
// }
