package bank.testovoe.exception;

public class InsufficientFundsException extends ApiException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
