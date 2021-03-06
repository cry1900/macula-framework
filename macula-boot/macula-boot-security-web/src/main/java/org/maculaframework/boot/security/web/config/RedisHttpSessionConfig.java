/*
 * Copyright 2004-2020 the original author or authors.
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

package org.maculaframework.boot.security.web.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.session.RedissonSessionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * <p>
 * <b>RedisHttpSessionConfig</b> 基于Spring Session的HTTP SESSION存储
 * </p>
 *
 * @author Rain
 * @since 2019-06-27
 */

@Configuration
public class RedisHttpSessionConfig extends SpringHttpSessionConfiguration {

    private static final String REMEMBER_ME_SERVICES_CLASS = "org.springframework.security.web.authentication.RememberMeServices";

    @Value("${server.servlet.session.cookie.domain-name-pattern}")
    private String domainNamePattern;
    @Value("${spring.session.redis.maxInactiveIntervalInSeconds:1800}")
    private Integer maxInactiveIntervalInSeconds;
    @Value("${spring.session.redis.namespace:spring:session:}")
    private String keyPrefix;

    @Bean
    public RedissonSessionRepository sessionRepository(
        @Qualifier("sessionRedissonClient")RedissonClient redissonClient, ApplicationEventPublisher eventPublisher) {
        RedissonSessionRepository repository = new RedissonSessionRepository(redissonClient, eventPublisher, keyPrefix);
        repository.setDefaultMaxInactiveInterval(maxInactiveIntervalInSeconds);
        return repository;
    }

    @Bean
    public DefaultCookieSerializer cookieSerializer(ServerProperties serverProperties) {
        Session.Cookie cookie = serverProperties.getServlet().getSession().getCookie();
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(cookie::getName).to(cookieSerializer::setCookieName);
        map.from(cookie::getDomain).to(cookieSerializer::setDomainName);
        map.from(cookie::getPath).to(cookieSerializer::setCookiePath);
        map.from(cookie::getHttpOnly).to(cookieSerializer::setUseHttpOnlyCookie);
        map.from(cookie::getSecure).to(cookieSerializer::setUseSecureCookie);
        map.from(cookie::getMaxAge).to((maxAge) -> cookieSerializer.setCookieMaxAge((int) maxAge.getSeconds()));

        if (!StringUtils.isEmpty(domainNamePattern)) {
            cookieSerializer.setDomainNamePattern(domainNamePattern);
        }

        if (ClassUtils.isPresent(REMEMBER_ME_SERVICES_CLASS, getClass().getClassLoader())) {
            cookieSerializer.setRememberMeRequestAttribute(SpringSessionRememberMeServices.REMEMBER_ME_LOGIN_ATTR);
        }
        return cookieSerializer;
    }
}
