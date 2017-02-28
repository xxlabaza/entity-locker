/*
 * Copyright 2017 Artem Labazin <xxlabaza@gmail.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xxlabaza.test.lock;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import lombok.EqualsAndHashCode;

/**
 * 
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 28.02.2017
 */
public final class EntityLocker {

    private static final Set<EntityLock> LOCKS;

    static {
        LOCKS = Collections.synchronizedSet(new HashSet<>());
    }

    public static synchronized EntityLock lock (Class<?> entityType, Object id) {
        EntityLock entityLock = new EntityLock(entityType, id);
        LOCKS.add(entityLock);
        entityLock.lock();
        return entityLock;
    }

    @EqualsAndHashCode(exclude = "lock")
    public static class EntityLock {

        private final Class<?> entityType;
        private final Object id;
        private final Lock lock;

        private EntityLock (Class<?> entityType, Object id) {
            this.entityType = entityType;
            this.id = id;
            lock = new ReentrantLock();
        }

        public void unlock () {
            lock.unlock();
            LOCKS.remove(this);
        }

        void lock () {
            lock.lock();
        }
    }

    private EntityLocker () {
    }
}
