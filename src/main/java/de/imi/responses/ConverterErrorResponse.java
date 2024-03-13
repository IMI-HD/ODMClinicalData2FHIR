package de.imi.responses;

import java.io.Serializable;

public record ConverterErrorResponse(
        String timestamp,
        int status,
        String error,
        String path
) implements Serializable {
    @Override
    public boolean equals(Object obj) {
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return null;
    }
}
