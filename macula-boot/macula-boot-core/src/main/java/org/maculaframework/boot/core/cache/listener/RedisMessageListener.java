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

package org.maculaframework.boot.core.cache.listener;

import org.maculaframework.boot.core.cache.cache.Cache;
import org.maculaframework.boot.core.cache.cache.LayeringCache;
import org.maculaframework.boot.core.cache.manager.AbstractCacheManager;
import org.maculaframework.boot.core.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.Collection;

/**
 * redis消息的订阅者
 *
 * @author yuhao.wang
 */
public class RedisMessageListener extends MessageListenerAdapter {
    private static final Logger log = LoggerFactory.getLogger(RedisPublisher.class);

    /**
     * 缓存管理器
     */
    private AbstractCacheManager cacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        super.onMessage(message, pattern);
        // 解析订阅发布的信息，获取缓存的名称和缓存的key
        RedisPubSubMessage redisPubSubMessage = (RedisPubSubMessage) cacheManager.getRedisTemplate()
                .getValueSerializer().deserialize(message.getBody());
        log.debug("redis消息订阅者接收到频道【{}】发布的消息。消息内容：{}", new String(message.getChannel()), JSONUtils.objectToJson(redisPubSubMessage));

        // 根据缓存名称获取多级缓存，可能有多个
        Collection<Cache> caches = cacheManager.getCache(redisPubSubMessage.getCacheName());
        for (Cache cache : caches) {
            // 判断缓存是否是多级缓存
            if (cache != null && cache instanceof LayeringCache) {
                switch (redisPubSubMessage.getMessageType()) {
                    case EVICT:
                        // 获取一级缓存，并删除一级缓存数据
                        ((LayeringCache) cache).getFirstCache().evict(redisPubSubMessage.getKey());
                        log.info("删除一级缓存{}数据,key={}", redisPubSubMessage.getCacheName(), redisPubSubMessage.getKey());
                        break;

                    case CLEAR:
                        // 获取一级缓存，并删除一级缓存数据
                        ((LayeringCache) cache).getFirstCache().clear();
                        log.info("清除一级缓存{}数据", redisPubSubMessage.getCacheName());
                        break;

                    default:
                        log.error("接收到没有定义的订阅消息频道数据");
                        break;
                }

            }
        }
    }

    public void setCacheManager(AbstractCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }
}
