package com.epam.epmcacm.storageservice.util;

import com.epam.epmcacm.storageservice.exceptions.InvalidResourceException;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {

    public enum FileAttribute {

        TRACK,
        ARTIST,
        ALBUM,
        LENGTH,
        YEAR;

    }

    private FileUtil() {
        throw new IllegalStateException("Utility class!");
    }

    public static void validateMultipartRequest(MultipartFile file) throws InvalidResourceException {
        if (file.getOriginalFilename().isEmpty()) throw new InvalidResourceException(InvalidResourceException.EMPTY_FILE_MESSAGE);
        if(file.getOriginalFilename().isBlank()) throw new InvalidResourceException(InvalidResourceException.BLANK_FILE_MESSAGE);
        String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if(!fileExtension.equals("mp3")) throw new InvalidResourceException(InvalidResourceException.WRONG_FILE_TYPE_MESSAGE);
    }


}
