package de.mib.bachelorarbeit.responses;

public record ConverterErrorResponse(
        String timestamp,
        int status,
        String error,
        String path
) {
}
