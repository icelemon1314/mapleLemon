package database;

public class DatabaseException extends RuntimeException {

    private static final long serialVersionUID = -420103154764822555L;

    public DatabaseException(String msg) {
        super(msg);
    }

    public DatabaseException(Exception e) {
        super(e);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
