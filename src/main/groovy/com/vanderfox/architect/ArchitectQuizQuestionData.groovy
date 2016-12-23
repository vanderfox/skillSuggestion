package com.vanderfox.architect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains player and score data to represent a score keeper game.
 */
public class ArchitectQuizQuestionData {
    private String question;
    private String answer;

    public ArchitectQuizQuestionData() {
        // public no-arg constructor required for DynamoDBMapper marshalling
    }

    public static ArchitectQuizQuestionData newInstance() {
        ArchitectQuizQuestionData newInstance = new ArchitectQuizQuestionData();
        newInstance.setPlayers(new ArrayList<String>());
        newInstance.setScores(new HashMap<String, Long>());
        newInstance;
    }

    public String getQuestion() {
        question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        "[ArchitectQuizQuestionData question: " + question + "] answer: " + answer + "]";
    }
}
