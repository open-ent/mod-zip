///*
// * Copyright © WebServices pour l'Éducation, 2014
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package fr.wseduc.zip.test.integration.java;
//
//import org.junit.Test;
//import io.vertx.core.AsyncResult;
//import io.vertx.core.Handler<AsyncResult>;
//import io.vertx.core.Handler;
//import io.vertx.core.eventbus.Message;
//import io.vertx.core.json.JsonArray;
//import io.vertx.core.json.JsonObject;
//import io.vertx.testtools.TestVerticle;
//
//import java.io.File;
//
//import static io.vertx.testtools.VertxAssert.*;
//
//
//public class ZipperTest extends TestVerticle {
//
//	private static final String DEST_ZIP = System.getProperty("java.io.tmpdir") +
//			File.separator + "dest.zip";
//	private static final String DEST_ZIP_FILE = System.getProperty("java.io.tmpdir") +
//			File.separator + "destFile.zip";
//	private static final String DEST_ZIP_MULTI = System.getProperty("java.io.tmpdir") +
//			File.separator + "destMulti.zip";
//	private static final String SRC_PATH = ZipperTest.class.getClassLoader()
//			.getResource("some-dir").getPath();
//
//	@Test
//	public void testZipSpecifyFile() {
//		JsonObject msg = new JsonObject().put("path", SRC_PATH +
//				File.separator + "textfile.txt")
//				.put("zipFile", DEST_ZIP_FILE);
//		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
//			@Override
//			public void handle(Message<JsonObject> reply) {
//				assertEquals("ok", reply.body().getString("status"));
//				String dest = reply.body().getString("destZip");
//				assertEquals(DEST_ZIP_FILE, dest);
//				assertZipped(dest);
//				testComplete();
//			}
//		});
//	}
//
//	@Test
//	public void testZipSpecifyDir() {
//		JsonObject msg = new JsonObject().put("path", SRC_PATH)
//				.put("zipFile", DEST_ZIP);
//		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
//			@Override
//			public void handle(Message<JsonObject> reply) {
//				assertEquals("ok", reply.body().getString("status"));
//				String dest = reply.body().getString("destZip");
//				assertEquals(DEST_ZIP, dest);
//				assertZipped(dest);
//				testComplete();
//			}
//		});
//	}
//
//	@Test
//	public void testZipTempDir() {
//		JsonObject msg = new JsonObject().put("path", SRC_PATH);
//		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
//			@Override
//			public void handle(Message<JsonObject> reply) {
//				assertEquals("ok", reply.body().getString("status"));
//				String dest = reply.body().getString("destZip");
//				assertNotNull(dest);
//				assertZipped(dest);
//				vertx.fileSystem().deleteSync(dest);
//				testComplete();
//			}
//		});
//	}
//
//	@Test
//	public void testZipMultipleFiles() {
//		JsonArray paths = new JsonArray()
//				.add(SRC_PATH + File.separator + "sub-dir")
//				.add(SRC_PATH + File.separator + "textfile.txt");
//		JsonObject msg = new JsonObject().put("path", paths)
//				.put("zipFile", DEST_ZIP_MULTI);
//		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
//			@Override
//			public void handle(Message<JsonObject> reply) {
//				assertEquals("ok", reply.body().getString("status"));
//				String dest = reply.body().getString("destZip");
//				assertEquals(DEST_ZIP_MULTI, dest);
//				assertZipped(dest);
//				testComplete();
//			}
//		});
//	}
//
//	private void assertZipped(String dest) {
//		assertTrue(vertx.fileSystem().existsSync(dest));
//	}
//
//	@Override
//	public void start() {
//		cleanup();
//		container.deployModule(System.getProperty("vertx.modulename"), new Handler<AsyncResult><String>() {
//			@Override
//			public void handle(AsyncResult<String> ar) {
//				if (ar.succeeded()) {
//					ZipperTest.super.start();
//				} else {
//					ar.cause().printStackTrace();
//				}
//			}
//		});
//	}
//
//	@Override
//	public void stop() {
//		cleanup();
//	}
//
//	private void cleanup() {
//		try {
//			vertx.fileSystem().deleteSync(DEST_ZIP);
//			vertx.fileSystem().deleteSync(DEST_ZIP_FILE);
//			vertx.fileSystem().deleteSync(DEST_ZIP_MULTI);
//		} catch (Exception ignore) {}
//	}
//
//}
