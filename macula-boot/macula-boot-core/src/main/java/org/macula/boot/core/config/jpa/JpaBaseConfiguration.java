package org.macula.boot.core.config.jpa;

import org.macula.boot.core.domain.support.AuditorAwareStub;
import org.macula.boot.core.domain.support.DbDateTimeProvider;
import org.macula.boot.core.hibernate.audit.AuditedEventListener;
import org.macula.boot.core.repository.jpa.templatequery.template.FreemarkerSqlTemplates;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateProperties;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateSettings;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.persistenceunit.PersistenceUnitManager;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.*;

/**
 * JPA配置的基类
 *
 * @author Rain
 * @see 2019-2-15
 */

@EnableTransactionManagement
@EnableJpaAuditing(auditorAwareRef = "auditorAwareStub", dateTimeProviderRef = "dbDateTimeProvider")
@EnableConfigurationProperties(HibernateProperties.class)
public abstract class JpaBaseConfiguration {

    private DataSource dataSource;

    @Autowired
    private JpaProperties jpaProperties;

    @Autowired
    private HibernateProperties hibernateProperties;

    @Autowired(required = false)
    ObjectProvider<PersistenceUnitManager> persistenceUnitManager;

    protected JpaBaseConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected Map<String, Object> getVendorProperties() {
        HibernateSettings settings = new HibernateSettings();

        List<HibernatePropertiesCustomizer> customizers = new ArrayList<>();
        customizers.add((Map<String, Object> hibernateProperties) -> {
            hibernateProperties.put("hibernate.ejb.event.post-update", AuditedEventListener.class.getName());
            hibernateProperties.put("hibernate.ejb.event.post-delete", AuditedEventListener.class.getName());
        });

        settings.hibernatePropertiesCustomizers(customizers);

        return hibernateProperties.determineHibernateProperties(jpaProperties.getProperties(), settings);
    }

    protected EntityManagerFactoryBuilder getEntityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(getJpaVendorAdapter(), jpaProperties.getProperties(), persistenceUnitManager.getIfAvailable());
    }

    protected JpaVendorAdapter getJpaVendorAdapter() {
        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        adapter.setShowSql(this.jpaProperties.isShowSql());
        adapter.setDatabase(this.jpaProperties.determineDatabase(this.dataSource));
        adapter.setDatabasePlatform(this.jpaProperties.getDatabasePlatform());
        adapter.setGenerateDdl(this.jpaProperties.isGenerateDdl());
        return adapter;
    }

    // JPA Audit配置
    @Bean
    @ConditionalOnMissingBean
    public AuditorAwareStub auditorAwareStub() {
        return new AuditorAwareStub();
    }

    @Bean
    @ConditionalOnMissingBean
    public DbDateTimeProvider dbDateTimeProvider() {
        return new DbDateTimeProvider();
    }

    // JPA TemplateQuery配置
    @Bean
    @ConditionalOnMissingBean
    public FreemarkerSqlTemplates freemarkerSqlTemplates() {
        return new FreemarkerSqlTemplates();
    }
}