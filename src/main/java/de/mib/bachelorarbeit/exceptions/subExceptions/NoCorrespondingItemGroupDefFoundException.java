package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingItemGroupDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingItemGroupDefFoundException(String message) {
        super(message);
    }
}
