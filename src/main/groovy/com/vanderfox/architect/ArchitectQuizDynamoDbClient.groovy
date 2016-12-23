package com.vanderfox.architect;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Client for DynamoDB persistance layer for the Score Keeper skill.
 */
public class ArchitectQuizDynamoDbClient {
    private final AmazonDynamoDBClient dynamoDBClient;
    private static final Logger log = LoggerFactory.getLogger(ArchitectQuizDynamoDbClient.class);

    public ArchitectQuizDynamoDbClient(final AmazonDynamoDBClient dynamoDBClient) {
        this.dynamoDBClient = dynamoDBClient;
    }

    /**
     * Loads an item from DynamoDB by primary Hash Key. Callers of this method should pass in an
     * object which represents an item in the DynamoDB table item with the primary key populated.
     *
     * @param tableItem
     * @return
     */
    public ArchitectQuizDataItem loadItem(final ArchitectQuizDataItem tableItem) {
        tableItem.setId(4)
        int itemIndex = tableItem.getId()
        log.info ("item index is:  " + itemIndex)
        DynamoDBMapper mapper = createDynamoDBMapper();
        log.info("getting ready to load the item")
        ArchitectQuizDataItem item = mapper.load(tableItem);
        log.info("Returned item:  " + item)
        item;
    }

    /**
     * Creates a {@link DynamoDBMapper} using the default configurations.
     *
     * @return
     */
    private DynamoDBMapper createDynamoDBMapper() {
        new DynamoDBMapper(dynamoDBClient);
    }
}
