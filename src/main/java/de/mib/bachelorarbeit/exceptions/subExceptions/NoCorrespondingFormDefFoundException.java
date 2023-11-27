package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingFormDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingFormDefFoundException(String message) {
        super(message);
    }
}
