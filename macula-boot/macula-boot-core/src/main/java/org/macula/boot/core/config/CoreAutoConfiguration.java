package org.macula.boot.core.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.macula.boot.core.config.core.CoreConfigProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * <p>
 * <b>CoreAutoConfiguration</b> Core模块的自动配置入口
 * </p>
 *
 * @author Rain
 * @since 2019-01-22
 */

@Configuration
@EnableConfigurationProperties({CoreConfigProperties.class})
@EnableRedisRepositories
@AutoConfigureBefore({DruidDataSourceAutoConfigure.class, RedisAutoConfiguration.class, JpaRepositoriesAutoConfiguration.class})
@Import({RedisConfiguration.class, DataSourceConfiguration.class})
public class CoreAutoConfiguration {

}