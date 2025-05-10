package bank.testovoe.exception;

public class ForbiddenOperationException extends ApiException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
