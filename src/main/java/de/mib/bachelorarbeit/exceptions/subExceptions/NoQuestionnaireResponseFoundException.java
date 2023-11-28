package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoQuestionnaireResponseFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoQuestionnaireResponseFoundException(String message) {
        super(message);
    }
}
