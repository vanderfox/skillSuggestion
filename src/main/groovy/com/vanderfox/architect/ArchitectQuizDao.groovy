package com.vanderfox.architect;

import com.amazon.speech.speechlet.Session
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Contains the methods to interact with the persistence layer for ScoreKeeper in DynamoDB.
 */
public class ArchitectQuizDao {
    private final ArchitectQuizDynamoDbClient dynamoDbClient;
    private static final Logger log = LoggerFactory.getLogger(ArchitectQuizDao.class);

    public ArchitectQuizDao(ArchitectQuizDynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public ArchitectQuizDataItem getQuestion(int id) {
        ArchitectQuizDataItem item = new ArchitectQuizDataItem();
        item.setId(id);
        log.info ("item index is:  " + id)

        item = dynamoDbClient.loadItem(item);

        if (item == null) {
            return null;
        }

        item
    }
}
