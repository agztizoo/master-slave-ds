/**
 * DynamicDataSourceHolder.java
 */
package com.github.jieshaocd.datasource;

/**
 * @author jieshao
 * @date Jun 10, 2015
 */
public class DataSourceStrategyHolder {

    public static final String READ_ONLY = "slave";

    public static final String READ_WRITE = "master";

    private static final ThreadLocal<String> datasourceIndicator =
            new ThreadLocal<String>();

    public static void clear() {
        datasourceIndicator.set(null);
    }

    public static String get() {
        return datasourceIndicator.get();
    }

    public static void set(String value) {
        datasourceIndicator.set(value);
    }

}
