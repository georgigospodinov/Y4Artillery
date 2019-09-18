package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Provides reading of ".props" files.
 *
 * @version 1.3
 */
public class Props {

    private static final HashMap<String, String> STRING_PROPERTIES = new HashMap<>();
    private static final HashMap<String, Long> LONG_PROPERTIES = new HashMap<>();
    private static final HashMap<String, Double> DOUBLE_PROPERTIES = new HashMap<>();
    private static final String DEFAULT_PROPS_FILE = "default.props";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String COMMENT_SYMBOL = "#";
    private static final int KEY_INDEX = 0;
    private static final int VALUE_INDEX = 1;

    /**
     * Gives the number of properties currently in storage.
     *
     * @return the current number of properties
     */
    public static int size() {
        return STRING_PROPERTIES.size() + LONG_PROPERTIES.size() + DOUBLE_PROPERTIES.size();
    }

    /**
     * Clears all the loaded properties.
     */
    public static void clear() {
        STRING_PROPERTIES.clear();
        LONG_PROPERTIES.clear();
        DOUBLE_PROPERTIES.clear();
    }

    /**
     * Get the {@link Long} value associated with the given property.
     *
     * @param property the property to look for
     * @return the long value associated with that property
     */
    public static long getLong(String property) {
        if (!LONG_PROPERTIES.containsKey(property))
            throw new NullPointerException("No long property \"" + property + "\"");

        return LONG_PROPERTIES.get(property);
    }

    /**
     * Get the {@link Double} value associated with the given property.
     *
     * @param property the property to look for
     * @return the double value associated with that property
     */
    public static double getDouble(String property) {
        if (!DOUBLE_PROPERTIES.containsKey(property))
            throw new NullPointerException("No double property \"" + property + "\"");

        return DOUBLE_PROPERTIES.get(property);
    }

    /**
     * Get the {@link String} value associated with the given property.
     *
     * @param property the property to look for
     * @return the string value associated with that property
     */
    public static String getString(String property) {
        if (!STRING_PROPERTIES.containsKey(property))
            throw new NullPointerException("No string property \"" + property + "\"");

        return STRING_PROPERTIES.get(property);
    }

    public static void load() throws FileNotFoundException {
        load(DEFAULT_PROPS_FILE);
    }

    /**
     * Load properties from the specified file.
     *
     * @param filename the name of the file containing properties.
     *                 This can be absolute or relative path.
     * @throws FileNotFoundException if there is no file with the given name
     */
    public static void load(String filename) throws FileNotFoundException {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filename));
        }
        catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not find props file \"" + filename + "\"");
        }

        reader.lines().forEach(line -> {
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith(COMMENT_SYMBOL)) return;

            String[] kv = line.split(KEY_VALUE_SEPARATOR);
            String key = kv[KEY_INDEX];
            String val = kv[VALUE_INDEX];
            try {
                long value = Long.parseLong(val);
                LONG_PROPERTIES.put(key, value);
            }
            catch (NumberFormatException nfe) {
                try {
                    double value = Double.parseDouble(val);
                    DOUBLE_PROPERTIES.put(key, value);
                }
                catch (NumberFormatException e) {
                    STRING_PROPERTIES.put(key, val);
                }
            }
        });
    }

}
