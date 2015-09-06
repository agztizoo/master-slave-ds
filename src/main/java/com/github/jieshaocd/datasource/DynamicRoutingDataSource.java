/**
 * DynamicRoutingDataSource.java
 */
package com.github.jieshaocd.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

/**
 * @author jieshao
 * @date Jun 10, 2015
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceStrategyHolder.get();
    }

    public void close() {
    }

}
