package client;

public class ResponseException extends RuntimeException {
    private int status;
    public ResponseException(int status, String message) {
        super(message);
        this.status = status;
    }
}
