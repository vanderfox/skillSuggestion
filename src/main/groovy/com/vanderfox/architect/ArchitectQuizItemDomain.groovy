package com.vanderfox.architect

import grails.persistence.Entity

/**
 * Created by rvanderwerf on 6/15/16.
 */
@Entity
class ArchitectQuizItemDomain implements Serializable{
    ArchitectQuizDomain quiz
    String question
    String domain
    int id
    static mapWith = "dynamodb"

}
