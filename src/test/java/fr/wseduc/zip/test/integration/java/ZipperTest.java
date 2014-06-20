package fr.wseduc.zip.test.integration.java;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.io.File;

import static org.vertx.testtools.VertxAssert.*;


public class ZipperTest extends TestVerticle {

	private static final String DEST_ZIP = System.getProperty("java.io.tmpdir") +
			File.separator + "dest.zip";
	private static final String DEST_ZIP_FILE = System.getProperty("java.io.tmpdir") +
			File.separator + "destFile.zip";
	private static final String SRC_PATH = ZipperTest.class.getClassLoader()
			.getResource("some-dir").getPath();

	@Test
	public void testZipSpecifyFile() {
		JsonObject msg = new JsonObject().putString("path", SRC_PATH +
				File.separator + "textfile.txt")
				.putString("zipFile", DEST_ZIP_FILE);
		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> reply) {
				assertEquals("ok", reply.body().getString("status"));
				String dest = reply.body().getString("destZip");
				assertEquals(DEST_ZIP_FILE, dest);
				assertZipped(dest);
				testComplete();
			}
		});
	}

	@Test
	public void testZipSpecifyDir() {
		JsonObject msg = new JsonObject().putString("path", SRC_PATH)
				.putString("zipFile", DEST_ZIP);
		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> reply) {
				assertEquals("ok", reply.body().getString("status"));
				String dest = reply.body().getString("destZip");
				assertEquals(DEST_ZIP, dest);
				assertZipped(dest);
				testComplete();
			}
		});
	}

	@Test
	public void testZipTempDir() {
		JsonObject msg = new JsonObject().putString("path", SRC_PATH);
		vertx.eventBus().send("zipper", msg, new Handler<Message<JsonObject>>() {
			@Override
			public void handle(Message<JsonObject> reply) {
				assertEquals("ok", reply.body().getString("status"));
				String dest = reply.body().getString("destZip");
				assertNotNull(dest);
				assertZipped(dest);
				vertx.fileSystem().deleteSync(dest);
				testComplete();
			}
		});
	}

	private void assertZipped(String dest) {
		assertTrue(vertx.fileSystem().existsSync(dest));
	}

	@Override
	public void start() {
		cleanup();
		container.deployModule(System.getProperty("vertx.modulename"), new AsyncResultHandler<String>() {
			@Override
			public void handle(AsyncResult<String> ar) {
				if (ar.succeeded()) {
					ZipperTest.super.start();
				} else {
					ar.cause().printStackTrace();
				}
			}
		});
	}

	@Override
	public void stop() {
		cleanup();
	}

	private void cleanup() {
		try {
			vertx.fileSystem().deleteSync(DEST_ZIP);
			vertx.fileSystem().deleteSync(DEST_ZIP_FILE);
		} catch (Exception ignore) {}
	}

}
