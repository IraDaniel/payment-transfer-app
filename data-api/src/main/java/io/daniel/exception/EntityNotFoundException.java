package io.daniel.exception;

public class EntityNotFoundException extends RuntimeException{
    private static final String MSG_TEMPLATE = "Cannot found %s with ID = %s";
    private Integer id;

    public EntityNotFoundException(String entityType, Integer id) {
        super(String.format(MSG_TEMPLATE, entityType, id));
        this.id = id;
    }

    public EntityNotFoundException(String message) {
        super(message);
    }
}
