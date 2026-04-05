package com.fishlog.fishlog.exception;

public class PhotoNotFoundException extends RuntimeException {
    public PhotoNotFoundException(Long id) {
        super("사진을 찾을 수 없습니다. id=" + id);
    }
}
