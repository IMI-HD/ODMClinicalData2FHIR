package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingItemDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingItemDefFoundException(String message) {
        super(message);
    }
}
