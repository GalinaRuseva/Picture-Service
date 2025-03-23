package app.exception;

public class PictureNotFoundException extends RuntimeException {

    public PictureNotFoundException(String message) {
        super(message);
    }

    public PictureNotFoundException() {
    }
}
