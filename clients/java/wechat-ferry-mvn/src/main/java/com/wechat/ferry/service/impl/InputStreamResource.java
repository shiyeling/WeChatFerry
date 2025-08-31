package com.wechat.ferry.service.impl;

import org.springframework.core.io.AbstractResource;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamResource extends AbstractResource {
    private final InputStream inputStream;
    private final String filename;
    private final String description;

    public InputStreamResource(InputStream inputStream, String filename) {
        this(inputStream, filename, "InputStream resource");
    }

    public InputStreamResource(InputStream inputStream, String filename, String description) {
        this.inputStream = inputStream;
        this.filename = filename;
        this.description = description;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    @Nullable
    public String getFilename() {
        return filename;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isReadable() {
        return true;
    }
}
