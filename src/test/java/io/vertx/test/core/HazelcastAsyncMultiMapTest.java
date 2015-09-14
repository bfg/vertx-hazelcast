/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.test.core;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;

import io.vertx.core.net.impl.ServerID;
import io.vertx.core.spi.cluster.ChoosableIterable;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class HazelcastAsyncMultiMapTest extends AsyncMultiMapTest {

  static {
    System.setProperty("hazelcast.wait.seconds.before.join", "0");
    System.setProperty("hazelcast.local.localAddress", "127.0.0.1");
  }

  @Override
  protected ClusterManager getClusterManager() {
    return new HazelcastClusterManager();
  }

  @Test
  public void shouldNotAddToMapCacheIfKeyDoesntAlreadyExist() throws Exception {
    String nonexistentKey = "non-existent-key." + UUID.randomUUID();

    map.get(nonexistentKey, ar -> {
      if (ar.succeeded()) {
        try {
          ChoosableIterable<ServerID> s = ar.result();
          Map<String, ChoosableIterable<ServerID>> cache = getCacheFromMap();

          // check result
          assertNotNull(s);
          assertTrue(s.isEmpty());

          // check cache
          assertNotNull(cache);
          assertFalse(
              "Map cache should not contain key " + nonexistentKey,
              cache.containsKey(nonexistentKey));

        } catch (Exception e) {
          fail(e.toString());
        } finally {
          testComplete();
        }
      } else {
        fail(ar.cause().toString());
        testComplete();
      }
    });

    await();
  }

  @SuppressWarnings("unchecked")
  private Map<String, ChoosableIterable<ServerID>> getCacheFromMap() throws Exception {
    Field field = map.getClass().getDeclaredField("cache");
    field.setAccessible(true);
    return (Map<String, ChoosableIterable<ServerID>>) field.get(map);
  }
}
