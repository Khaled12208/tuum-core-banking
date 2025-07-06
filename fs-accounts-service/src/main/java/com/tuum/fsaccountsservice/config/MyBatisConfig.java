package com.tuum.fsaccountsservice.config;

import com.tuum.common.types.Currency;
import com.tuum.common.util.CurrencyTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
@EnableAutoConfiguration(exclude = {
    HibernateJpaAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class
})
@MapperScan("com.tuum.fsaccountsservice.mapper")
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        
        // Set mapper locations
        sessionFactory.setMapperLocations(
            new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*.xml")
        );
        
        // Set type aliases package
        sessionFactory.setTypeAliasesPackage("com.tuum.common.domain.entities");
        
        // MyBatis configuration
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setLogImpl(org.apache.ibatis.logging.stdout.StdOutImpl.class);
        
        // Register type handlers
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        typeHandlerRegistry.register(Currency.class, CurrencyTypeHandler.class);
        typeHandlerRegistry.register(com.tuum.common.types.TransactionDirection.class, com.tuum.common.util.DirectionTypeHandler.class);
        typeHandlerRegistry.register(com.tuum.common.types.TransactionStatus.class, com.tuum.common.util.StatusTypeHandler.class);
        
        sessionFactory.setConfiguration(configuration);
        
        return sessionFactory.getObject();
    }
} 