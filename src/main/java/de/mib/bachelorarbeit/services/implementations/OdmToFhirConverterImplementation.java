package de.mib.bachelorarbeit.services.implementations;


import ca.uhn.fhir.context.FhirContext;
import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.mib.bachelorarbeit.exceptions.subExceptions.*;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.*;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OdmToFhirConverterImplementation implements OdmToFhirConverter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OdmToFhirConverterImplementation.class);

    private final FhirContext fhirContext = FhirContext.forR4();

    private final String codingSystem;

    @Autowired
    public OdmToFhirConverterImplementation(
            Environment env
    ) {
        this.codingSystem = env.getProperty("coding");
    }

    @Override
    public String getTestRessource() {
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem("http://example.com/fhir/patient-identifiers")
                .setValue("12345");
        patient.addName()
                .setFamily("Mustermann")
                .addGiven("Max");
        LOGGER.info("Test patient created!");

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(patient);
    }

    @Override
    public String clinicalDataToQuestionnaireResponse(
            @NotNull ODM odm,
            String language,
            String linkToQuestionnaire
    )
            throws ClinicalDataToQuestionnaireResponseException {
        LOGGER.info("Received ODM in the converter!");
        // FHIR Bundle that will be returned
        Bundle bundle = createBundle();
        // List of all QRS contained in the final Bundle
        List<QuestionnaireResponse> _qrs = new ArrayList<>();
        // List of <ClinicalData> Elements from ODM
        List<ODMcomplexTypeDefinitionClinicalData> _clinicalData;
        // List of <Study> Elements from ODM
        List<ODMcomplexTypeDefinitionStudy> _study;
        // <ClinicalData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionClinicalData clinicalData;
        // <SubjectData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionSubjectData subjectData;
        // <Study> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionStudy study;
        // <MetaDataVersion> Element at index 0 of the <Study> (only this will be converted!)
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion;

        // set the expected language string according to header parameter
        String givenLanguage = "";
        if (language.equals("german")) {
            givenLanguage = "de";
        }
        if (language.equals("english")) {
            givenLanguage = "en";
        }


        // validate basic structure of the ODM file

        // check if a <ClinicalData> Element is present!
        if (odm.getClinicalData().isEmpty()) {
            LOGGER.error("<ClinicalData> was empty!");
            throw new ClinicalDataNotFoundException("<ClinicalData> was empty!");
        } else {
            // set <ClinicalData> from ODM
            LOGGER.info("Set <ClinicalData> list!");
            _clinicalData = odm.getClinicalData();
            try {
                clinicalData = _clinicalData.get(0);
                LOGGER.info("<ClinicalData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <ClinicalData> found at index 0!");
                throw new ClinicalDataNotFoundException("No <ClinicalData> found at index 0!");
            }
        }

        // check if a <SubjectData> Element is present in the selected <ClinicalData>
        if (clinicalData.getSubjectData().isEmpty()) {
            LOGGER.error("<SubjectData> was empty!");
            throw new SubjectDataNotFoundException("<SubjectData> was empty!");
        } else {
            // set <SubjectData> at index 0 from selected <ClinicalData>
            try {
                subjectData = clinicalData.getSubjectData().get(0);
                LOGGER.info("<SubjetData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <SubjectData> found at index 0!");
                throw new SubjectDataNotFoundException("No <SubjectData> found at index 0!");
            }
        }

        // check if a <Study> Element is present!
        if (odm.getStudy().isEmpty()) {
            LOGGER.error("<Study> was empty!");
            throw new StudyNotFoundException("<Study> was empty!");
        } else {
            // set <Study> from ODM
            LOGGER.info("Set <Study> list!");
            _study = odm.getStudy();
            try {
                study = _study.get(0);
                LOGGER.info("<Study> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <Study> found at index 0!");
                throw new StudyNotFoundException("No <Study> found at index 0!");
            }
        }

        // check if a <MetaDataVersion> from the selected <Study> is present!
        if (study.getMetaDataVersion().isEmpty()) {
            LOGGER.error("<MetaDataVersion> was empty!");
            throw new MetaDataVersionNotFoundException("<MetaDataVersion> was empty!");
        } else {
            // set <MetaDataVersion> at index 0 from selected <Study> from ODM
            try {
                metaDataVersion = study.getMetaDataVersion().get(0);
                LOGGER.info("<MetaDataVersion> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <MetaDataVersion> found at index 0!");
                throw new MetaDataVersionNotFoundException("No <MetaDataVersion> found at index 0!");
            }
        }

        LOGGER.info("All data fields set! Looping through ODM");

        // check if <Study> and <ClinicalData> OIDs match
        if (!clinicalData.getStudyOID().equals(study.getOID())) {
            String errorString = String.format("<ClinicalData> StudyOID: %s does not match provided <Study> OID: %s",
                    clinicalData.getStudyOID(), study.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // check if <ClinicalData> MetaDataVersionOID match provided <MetaDataVersion> OID
        if (!metaDataVersion.getOID().equals(clinicalData.getMetaDataVersionOID())) {
            String errorString = String.format("<ClinicalData> MetaDataVersionOID: %s does not " +
                                               "match provided <MetaDataVersion> OID: %s",
                    clinicalData.getMetaDataVersionOID(), metaDataVersion.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // Loop through all <StudyEventData> => QuestionnaireResponse
        if (subjectData.getStudyEventData().isEmpty()) {
            String error = "No <StudyEventData> Element found in <SubjectData>";
            LOGGER.error(error);
            throw new StudyEventDataNotFoundException(error);
        }

        // List of all <StudyEventData> Elements in one <SubjectData>
        // Each Element will be converted to a Single QuestionnaireResponse
        List<ODMcomplexTypeDefinitionStudyEventData> _studyEventData = subjectData.getStudyEventData();

        for (ODMcomplexTypeDefinitionStudyEventData studyEventData : _studyEventData) {

            LOGGER.info(
                    String.format("Converting <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID())
            );
            // QuestionnaireResponse
            QuestionnaireResponse qrs = createQuestionnaireResponseBase(linkToQuestionnaire);
            // Create root Element
            QuestionnaireResponse.QuestionnaireResponseItemComponent root = qrs.addItem();
            // set the linkId of the root element (StudyOID)
            root.setLinkId(studyEventData.getStudyEventOID());
            //List of <StudyEventDef> from <MetaDataVersion>
            List<ODMcomplexTypeDefinitionStudyEventDef> _studyEvents
                    = metaDataVersion.getStudyEventDef();
            LOGGER.info(
                    String.format("Searching for corresponding <StudyEventDef> in <MetaDataVersion>" +
                                  " for <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID()));
            // Search for corresponding <StudyEventDef> in the <MetaDataVersion>
            Optional<ODMcomplexTypeDefinitionStudyEventDef> studyEventDef = _studyEvents.stream()
                    .filter(obj -> studyEventData.getStudyEventOID().equals(obj.getOID()))
                    .findFirst();
            if (studyEventDef.isPresent()) {
                LOGGER.info(
                        String.format("<StudyEventDef> with OID: %s found!",
                                studyEventDef.get().getOID()));
            } else {
                String error = String.format("No corresponding <StudyEventDef> with OID: %s found!",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new NoCorrespondingStudyEventDefFoundException(error);
            }

            // check if the <StudyEvent> is repeating if so
            // => add extension with repeating key
            if (studyEventDef.get().getRepeating() == YesOrNo.YES) {
                String message = String.format(
                        "<StudyEvent> with OID: '%s' found repeating! Adding key: '%s' to item",
                        studyEventDef.get().getOID(),
                        studyEventData.getStudyEventRepeatKey()
                );
                LOGGER.info(message);
                addRepeatKeyToItem(studyEventData.getStudyEventRepeatKey(), root);
            }

            // List of <FormData> => linkId under the root (1.x)
            List<ODMcomplexTypeDefinitionFormData> _formData = studyEventData.getFormData();
            if (_formData.isEmpty()) {
                String error = String.format("No <FormData> found in <StudyEventData> with OID: %s",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new FormDataNotFoundException(error);
            } else {
                LOGGER.info("<FormData> found in <StudyEventData>");
            }

            // <FormDef> list from <MetaDataVersion> this list
            // will be searched for each <FormData>
            List<ODMcomplexTypeDefinitionFormDef> _formDef = metaDataVersion.getFormDef();

            // Loop through all <FormData> Elements
            for (ODMcomplexTypeDefinitionFormData formData : _formData) {

                // Current <FormData> Element
                // Search for corresponding <FormDef> in the <MetaDataVersion>
                LOGGER.info(
                        String.format("Searching for corresponding " +
                                      "<FormDef> with OID: %s in <MetaDataVersion>",
                                formData.getFormOID()));
                Optional<ODMcomplexTypeDefinitionFormDef> formDef = _formDef.stream()
                        .filter(obj -> formData.getFormOID().equals(obj.getOID()))
                        .findFirst();

                if (formDef.isPresent()) {
                    LOGGER.info(
                            String.format("<FormDef> with OID: %s found!",
                                    formDef.get().getOID()));
                } else {
                    String error = String.format("No corresponding <FormDef> found for <FormData> with OID: %s",
                            formData.getFormOID());
                    LOGGER.error(error);
                    throw new NoCorrespondingFormDefFoundException(error);
                }

                // Create QRS Item one level under the root
                QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x = root.addItem();
                LOGGER.info(
                        String.format("Converting <FormData> with OID: %s",
                                formData.getFormOID()));

                // check if the <Form> is repeating if so
                // => add extension with repeating key
                if (formDef.get().getRepeating() == YesOrNo.YES) {
                    String message = String.format(
                            "<Form> with OID: '%s' found repeating! Adding key: '%s' to item",
                            formDef.get().getOID(),
                            formData.getFormRepeatKey()
                    );
                    LOGGER.info(message);
                    addRepeatKeyToItem(formData.getFormRepeatKey(), item_1_x);
                }


                // Set text of the item
                ODMcomplexTypeDefinitionDescription formDescription = formDef.get().getDescription();
                if (formDescription.getTranslatedText().isEmpty()) {
                    String error = String.format(
                            "No <TranslatedText> in <Description> found for <FormDef> with OID: %s!",
                            formDef.get().getOID()
                    );
                    LOGGER.error(error);
                    throw new FormDescriptionNotFoundException(error);
                } else {
                    LOGGER.info("Set text of linkId 1.x Element!");
                    item_1_x.setText(getStringFormTranslatedText(
                            formDef.get().getDescription().getTranslatedText(),
                            givenLanguage));
                }

                // Set linkId of item_1_x
                item_1_x.setLinkId(formData.getFormOID());

                // List of <ItemGroupData> in <FormData>
                List<ODMcomplexTypeDefinitionItemGroupData> _itemGroupData =
                        formData.getItemGroupData();
                if (_itemGroupData.isEmpty()) {
                    String error = String.format("No <ItemGroupData> found in <FormData> with OID: %s",
                            formData.getFormOID());
                    LOGGER.error(error);
                    throw new ItemGroupDataNotFoundException(error);
                } else {
                    LOGGER.info("<ItemGroupData> found in <FormData>");
                }

                // <ItemGroupDef> list from <MetaDataVersion> list
                // will be searched for each <ItemGroupData>
                List<ODMcomplexTypeDefinitionItemGroupDef> _itemGroupDef = metaDataVersion.getItemGroupDef();

                // Loop through all <ItemGroupData>
                for (ODMcomplexTypeDefinitionItemGroupData itemGroupData : _itemGroupData) {
                    // Current <ItemGroupData> Element
                    // Search for corresponding <ItemGroupDef> in <MetaDataVersion>
                    LOGGER.info(
                            String.format("Searching for corresponding <ItemGroupDef>" +
                                          " with OID: %s in <MetaDataVersion",
                                    itemGroupData.getItemGroupOID()));
                    Optional<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDef = _itemGroupDef.stream()
                            .filter(
                                    obj -> itemGroupData.getItemGroupOID()
                                            .equals(obj.getOID()))
                            .findFirst();

                    if (itemGroupDef.isPresent()) {
                        LOGGER.info(
                                String.format("<ItemGroupDef> with OID: %s found!",
                                        itemGroupDef.get().getOID()));
                    } else {
                        String error = String.format("No corresponding <ItemGroupDef> found for " +
                                                     "<ItemGroupData> with OID: %s",
                                itemGroupData.getItemGroupOID());
                        LOGGER.error(error);
                        throw new NoCorrespondingItemGroupDefFoundException(error);
                    }

                    // Create QRS Item one level under the Forms (1.x.x)
                    QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x = item_1_x.addItem();
                    LOGGER.info(
                            String.format("Converting <ItemGroupData> with OID: %s",
                                    itemGroupData.getItemGroupOID()));

                    // check if the <ItemGroup> is repeating if so
                    // => add extension with repeating key
                    if (itemGroupDef.get().getRepeating() == YesOrNo.YES) {
                        String message = String.format(
                                "<ItemGroup> with OID: '%s' found repeating! Adding key: '%s' to item",
                                itemGroupDef.get().getOID(),
                                itemGroupData.getItemGroupRepeatKey()
                        );
                        LOGGER.info(message);
                        addRepeatKeyToItem(itemGroupData.getItemGroupRepeatKey(), item_1_x_x);
                    }

                    // Set text of the item
                    ODMcomplexTypeDefinitionDescription itemGroupDescription = itemGroupDef.get().getDescription();
                    if (itemGroupDescription.getTranslatedText().isEmpty()) {
                        String error = String.format(
                                "No <TranslatedText> in <Description> found for <ItemGroupDef> with OID: %s!",
                                itemGroupDef.get().getOID()
                        );
                        LOGGER.error(error);
                        throw new ItemGroupDefDescriptionNotFoundException(error);
                    } else {
                        LOGGER.info("Set text of linkId 1.x.x Element!");
                        item_1_x_x.setText(getStringFormTranslatedText(
                                itemGroupDescription.getTranslatedText(),
                                givenLanguage
                        ));
                    }

                    // set linkId of item_1_x_x
                    item_1_x_x.setLinkId(itemGroupData.getItemGroupOID());


                    // List of <ItemData> in <ItemGroupData>
                    List<ODMcomplexTypeDefinitionItemData> _itemData =
                            itemGroupData.getItemDataGroup();
                    if (_itemData.isEmpty()) {
                        String error = String.format("No <ItemData> found in <ItemGroupData> with OID: %s",
                                itemGroupData.getItemGroupOID());
                        LOGGER.error(error);
                        throw new ItemDataNotFoundException(error);
                    } else {
                        LOGGER.info("<ItemData> found in <ItemGroupData>");
                    }

                    // <ItemDef> list from <MetaDataVersion> list
                    // will be searched for each <ItemData>
                    List<ODMcomplexTypeDefinitionItemDef> _itemDef = metaDataVersion.getItemDef();

                    // Loop through all <ItemData>
                    for (ODMcomplexTypeDefinitionItemData itemData : _itemData) {

                        // Current <ItemData> Element
                        // Search for corresponding <ItemDef> in <MetaDataVersion>
                        LOGGER.info(
                                String.format("Searching for corresponding <ItemDef>" +
                                              " with OID: %s in <MetaDataVersion>",
                                        itemData.getItemOID()));
                        Optional<ODMcomplexTypeDefinitionItemDef> itemDef = _itemDef.stream()
                                .filter(
                                        obj -> itemData.getItemOID()
                                                .equals(obj.getOID()))
                                .findFirst();

                        // Check if <ItemDef> was found
                        if (itemDef.isPresent()) {
                            LOGGER.info(
                                    String.format("<ItemDef> with OID: %s found!",
                                            itemDef.get().getOID()));
                        } else {
                            String error = String.format("No corresponding <ItemDef> found for " +
                                                         "<ItemData> with OID: %s",
                                    itemData.getItemOID());
                            LOGGER.error(error);
                            throw new NoCorrespondingItemDefFoundException(error);
                        }

                        // Create QRS Item one level under the ItemGroup (1.x.x.x)
                        QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x_x = item_1_x_x.addItem();
                        LOGGER.info(
                                String.format("Converting <ItemData> with OID: %s",
                                        itemData.getItemOID()));

                        // Set text of the item
                        ODMcomplexTypeDefinitionQuestion itemQuestion = itemDef.get().getQuestion();
                        if (itemQuestion.getTranslatedText().isEmpty()) {
                            String error = String.format(
                                    "No <TranslatedText> in <Question> found for <ItemDef> with OID: %s!",
                                    itemDef.get().getOID()
                            );
                            LOGGER.error(error);
                            throw new ItemGroupDefDescriptionNotFoundException(error);
                        } else {
                            LOGGER.info("Set text of linkId 1.x.x.x Element!");
                            item_1_x_x_x.setText(getStringFormTranslatedText(
                                    itemQuestion.getTranslatedText(),
                                    givenLanguage
                            ));
                        }

                        // set linkId of item_1_x_x_x
                        item_1_x_x_x.setLinkId(itemData.getItemOID());


                        // Check if Item is coded
                        if (itemDef.get().getCodeListRef() != null) {
                            LOGGER.info(String.format(
                                    "<CodeListRef> found for <ItemDef> with OID: %s!",
                                    itemDef.get().getOID()
                            ));
                            // List of <CodeList> from <MetaDataVersion>
                            List<ODMcomplexTypeDefinitionCodeList> _codeList = metaDataVersion.getCodeList();
                            // Search the list for corresponding <CodeList> from given <CodeListRef>
                            if (_codeList.isEmpty()) {
                                String error = "<CodeList> was empty!";
                                LOGGER.error(error);
                                throw new CodeListWasEmptyException(error);
                            }

                            LOGGER.info(
                                    String.format("Searching for <CodeList> with OID: %s",
                                            itemDef.get().getCodeListRef().getCodeListOID()));
                            // <CodeList> with matching OID of <CodeListRef> from <ItemDef>
                            Optional<ODMcomplexTypeDefinitionCodeList> codeList = _codeList.stream()
                                    .filter(obj ->
                                            itemDef.get().getCodeListRef().getCodeListOID().equals(obj.getOID()))
                                    .findFirst();

                            if (codeList.isPresent()) {
                                LOGGER.info(
                                        String.format("Corresponding <CodeList> found with OID: %s",
                                                codeList.get().getOID())
                                );
                                // List of <CodeListItems> inside the <CodeList>
                                List<ODMcomplexTypeDefinitionCodeListItem> _codeListItem =
                                        codeList.get().getCodeListItem();
                                LOGGER.info(
                                        String.format("Searching for <CodeListItem> with CodedValue=%s",
                                                itemData.getValue())
                                );
                                if (_codeListItem.isEmpty()) {
                                    String error = "<CodeList> was empty!";
                                    LOGGER.error(error);
                                    throw new NoCodeListItemsException(error);
                                }
                                // Search for matching CodedValue
                                Optional<ODMcomplexTypeDefinitionCodeListItem> codeListItem =
                                        _codeListItem.stream()
                                                .filter(obj -> itemData.getValue().equals(obj.getCodedValue()))
                                                .findFirst();

                                if (codeListItem.isPresent()) {
                                    // Set coded Value as answer
                                    // Assumption: if more than one <Alias> given select the one at index 0

                                    // check if a <MeasurementUnit> for the item is given
                                    if (itemDef.get().getMeasurementUnitRef().isEmpty()) {
                                        LOGGER.info(
                                                String.format("No <MeasurementUnitRef> found in" +
                                                              " <ItemDef> with OID: %s",
                                                        itemDef.get().getOID())
                                        );
                                        // continue with no <MeasurementUnit>
                                        String itemDataType = itemDef.get().getDataType().value();
                                        String data = getStringFormTranslatedText(
                                                codeListItem.get().getDecode().getTranslatedText(),
                                                givenLanguage
                                        );
                                        setFhirDataWithoutUnit(itemDef, item_1_x_x_x, itemDataType, data);


                                    } else {
                                        LOGGER.info(
                                                String.format("<MeasurementUnitRef> found in" +
                                                              " <ItemDef> with OID: %s",
                                                        itemDef.get().getOID())
                                        );
                                        // continue with <MeasurementUnit>
                                        // Auto select Ref at index 0
                                        ODMcomplexTypeDefinitionMeasurementUnitRef measurementUnitRef =
                                                itemDef.get().getMeasurementUnitRef().get(0);

                                        // Search for corresponding <MeasurementUnit> in the <MetaDataVersion>
                                        List<ODMcomplexTypeDefinitionMeasurementUnit> _measurementUnit =
                                                study.getBasicDefinitions().getMeasurementUnit();


                                        Optional<ODMcomplexTypeDefinitionMeasurementUnit> measurementUnit =
                                                findMeasurementUnit(measurementUnitRef, _measurementUnit);

                                        if (measurementUnit.isPresent()) {
                                            LOGGER.info(
                                                    String.format(
                                                            "<MeasurementUnit> found for" +
                                                            " <MeasurementUnitRef> with OID: %s",
                                                            measurementUnitRef.getMeasurementUnitOID()
                                                    )
                                            );

                                            //valueQuantity
                                            // Todo: insert Coding if Alias for UCUM is given
                                            Quantity quantity = new Quantity();
                                            try {
                                                quantity.setValue(Float.parseFloat(
                                                        getStringFormTranslatedText(
                                                                codeListItem.get().getDecode().getTranslatedText(),
                                                                givenLanguage
                                                        )
                                                ));
                                                quantity.setUnit(
                                                        getStringFormTranslatedText(
                                                                measurementUnit.get().getSymbol().getTranslatedText(),
                                                                givenLanguage
                                                        )
                                                );
                                            } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                                                LOGGER.error(indexOutOfBoundsException.getMessage());
                                                throw new TranslatedTextNotFoundException("No translated " +
                                                                                          "text at index 0!");
                                            } catch (NumberFormatException numberFormatException) {
                                                String error = String.format(
                                                        "The given data: '%s' with MeasurementUnitRef was not numerical!",
                                                        getStringFormTranslatedText(
                                                                codeListItem.get().getDecode().getTranslatedText(),
                                                                givenLanguage
                                                        )
                                                );
                                                LOGGER.error(error);
                                                throw new ItemDataWithUnitWasNotNumericalException(error);
                                            }

                                            // Add Alias (valueCoding)
                                            addAliasToItem(itemDef, item_1_x_x_x);

                                            // Add valueQuantity created above
                                            item_1_x_x_x.addAnswer()
                                                    .setValue(quantity);

                                        } else {
                                            String error = String.format(
                                                    "No corresponding <MeasurementUnit> found for" +
                                                    " <MeasurementUnitRef> with OID: %s",
                                                    measurementUnitRef.getMeasurementUnitOID()
                                            );
                                            LOGGER.error(error);
                                            throw new NoCorrespondingMeasurementUnitException(error);
                                        }

                                    }


                                } else {
                                    String error = String.format("No matching <CodeListItem> with" +
                                                                 " CodedValue=%s found!",
                                            itemData.getValue()
                                    );
                                    LOGGER.error(error);
                                    throw new NoMatchingCodeListItemException(error);
                                }

                            } else {
                                String error = String.format("No corresponding <CodeList> found for <CodeListRef>" +
                                                             "with OID: %s",
                                        itemDef.get().getCodeListRef().getCodeListOID());
                                LOGGER.error(error);
                                throw new NoCorrespondingCodeListException(error);
                            }

                        } else {
                            LOGGER.info(String.format(
                                    "No coding for <ItemDef> with OID: %s found!",
                                    itemDef.get().getOID()
                            ));
                            // Insert answer[] into item
                            // WARNING may produce IndexOutOfBoundsException
                            // ToDo: generalize here and fix IndexOutOfBounds Alias
                            // check if a <MeasurementUnit> for the item is given
                            if (itemDef.get().getMeasurementUnitRef().isEmpty()) {
                                LOGGER.info(
                                        String.format("No <MeasurementUnitRef> found in" +
                                                      " <ItemDef> with OID: %s",
                                                itemDef.get().getOID())
                                );

                                // continue with no <MeasurementUnit>
                                String itemDataType = itemDef.get().getDataType().value();
                                String data = itemData.getValue();
                                setFhirDataWithoutUnit(itemDef, item_1_x_x_x, itemDataType, data);


                            } else {
                                LOGGER.info(
                                        String.format("<MeasurementUnitRef> found in" +
                                                      " <ItemDef> with OID: %s",
                                                itemDef.get().getOID())
                                );

                                // continue with <MeasurementUnit>
                                // Auto select Ref at index 0
                                ODMcomplexTypeDefinitionMeasurementUnitRef measurementUnitRef =
                                        itemDef.get().getMeasurementUnitRef().get(0);

                                // Search for corresponding <MeasurementUnit> in the <MetaDataVersion>
                                List<ODMcomplexTypeDefinitionMeasurementUnit> _measurementUnit =
                                        study.getBasicDefinitions().getMeasurementUnit();

                                findMeasurementUnit(measurementUnitRef, _measurementUnit);

                                Optional<ODMcomplexTypeDefinitionMeasurementUnit> measurementUnit =
                                        findMeasurementUnit(measurementUnitRef, _measurementUnit);

                                if (measurementUnit.isPresent()) {
                                    LOGGER.info(
                                            String.format(
                                                    "<MeasurementUnit> found for" +
                                                    " <MeasurementUnitRef> with OID: %s",
                                                    measurementUnitRef.getMeasurementUnitOID()
                                            )
                                    );

                                    //valueQuantity
                                    // Todo: insert Coding if Alias for UCUM is given
                                    Quantity quantity = new Quantity();
                                    quantity.setValue(Float.parseFloat(
                                            itemData.getValue()
                                    ));
                                    quantity.setUnit(
                                            getStringFormTranslatedText(
                                                    measurementUnit.get().getSymbol().getTranslatedText(),
                                                    givenLanguage
                                            )
                                    );

                                    addAliasToItem(itemDef, item_1_x_x_x);

                                    item_1_x_x_x.addAnswer()
                                            .setValue(quantity);

                                } else {
                                    String error = String.format(
                                            "No corresponding <MeasurementUnit> found for" +
                                            " <MeasurementUnitRef> with OID: %s",
                                            measurementUnitRef.getMeasurementUnitOID()
                                    );
                                    LOGGER.error(error);
                                    throw new NoCorrespondingMeasurementUnitException(error);
                                }

                            }

                        }

                    }

                }
            }

            // Add finished QuestionnaireResponse to the _qrs List
            LOGGER.info("Add QuestionnaireResponse to List!");
            _qrs.add(qrs);

        }

        // Add all QuestionnaireResponse Resources from the List the Bundle
        if (_qrs.isEmpty()) {
            String error = "No QuestionnaireResponse was found in the List!";
            LOGGER.error(error);
            throw new NoQuestionnaireResponseFoundException(error);
        }

        for (QuestionnaireResponse qrs : _qrs) {
            bundle.addEntry().setResource(qrs);
        }

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(bundle);
    }

    private void addRepeatKeyToItem(
            String repeatKey,
            QuestionnaireResponse.QuestionnaireResponseItemComponent item
    ) throws NoIntegerTypeException {

        try {
            int key = Integer.parseInt(repeatKey);
            Extension extension = new Extension();
            // ToDo: replace at given time with actual published URL
            extension.setUrl("http://example.org/fhir/StructureDefinition/extension-repeating-key");
            extension.setValue(
                    new IntegerType(key)
            );
            item.addExtension(extension);
        } catch (NumberFormatException e) {
            String error = String.format(
                    "Given repeatKey: '%s' was not an integer type!",
                    repeatKey
            );
            LOGGER.error(error);
            throw new NoIntegerTypeException(error);
        }
    }

    private Optional<ODMcomplexTypeDefinitionMeasurementUnit> findMeasurementUnit(
            ODMcomplexTypeDefinitionMeasurementUnitRef measurementUnitRef,
            List<ODMcomplexTypeDefinitionMeasurementUnit> _measurementUnit

    ) {
        return
                _measurementUnit.stream()
                        .filter(obj ->
                                measurementUnitRef
                                        .getMeasurementUnitOID()
                                        .equals(obj.getOID()))
                        .findFirst();
    }

    private void setFhirDataWithoutUnit(
            Optional<ODMcomplexTypeDefinitionItemDef> itemDef,
            QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x_x,
            String itemDataType,
            String data
    ) throws UnknownDataTypeException {
        try {
            // ToDo: parse to actual type and catch exception for easier debugging
            switch (itemDataType) {
                case "integer" -> {
                    int value = Integer.parseInt(data);
                    IntegerType integerType = new IntegerType(value);
                    item_1_x_x_x.addAnswer()
                            .setValue(integerType);
                }
                case "float" -> {
                    float value = Float.parseFloat(data);
                    DecimalType decimalType = new DecimalType(value);
                    item_1_x_x_x.addAnswer()
                            .setValue(decimalType);
                }
                case "text" -> {
                    StringType stringType = new StringType(data);
                    item_1_x_x_x.addAnswer()
                            .setValue(stringType);
                }
                case "date" -> {
                    DateType dateType = new DateType(data);
                    item_1_x_x_x.addAnswer()
                            .setValue(dateType);
                }
                case "boolean" -> {
                    //convert 0 and 1 coded bool to false / true
                    String value = "";
                    if (data.equals("0")) {
                        value = "false";
                    }
                    if (data.equals("1")) {
                        value = "true";
                    }
                    if (value.isEmpty() && !data.equals("true") && !data.equals("false")) {
                        String error = String.format(
                                "Coding: '%s' for bool data is not known!",
                                data
                        );
                        LOGGER.error(error);
                        throw new UnknownBoolCodingException(error);
                    }
                    BooleanType booleanType = new BooleanType(value);
                    item_1_x_x_x.addAnswer()
                            .setValue(booleanType);
                }
                default -> {
                    LOGGER.warn(String.format(
                            "Data type: '%s' is not known",
                            itemDataType
                    ));
                    throw new UnknownDataTypeException("Unknown data type!");
                }
            }

            addAliasToItem(itemDef, item_1_x_x_x);
        } catch (Exception e) {
            String error = String.format(
                    "The given data type: '%s' is unknown! Or the data '%s' did not match the expected data type!",
                    itemDataType,
                    data
            );
            LOGGER.error(error);
            throw new UnknownDataTypeException(error);
        }

    }

    // ToDo: verify that presence of <Alias> is verified
    private void addAliasToItem(
            Optional<ODMcomplexTypeDefinitionItemDef> itemDef,
            QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x_x) throws UnknownCodingSystemException {
        if (itemDef.get().getAlias().isEmpty()) {
            LOGGER.warn(
                    String.format("No <Alias> found in <ItemDef> with OID: %s",
                            itemDef.get().getOID())
            );
        } else {
            LOGGER.info(
                    String.format("<Alias> found in <ItemDef> with OID: %s",
                            itemDef.get().getOID())
            );
            // check if the given coding system matches the expected one
            // Todo: fix index out of bounds!
            String itemSystem = itemDef.get().getAlias().get(0).getContext();
            if (!codingSystem.equals(itemSystem)) {
                String error = String.format(
                        "Coding system in <ItemDef> : '%s' does not match expected system: '%s'",
                        itemSystem,
                        codingSystem
                );
                LOGGER.error(error);
                throw new UnknownCodingSystemException(error);
            }
            // valueCoding
            Coding coding = new Coding();
            switch (codingSystem) {
                case "LOINC" -> {
                    coding.setSystem("http://loinc.org");
                }
                case "SNOMED CT" -> {
                    coding.setSystem("http://snomed.info/sct");
                }
                default -> {
                    String error = String.format(
                            "Coding system: '%s' unknown",
                            codingSystem
                    );
                    LOGGER.error(error);
                    throw new UnknownCodingSystemException(error);
                }
            }
            coding.setCode(
                    itemDef.get().getAlias().get(0).getName()
            );

            // add to answer array
            item_1_x_x_x.addAnswer()
                    .setValue(coding);
        }
    }

    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        return bundle;
    }

    private QuestionnaireResponse createQuestionnaireResponseBase(
            String linkToQuestionnaire
    ) {
        QuestionnaireResponse qrs = new QuestionnaireResponse();
        qrs.setQuestionnaire(linkToQuestionnaire);
        qrs.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        return qrs;
    }

    private String getStringFormTranslatedText(List<ODMcomplexTypeDefinitionTranslatedText> _elements,
                                               String expectedLanguage)
            throws NoElementWithExpectedLanguageException {
        for (ODMcomplexTypeDefinitionTranslatedText element : _elements) {
            if (element.getLang().equals(expectedLanguage)) {
                return element.getValue();
            }
        }
        String error = String.format(
                "No element found with expected xml:language: '%s'",
                expectedLanguage
        );
        LOGGER.error(error);
        throw new NoElementWithExpectedLanguageException(error);
    }

}
