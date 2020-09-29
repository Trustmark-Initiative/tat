package nstic.web

/**
 * Thrown when the user has requested something that is invalid.  Should render a 400: Bad Request instead of a 500 error.
 * Created by brad on 5/5/16.
 */
class InvalidRequestError extends RuntimeException {

    InvalidRequestError() {
    }

    InvalidRequestError(String var1) {
        super(var1)
    }

    InvalidRequestError(String var1, Throwable var2) {
        super(var1, var2)
    }

    InvalidRequestError(Throwable var1) {
        super(var1)
    }

    InvalidRequestError(String var1, Throwable var2, boolean var3, boolean var4) {
        super(var1, var2, var3, var4)
    }
}
