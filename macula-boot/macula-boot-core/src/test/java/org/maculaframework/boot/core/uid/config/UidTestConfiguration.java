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

package org.maculaframework.boot.core.uid.config;

import org.maculaframework.boot.core.uid.support.service.DefaultWorkerIdAssigner;
import org.maculaframework.boot.core.uid.worker.WorkerIdAssigner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * <p>
 * <b>UidTestConfiguration</b> 测试UID配置
 * </p>
 *
 * @author Rain
 * @since 2019-03-13
 */

@TestConfiguration
public class UidTestConfiguration {
    @Bean
    WorkerIdAssigner workerIdAssigner() {
        return new DefaultWorkerIdAssigner();
    }
}