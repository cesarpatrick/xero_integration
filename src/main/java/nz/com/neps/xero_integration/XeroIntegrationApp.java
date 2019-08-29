package nz.com.neps.xero_integration;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

@EnableAutoConfiguration
@SpringBootApplication
public class XeroIntegrationApp 
{    
    public static void main(String[] args) {
		SpringApplication.run(XeroIntegrationApp.class,args);
	}
    
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:testdb");
        dataSource.setUsername("sa");
        dataSource.setPassword("Cesar33775644");
        return dataSource;
    }

	
	@Bean
	public JpaVendorAdapter jpaVendorApapter() {
	    return new HibernateJpaVendorAdapter();
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
	    LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
	    emf.setDataSource(dataSource());
	    emf.setPackagesToScan("nz.com.neps.xero_integration.model");
	    emf.setJpaVendorAdapter(jpaVendorApapter());
	    emf.setJpaProperties(jpaProperties());
	    return emf;
	}
	 
	private Properties jpaProperties() {
	    Properties properties = new Properties();
	    properties.put("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
	    properties.put("hibernate.show_sql", "false");
	    properties.put("hibernate.hbm2ddl.auto", "update");
	    return properties;
	}
	
	@Bean
	@Autowired
	public PlatformTransactionManager transactionManager(EntityManagerFactory entityManager){
	    JpaTransactionManager transactionManager = new JpaTransactionManager();
	    transactionManager.setEntityManagerFactory(entityManager);
	    return transactionManager;
	}
}
	

