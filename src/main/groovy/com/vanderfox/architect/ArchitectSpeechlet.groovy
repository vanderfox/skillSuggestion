package com.vanderfox.architect

import com.amazon.speech.slu.Intent
import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.Session
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.Speechlet
import com.amazon.speech.speechlet.SpeechletException
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SimpleCard
import com.amazon.speech.ui.SsmlOutputSpeech
import com.vanderfox.architect.question.Question
import com.vanderfox.architect.user.User
import groovy.transform.CompileStatic
import org.slf4j.Logger;
import org.slf4j.LoggerFactory
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Table
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.model.ScanRequest
import com.amazonaws.services.dynamodbv2.model.ScanResult
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

/**
 * This app shows how to connect to architect with Spring Social, Groovy, and Alexa.
 * @author Lee Fox and Ryan Vanderwerf
 */
@CompileStatic
public class ArchitectSpeechlet implements Speechlet {
    private static final Logger log = LoggerFactory.getLogger(ArchitectSpeechlet.class);

    @Override
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId())
        session.setAttribute("playerList", new ArrayList<User>())
        LinkedHashMap<String, Question> askedQuestions = new LinkedHashMap()
        session.setAttribute("askedQuestions", askedQuestions)
        session.setAttribute("questionCounter", getNumberOfQuestions())
        session.setAttribute("score", 0)
        initializeComponents(session)

        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        getWelcomeResponse(session);
    }

    @Override
    public SpeechletResponse onIntent(final IntentRequest request, final Session session)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;
        log.debug("Intent = " + intentName)
        switch (intentName) {
            case "AnswerIntent":
                getAnswer(intent.getSlot("Answer"), session)
                break
            case "DontKnowIntent":
                dontKnow(session)
                break
            case "AMAZON.HelpIntent":
                getHelpResponse(session)
                break
            case "AMAZON.CancelIntent":
                sayGoodbye()
                break
            case "AMAZON.RepeatIntent":
                repeatQuestion(session)
                break
            case "AMAZON.StopIntent":
                sayGoodbye()
                break
            default:
                didNotUnderstand()
                break
        }
    }

    @Override
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse sayGoodbye() {
        String speechText = "OK.  I'm going to stop the game now.";
        tellResponse(speechText, speechText)
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getWelcomeResponse(Session session) {
        String speechText = "Welcome to Architect Quiz.  I will ask you 10 questions to test your architect knowledge and help you prepare for the Solutions Architect Associate level test.  Say the number of the answer that you think is correct.  Say repeat question anytime you need to hear the question again.  You can end the game at any time by saying cancel or stop.  Of course, you can always say help at any time.  OK, let's get started.\n\n";
        speechText = getQuestion(session, speechText)
        askResponse(speechText, speechText)

    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private String getQuestion(final Session session, String speechText) {
        Question question = getRandomUnaskedQuestion(session)
        session.setAttribute("lastQuestionAsked", question)

        speechText += question.getQuestion() + "\n"
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + "\n\n\n\n" + option + "\n\n\n"
        }
        speechText
    }

    private Question getRandomUnaskedQuestion(Session session) {
        LinkedHashMap<String, Question> askedQuestions = (LinkedHashMap) session.getAttribute("askedQuestions")
        Question question = getRandomQuestion(session)
        while(askedQuestions.get(question.getQuestion()) != null) {
            question = getRandomQuestion(session)
        }
        askedQuestions.put(question.getQuestion(), question)
        session.setAttribute("askedQuestions", askedQuestions)
        question
    }

    private Question getRandomQuestion(Session session) {
        int tableRowCount = Integer.parseInt((String) session.getAttribute("tableRowCount"))
        int questionIndex = (new Random().nextInt() % tableRowCount).abs()
        log.info("The question index is:  " + questionIndex)
        Question question = getQuestion(questionIndex)
        question
    }

    private Question getQuestion(int questionIndex) {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient());
        Table table = dynamoDB.getTable("ArchitectQuiz");
        Item item = table.getItem("Id", questionIndex);
        def questionText = item.getString("Question")
        def questionAnswer = item.getInt("answer")
        def options = new String[4]
        options[0] = item.getString("option1")
        options[1] = item.getString("option2")
        options[2] = item.getString("option3")
        options[3] = item.getString("option4")
        Question question = new Question()
        question.setQuestion(questionText)
        question.setOptions(options)
        question.setAnswer(questionAnswer - 1)
        question.setIndex(questionIndex)
        log.info("question retrieved:  " + question.getIndex())
        log.info("question retrieved:  " + question.getQuestion())
        log.info("question retrieved:  " + question.getAnswer())
        log.info("question retrieved:  " + question.getOptions().length)
        question
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse askForPlayerNameAgain(Slot query, final Session session) {
        def speechText = ""
        speechText = "Sorry about that.  Please tell me the next players name again."
        askResponse(speechText, speechText)
    }

    private SpeechletResponse askResponse(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Architect Quiz");
        card.setContent(cardText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        SpeechletResponse.newAskResponse(speech, reprompt, card);
    }

    private SpeechletResponse tellResponse(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Architect Quiz");
        card.setContent(cardText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(speech);

        SpeechletResponse.newTellResponse(speech, card);
    }

    private SpeechletResponse askResponseFancy(String cardText, String speechText, String fileUrl) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Architect Quiz");
        card.setContent(cardText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);
        log.info("making welcome audio")
        SsmlOutputSpeech fancySpeech = new SsmlOutputSpeech()
        fancySpeech.ssml = "<speak><audio src=\"${fileUrl}\"/> ${speechText}</speak>"
        log.info("finished welcome audio")
        // Create reprompt
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(fancySpeech);

        SpeechletResponse.newAskResponse(fancySpeech, reprompt, card);
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse repeatQuestion(final Session session) {
        Question question = (Question) session.getAttribute("lastQuestionAsked")
        String speechText = ""
        speechText += "\n"
        speechText += question.getQuestion() + "\n"
        String[] options = question.getOptions()
        int index = 1
        for(String option: options) {
            speechText += (index++) + "\n\n\n\n" + option + "\n\n\n"
        }
        askResponse(speechText, speechText)

    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getAnswer(Slot query, final Session session) {

        def speechText

        int guessedAnswer
        try {
            guessedAnswer = Integer.parseInt(query.getValue()) - 1
        } catch (Exception e) {
            speechText = "That doesn't sound like it was an answer.  Go ahead and try again."
            return askResponse(speechText, speechText)
        }
        log.info("Guessed answer is:  " + query.getValue())

        Question question = (Question) session.getAttribute("lastQuestionAsked")
        if(question == null) {
            speechText = "I'm sorry.  Something went wrong.  Please repeat that."
            return askResponse(speechText, speechText)
        }
        def answer = question.getAnswer()
        log.info("correct answer is:  " + answer)

        int questionCounter = decrementQuestionCounter(session)

        if(guessedAnswer == answer) {
            incrementScore(session)
            speechText = "You got it right.\n\n"
        } else {
            speechText = "You got it wrong.\n\n"
        }

        log.info("questionCounter:  " + questionCounter)

        if(questionCounter > 0) {
            session.setAttribute("state", "askQuestion")
            speechText = getQuestion(session, speechText)
            return askResponse(speechText, speechText)
        } else {
            int score = (int) session.getAttribute("score")
            speechText += "You got " + score + " questions correct."
            return tellResponse(speechText, speechText)
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the hello intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse dontKnow(final Session session) {

        def speechText

        speechText = "Sorry about that.  Better luck next time.  OK, next question.\n\n"

        int questionCounter = decrementQuestionCounter(session)

        log.info("questionCounter:  " + questionCounter)

        if(questionCounter > 0) {
            session.setAttribute("state", "askQuestion")
            speechText = getQuestion(session, speechText)
            return askResponse(speechText, speechText)
        } else {
            int score = (int) session.getAttribute("score")
            speechText += "You got " + score + " questions correct."
            return tellResponse(speechText, speechText)
        }
    }

    /**
     * Creates a {@code SpeechletResponse} for the help intent.
     *
     * @return SpeechletResponse spoken and visual response for the given intent
     */
    private SpeechletResponse getHelpResponse(Session session) {
        String speechText = ""
        speechText = "You can say stop or cancel to end the game at any time.  If you need a question repeated, say repeat question.";
        askResponse(speechText, speechText)
    }

    private SpeechletResponse didNotUnderstand() {
        String speechText = "I'm sorry.  I didn't understand what you said.";
        askResponse(speechText, speechText)
    }

    private int decrementQuestionCounter(Session session) {
        int questionCounter = (int) session.getAttribute("questionCounter")
        questionCounter--
        session.setAttribute("questionCounter", questionCounter)
        questionCounter
    }

    private int incrementScore(Session session) {
        int score = (int) session.getAttribute("score")
        score++
        session.setAttribute("score", score)
        score
    }

    private int getNumberOfQuestions() {
        InputStream stream = com.vanderfox.architect.ArchitectSpeechlet.class.getClassLoader()getResourceAsStream("springSocial.properties")
        final Properties properties = new Properties();
        properties.load(stream);

        def property = properties.getProperty("numberOfQuestions")
        if (!property) {
            return 2
        }
        log.info("setting number of questions from config: ${property}")
        property.toInteger()
    }

    /**
     * Initializes the instance components if needed.
     */
    private void initializeComponents(Session session) {
        AmazonDynamoDBClient amazonDynamoDBClient;
        amazonDynamoDBClient = new AmazonDynamoDBClient();
        ScanRequest req = new ScanRequest();
        req.setTableName("ArchitectQuiz");
        ScanResult result = amazonDynamoDBClient.scan(req)
        List quizItems = result.items
        int tableRowCount = quizItems.size()
        session.setAttribute("tableRowCount", Integer.toString(tableRowCount))
        log.info("This many rows in the table:  " + tableRowCount)
    }
}
