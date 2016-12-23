package com.vanderfox.architect;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshaller;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMarshalling;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory;

/**
 * Model representing an item of the ScoreKeeperUserData table in DynamoDB for the ScoreKeeper
 * skill.
 */
@DynamoDBTable(tableName = "ArchitectQuiz")
public class ArchitectQuizDataItem {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(ArchitectQuizDataItem.class);

    private String id;

    private ArchitectQuizQuestionData questionData;

    @DynamoDBHashKey(attributeName = "id")
    public int getId() {
        log.info ("get item index is:  " + id)
        id;
    }

    public void setId(int id) {
        this.id = id;
        log.info ("set item index is:  " + id)
    }

    @DynamoDBAttribute(attributeName = "question")
    @DynamoDBMarshalling(marshallerClass = ArchitectQuizGameDataMarshaller.class)
    public ArchitectQuizQuestionData getQuestion() {
        return questionData;
    }

    public static class ArchitectQuizGameDataMarshaller implements
            DynamoDBMarshaller<ArchitectQuizQuestionData> {

        @Override
        public String marshall(ArchitectQuizQuestionData questionData) {
            try {
                return OBJECT_MAPPER.writeValueAsString(questionData);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to marshall game data", e);
            }
        }

        @Override
        public ArchitectQuizQuestionData unmarshall(Class<ArchitectQuizQuestionData> clazz, String value) {
            try {
                return OBJECT_MAPPER.readValue(value, new TypeReference<ArchitectQuizQuestionData>() {
                });
            } catch (Exception e) {
                throw new IllegalStateException("Unable to unmarshall game data value", e);
            }
        }
    }
}
