package com.findevglobal.cloud.stream.binder.amps.integration;

import java.util.Objects;
import java.util.UUID;

public class Dto {
    private String id;
    private String message;

    public static Dto dto(final String message) {
        Dto dto = new Dto();
        dto.setId(UUID.randomUUID().toString());
        dto.setMessage(message);
        return dto;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Dto dto = (Dto) o;
        return Objects.equals(id, dto.id) && Objects.equals(message, dto.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message);
    }

    @Override
    public String toString() {
        return "Dto{id='" + id + "', message='" + message + "'}";
    }
}
