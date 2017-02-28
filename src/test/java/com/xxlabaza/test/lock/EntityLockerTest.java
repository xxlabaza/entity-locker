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

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

import com.xxlabaza.test.lock.EntityLocker.EntityLock;
import java.util.concurrent.CountDownLatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Test;

/**
 *
 * @author Artem Labazin <xxlabaza@gmail.com>
 * @since 28.02.2017
 */
public class EntityLockerTest {

    private static final String RESULT_NAME;

    static {
        RESULT_NAME = "Artem";
    }

    @Test
    @SneakyThrows
    public void lock () {
        val user = new User(1, "");
        val threadsCount = RESULT_NAME.length();

        val countDownLatch = new CountDownLatch(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            new Thread(new NameChangerLock(user, countDownLatch)).start();
        }
        countDownLatch.await(5, SECONDS);

        assertEquals(RESULT_NAME, user.getName());
    }

    @Test
    @SneakyThrows
    public void withinLock () {
        val user = new User(1, "");
        val threadsCount = RESULT_NAME.length();

        val countDownLatch = new CountDownLatch(threadsCount);
        for (int i = 0; i < threadsCount; i++) {
            new Thread(new NameChangerWithinLock(user, countDownLatch)).start();
        }
        countDownLatch.await(5, SECONDS);

        assertEquals(RESULT_NAME, user.getName());
    }

    private static class NameChangerLock implements Runnable {

        protected final User user;

        protected final CountDownLatch latch;

        NameChangerLock (User user, CountDownLatch latch) {
            this.user = user;
            this.latch = latch;
        }

        @Override
        public void run () {
            EntityLock entityLock = EntityLocker.lock(user.getClass(), user.getId());
            try {
                String name = user.getName();
                String newName = name + RESULT_NAME.charAt(name.length());
                user.setName(newName);
            } finally {
                entityLock.unlock();
                latch.countDown();
            }
        }
    }

    private static class NameChangerWithinLock implements Runnable {

        private final User user;

        private final CountDownLatch latch;

        NameChangerWithinLock (User user, CountDownLatch latch) {
            this.user = user;
            this.latch = latch;
        }

        @Override
        public void run () {
            EntityLocker.withinLock(user.getClass(), user.getId(), () -> {
                String name = user.getName();
                String newName = name + RESULT_NAME.charAt(name.length());
                user.setName(newName);
            });
            latch.countDown();
        }
    }

    @Data
    @AllArgsConstructor
    private static class User {

        private final Integer id;

        private String name;
    }
}
