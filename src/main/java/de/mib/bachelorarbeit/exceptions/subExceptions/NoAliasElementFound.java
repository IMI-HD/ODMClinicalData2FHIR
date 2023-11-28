package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoAliasElementFound extends ClinicalDataToQuestionnaireResponseException {
    public NoAliasElementFound(String message) {
        super(message);
    }
}
