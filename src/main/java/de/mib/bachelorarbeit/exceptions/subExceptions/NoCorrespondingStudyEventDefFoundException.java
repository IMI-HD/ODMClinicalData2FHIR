package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingStudyEventDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingStudyEventDefFoundException(String message) {
        super(message);
    }
}
