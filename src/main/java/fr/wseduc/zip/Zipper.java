package fr.wseduc.zip;

import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Zipper extends BusModBase implements Handler<Message<JsonObject>> {

	private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
	private static final int BUFFER_SIZE = 4096;

	@Override
	public void start() {
		super.start();
		JsonObject conf = container.config();
		String address = conf.getString("address", "zipper");
		eb.registerHandler(address, this);
	}

	@Override
	public void handle(Message<JsonObject> message) {
		final Object path = message.body().getValue("path");
		if (path == null) {
			sendError(message, "Source path is null.");
			return;
		}
		JsonArray paths;
		if (path instanceof JsonArray) {
			paths = (JsonArray) path;
		} else if (path instanceof String) {
			paths = new JsonArray().add(path);
		} else {
			sendError(message, "Invalid path type.");
			return;
		}
		final String zipName = message.body().getString("zipFile", generateTmpFileName());
		final boolean deletePath = message.body().getBoolean("deletePath", false);
		final int level = message.body().getInteger("level", Deflater.BEST_SPEED);
		if (vertx.fileSystem().existsSync(zipName)) {
			sendError(message, "Zip file already exists.");
			return;
		}
		for (Object o : paths) {
			if (!vertx.fileSystem().existsSync(o.toString())) {
				sendError(message, "Source path doesn't exists : " + o);
				return;
			}
		}
		if (level < 0 || level > 9) {
			sendError(message, "Level value must be 0-9");
			return;
		}
		try {
			zipData(paths, zipName, level);
			if (deletePath) {
				for (Object o : paths) {
					vertx.fileSystem().deleteSync(o.toString(), true);
				}
			}
			sendOK(message, new JsonObject().putString("destZip", zipName));
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			sendError(message, e.getMessage());
		}
	}

	private String generateTmpFileName() {
		return TEMP_DIR + File.separator + UUID.randomUUID().toString() + ".zip";
	}

	private void zipData(JsonArray paths, String zipFile, int level) throws IOException {
		try (FileOutputStream fos  = new FileOutputStream(zipFile);
			 ZipOutputStream zos = new ZipOutputStream(fos)) {
			zos.setLevel(level);
			for (Object path : paths) {
				Set<String> files;
				File directory = new File(path.toString());
				if (directory.isDirectory()) {
					files = listFilesRecursive(directory);
				} else {
					files = new HashSet<>();
					files.add(directory.getAbsolutePath());
				}
				for (String filePath : files) {
					String name = filePath.substring(directory.getParentFile()
							.getAbsolutePath().length() + 1,
							filePath.length());
					ZipEntry zipEntry = new ZipEntry(name);
					zos.putNextEntry(zipEntry);
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(filePath);
						byte[] buffer = new byte[BUFFER_SIZE];
						int length;
						while ((length = fis.read(buffer)) > 0) {
							zos.write(buffer, 0, length);
						}
					} finally {
						zos.closeEntry();
						if (fis != null) {
							fis.close();
						}
					}
				}
			}
		}
	}

	private Set<String> listFilesRecursive(File path) {
		File[] files = path.listFiles();
		Set<String> f = new HashSet<>();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					f.add(file.getAbsolutePath());
				} else {
					f.addAll(listFilesRecursive(file));
				}
			}
		}
		return f;
	}

}
