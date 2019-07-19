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

package org.maculaframework.boot.web.security.access;

import org.maculaframework.boot.web.security.support.Role;
import org.springframework.security.access.SecurityConfig;

/**
 * <p>
 * <b>MaculaSecurityConfigAttribute</b> 角色配置
 * </p>
 *
 * @author Rain
 * @since 2019-07-04
 */
public class MaculaSecurityConfigAttribute extends SecurityConfig {
    private boolean opposite;

    public MaculaSecurityConfigAttribute(Role roleVo) {
        super(roleVo.getRoleCode());
        this.opposite = roleVo.isOpposite();
    }

    public boolean isOpposite() {
        return this.opposite;
    }
}
