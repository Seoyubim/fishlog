package com.fishlog.fishlog.exception;

public class FishLogNotFoundException extends RuntimeException {
    public FishLogNotFoundException(Long id) {
        super("FishLog not found: id=" + id);
    }
}
