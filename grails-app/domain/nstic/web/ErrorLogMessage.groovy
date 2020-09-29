package nstic.web

/**
 * A simple place to store errors when they occur in the system, so we can analyze them later without fear of having to
 * dig through some damn log somewhere.
 * <br/><br/>
 * Created by brad on 3/31/16.
 */
class ErrorLogMessage {

    public static void quickAdd(String context, String message, Throwable error){
//        ErrorLogMessage.withTransaction {
//            ErrorLogMessage msg = new ErrorLogMessage();
//            msg.constraints = context;
//            msg.message = message;
//            if( error ){
//                msg.errorClass = error.getClass().getName();
//                msg.errorMessage = error.getMessage();
//                StringWriter stackTrace = new StringWriter();
//                PrintWriter printWriter = new PrintWriter(stackTrace);
//                error.printStackTrace(printWriter);
//                printWriter.flush();
//                printWriter.close();
//                msg.stackTrace = stackTrace.toString();
//
//                if( error.getCause() != null ) {
//                    Throwable cause = error;
//                    while (cause.getCause() != null)
//                        cause = cause.getCause();
//                    msg.causeClass = cause.getClass().getName();
//                    msg.causeMessage = cause.getMessage();
//                }
//            }
//            msg.save(failOnError: true);
//        }
    }//end quickAdd()

    public static void quickAdd(String context, String message){
//        quickAdd(context, message, null);
    }//end quickAdd()


    /**
     * When this failure occurred.
     */
    Date dateCreated
    /**
     * What we were trying to do at the time of the failure.
     */
    String context;
    /**
     * A statement about what the failure was, from the programmer's perspective.
     */
    String message;
    /**
     * The error stack trace, if available.
     */
    String stackTrace
    /**
     * The class of the thrown exception.
     */
    String errorClass
    /**
     * The message of the the thrown exception.
     */
    String errorMessage

    /**
     * If available, the class of the underlying cause exception.
     */
    String causeClass
    /**
     * If available, the message of the underlying cause exception.
     */
    String causeMessage

    static constraints = {
        dateCreated(nullable: true)
        context(nullable: false, blank: false, maxSize: 5096)
        message(nullable: false, blank: false, maxSize: 5096)
        stackTrace(nullable: true, blank: true, maxSize: 65535)
        errorClass(nullable: true, blank: true, maxSize: 65535)
        errorMessage(nullable: false, blank: false, maxSize: 65535)
        causeClass(nullable: true, blank: true, maxSize: 65535)
        causeMessage(nullable: true, blank: true, maxSize: 65535)
    }


    static mapping = {
        table(name: 'error_log')
        context(type: 'text')
        message(type: 'text')
        stackTrace(type: 'text')
        errorClass(type: 'text')
        errorMessage(type: 'text')
        causeClass(type: 'text')
        causeMessage(type: 'text')
    }


}
