package com.epam.epmcacm.storageservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidResourceException extends  RuntimeException {

    public static final String EMPTY_FILE_MESSAGE = "File can't be empty";
    public static final String BLANK_FILE_MESSAGE = "File can't be blank!";
    public static final String WRONG_FILE_TYPE_MESSAGE = "File is not of type mp3!";

    public InvalidResourceException(String message)
    {
        super(message);
    }
}
