/*
 * Copyright 2004-2019 the original author or authors.
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
package org.maculaframework.boot.core.modelmapper;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.maculaframework.boot.core.modelmapper.config.ModelMapperConfiguration;
import org.maculaframework.boot.core.modelmapper.dtos.UserDto;
import org.maculaframework.boot.core.modelmapper.entities.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests the {@link TypeMapConfigurer} class.
 *
 * @author Idan Rozenfeld
 */
@RunWith(SpringRunner.class)
public class TypeMapNameTest {

    @Autowired
    private ModelMapper modelMapper;

    @Test
    public void shouldInstantiateMapper() {
        assertThat(modelMapper, is(notNullValue()));
    }

    @Test
    public void shouldMapUserEntity() {
        final User user = new User("John Doe", 23);

        final UserDto userDto1 = modelMapper.map(user, UserDto.class);
        assertThat(userDto1.getFirstName(), is(nullValue()));

        final UserDto userDto2 = modelMapper.map(user, UserDto.class, "userMappingV1");
        assertThat(userDto2.getFirstName(), equalTo("John Doe"));
        assertThat(userDto2.getAge(), equalTo(user.getAge()));
    }

    @Configuration
    @Import(ModelMapperConfiguration.class)
    public static class Application {

        @Bean
        public TypeMapConfigurer<User, UserDto> userMapping() {
            return new TypeMapConfigurer<User, UserDto>() {

                @Override
                public String getTypeMapName() {
                    return "userMappingV1";
                }

                @Override
                public void configure(TypeMap<User, UserDto> typeMap) {
                    typeMap.addMapping(User::getAge, UserDto::setAge);
                    typeMap.addMapping(User::getName, UserDto::setFirstName);
                }
            };
        }
    }
}