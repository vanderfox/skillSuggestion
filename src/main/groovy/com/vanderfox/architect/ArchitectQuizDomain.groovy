package com.vanderfox.architect

import grails.persistence.Entity

/**
 * Created by rvanderwerf on 6/15/16.
 */
@Entity
class ArchitectQuizDomain implements Serializable {
    int id
    String quizName
    static mapWith = "dynamodb"

}
