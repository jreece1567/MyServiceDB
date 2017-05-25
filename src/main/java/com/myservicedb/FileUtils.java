/**
 *
 */
package com.myservicedb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author jreece
 *
 */
public class FileUtils {

    protected static Logger LOGGER = Logger
            .getLogger(FileUtils.class.getName());

    /**
     * @param filename
     *            the name of the file to read
     * @return the contents of the file, as a {@code List<String>} of lines
     */
    public static List<String> readFileAsList(final String filename) {
        final File f = new File(filename);
        if (f.exists()) {
            return readFileAsList(f);
        }
        return null;
    }

    /**
     * @param file
     *            the File to read
     * @return the contents of the file, as a {@code List<String>} of lines
     */
    public static List<String> readFileAsList(final File file) {

        final List<String> contents = new ArrayList<String>();
        try {
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                contents.add(new String(line));
            }
            br.close();
        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }

        return contents;
    }

    /**
     * @param filename
     *            the name of the file to read
     * @return the contents of the file, as a String
     */
    public static String readFileAsString(final String filename) {
        final File f = new File(filename);
        if (f.exists()) {
            return readFileAsString(f);
        }
        return null;
    }

    /**
     * @param file
     *            the File to read
     * @return the contents of the file, as a String
     */
    public static String readFileAsString(final File file) {

        try {
            final StringBuilder sb = new StringBuilder();
            final BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            return sb.toString();

        } catch (final IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * @param uri
     *            the URL of the file to read
     * @return the contents of the file, as a String
     */
    public static String readUrlAsString(final String uri) {
        try {
            final URL url = new URL(uri);
            final HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.addRequestProperty("Accept", "application/json");
            InputStream is;
            if ((connection.getResponseCode() > 199)
                    && (connection.getResponseCode() < 300)) {
                // read response-body for HTTP-2xx responses
                is = connection.getInputStream();
            } else {
                // read response-body for HTTP-3xx/4xx/5xx responses
                is = connection.getErrorStream();
            }
            final BufferedReader br = new BufferedReader(new InputStreamReader(
                    is));
            final StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            is.close();
            if ((connection.getResponseCode() > 199)
                    && (connection.getResponseCode() < 300)) {
                // return the result
                return sb.toString();
            } else {
                // throw the exception, with the string read from the error
                // stream
                throw new IOException(sb.toString());
            }
        } catch (final IOException e) {
            LOGGER.severe("Response '" + e.getLocalizedMessage()
                    + "' when accessing " + uri);
            return null;
        }
    }

    /**
     * @param filename
     *            the name of the file to be written
     * @param lines
     *            a {@code List<String>} to be written to the file
     */
    public static void writeFileFromList(final String filename,
            final List<String> lines) {
        writeFileFromList(new File(filename), lines);
    }

    /**
     * @param file
     *            the File to be written
     * @param lines
     *            a {@code List<String>} to be written to the file
     */
    public static void writeFileFromList(final File file,
            final List<String> lines) {
        try {
            System.out.println("writing file " + file.getAbsolutePath());

            final FileWriter fw = new FileWriter(file);
            final Iterator<String> it = lines.iterator();
            while (it.hasNext()) {
                fw.write(it.next() + "\n");
            }
            fw.flush();
            fw.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param filename
     *            the name of the file to be written
     * @param string
     *            a String to be written to the file
     */
    public static void writeFileFromString(final String filename,
            final String string) {
        writeFileFromString(new File(filename), string);
    }

    /**
     * @param file
     *            the file to be written
     * @param string
     *            a String to be written to the file
     */
    public static void writeFileFromString(final File file, final String string) {
        try {
            System.out.println("writing file " + file.getAbsolutePath());

            final FileWriter fw = new FileWriter(file);
            fw.write(string + "\n");
            fw.flush();
            fw.close();
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
