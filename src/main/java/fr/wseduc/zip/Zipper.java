/*
 * Copyright © WebServices pour l'Éducation, 2014
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

package fr.wseduc.zip;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.vertx.java.busmods.BusModBase;

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
		JsonObject conf = config;
		String address = conf.getString("address", "zipper");
		eb.consumer(address, this);
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
		if (vertx.fileSystem().existsBlocking(zipName)) {
			sendError(message, "Zip file already exists.");
			return;
		}
		for (Object o : paths) {
			if (!vertx.fileSystem().existsBlocking(o.toString())) {
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
				try
				{
					for (Object o : paths) {
						vertx.fileSystem().deleteRecursiveBlocking(o.toString(), true);
					}
				} catch(io.vertx.core.file.FileSystemException e)
				{
					logger.error(e.getMessage(), e);
					// Don't send an error if the delete fails
				}
			}
			sendOK(message, new JsonObject().put("destZip", zipName));
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

					if(filePath.endsWith(File.separator) == false)
					{
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
	}

	private Set<String> listFilesRecursive(File path) {
		File[] files = path.listFiles();
		Set<String> f = new HashSet<>();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					f.add(file.getAbsolutePath());
				} else {
					String dirName = file.getAbsolutePath();
					if(dirName.endsWith(File.separator) == false)
						dirName += File.separator;
					f.add(dirName);
					f.addAll(listFilesRecursive(file));
				}
			}
		}
		return f;
	}

}
