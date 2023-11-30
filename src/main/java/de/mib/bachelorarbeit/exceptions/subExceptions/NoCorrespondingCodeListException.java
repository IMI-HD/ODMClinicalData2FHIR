package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingCodeListException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingCodeListException(String message) {
        super(message);
    }
}
