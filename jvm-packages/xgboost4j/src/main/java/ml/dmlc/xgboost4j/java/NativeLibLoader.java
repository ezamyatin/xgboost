/*
 Copyright (c) 2014, 2021 by Contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package ml.dmlc.xgboost4j.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * class to load native library
 *
 * @author hzx
 */
class NativeLibLoader {
  private static final Log logger = LogFactory.getLog(NativeLibLoader.class);

  private static boolean initialized = false;
  private static final String nativeResourcePath = "/lib";
  private static final String[] libNames = new String[]{"xgboost4j"};

  static synchronized void initXGBoost() throws IOException {
    if (!initialized) {
      String platform = computePlatformArchitecture();
      for (String libName : libNames) {
        try {
          String libraryPathInJar = nativeResourcePath + "/" +
              platform + "/" + System.mapLibraryName(libName);
          loadLibraryFromJar(libraryPathInJar);
        } catch (IOException ioe) {
          System.err.println("xgboost4j logger " + "failed to load " + libName + " library from jar for platform " + platform);
          throw ioe;
        }
      }
      initialized = true;
    }
  }

  /**
   * Computes a String representing the path to look for.
   * Assumes the libraries are stored in the jar in os/architecture folders.
   * <p>
   * Throws IllegalStateException if the architecture or OS is unsupported.
   * Supported OS: macOS, Windows, Linux, Solaris.
   * Supported Architectures: x86_64, aarch64, sparc.
   * @return The platform & architecture path.
   */
  private static String computePlatformArchitecture() {
    String detectedOS;
    String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
    if (os.contains("mac") || os.contains("darwin")) {
      detectedOS = "macos";
    } else if (os.contains("win")) {
      detectedOS = "windows";
    } else if (os.contains("nux")) {
      detectedOS = "linux";
    } else if (os.contains("sunos")) {
      detectedOS = "solaris";
    } else {
      throw new IllegalStateException("Unsupported os:" + os);
    }
    String detectedArch;
    String arch = System.getProperty("os.arch", "generic").toLowerCase(Locale.ENGLISH);
    if (arch.startsWith("amd64") || arch.startsWith("x86_64")) {
      detectedArch = "x86_64";
    } else if (arch.startsWith("aarch64") || arch.startsWith("arm64")) {
      detectedArch = "aarch64";
    } else if (arch.startsWith("sparc")) {
      detectedArch = "sparc";
    } else {
      throw new IllegalStateException("Unsupported architecture:" + arch);
    }

    return detectedOS + "/" + detectedArch;
  }

  /**
   * Loads library from current JAR archive
   * <p/>
   * The file from JAR is copied into system temporary directory and then loaded.
   * The temporary file is deleted after exiting.
   * Method uses String as filename because the pathname is "abstract", not system-dependent.
   * <p/>
   * The restrictions of {@link File#createTempFile(java.lang.String, java.lang.String)} apply to
   * {@code path}.
   *
   * @param path The filename inside JAR as absolute path (beginning with '/'),
   *             e.g. /package/File.ext
   * @throws IOException              If temporary file creation or read/write operation fails
   * @throws IllegalArgumentException If source file (param path) does not exist
   * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than
   * three characters
   */
  private static void loadLibraryFromJar(String path) throws IOException, IllegalArgumentException {
    String temp = createTempFileFromResource(path);
    System.load(temp);
  }

  /**
   * Create a temp file that copies the resource from current JAR archive
   * <p/>
   * The file from JAR is copied into system temp file.
   * The temporary file is deleted after exiting.
   * Method uses String as filename because the pathname is "abstract", not system-dependent.
   * <p/>
   * The restrictions of {@link File#createTempFile(java.lang.String, java.lang.String)} apply to
   * {@code path}.
   * @param path Path to the resources in the jar
   * @return The created temp file.
   * @throws IOException If it failed to read the file.
   * @throws IllegalArgumentException If the filename is invalid.
   */
  static String createTempFileFromResource(String path) throws
          IOException, IllegalArgumentException {
    // Obtain filename from path
    if (!path.startsWith("/")) {
      throw new IllegalArgumentException("The path has to be absolute (start with '/').");
    }

    String[] parts = path.split("/");
    String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

    // Split filename to prefix and suffix (extension)
    String prefix = "";
    String suffix = null;
    if (filename != null) {
      parts = filename.split("\\.", 2);
      prefix = parts[0];
      suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null; // Thanks, davs! :-)
    }

    // Check if the filename is okay
    if (filename == null || prefix.length() < 3) {
      throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
    }
    // Prepare temporary file
    File temp = File.createTempFile(prefix, suffix);
    temp.deleteOnExit();

    if (!temp.exists()) {
      throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
    }

    // Prepare buffer for data copying
    byte[] buffer = new byte[1024];
    int readBytes;

    // Open and check input stream
    try (InputStream is = NativeLibLoader.class.getResourceAsStream(path);
         OutputStream os = new FileOutputStream(temp)) {
      if (is == null) {
        throw new FileNotFoundException("File " + path + " was not found inside JAR.");
      }

      // Open output stream and copy data between source file in JAR and the temporary file
      while ((readBytes = is.read(buffer)) != -1) {
        os.write(buffer, 0, readBytes);
      }
    }

    return temp.getAbsolutePath();
  }

}
