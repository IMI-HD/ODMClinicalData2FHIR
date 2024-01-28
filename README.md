# ODMClinicalDataToFHIRQuestionnaireResponse

This application takes an ODM XML Questionnaire and 
converts the ClinicalData Element to a Bundle of QuestionnaireResponse Resources

## Installation and usage
### For self-hosting 

1. checkout repository
2. install an java 17 jdk & maven
3. call `mvn package`
4. run the produced .jar within the "target" dir

### For usage as docker container 

1. checkout repository
2. call `docker build -t odmtofhirconverter:1.0 .`
3. start container with `docker run -p 8080:8080 odmtofhirconverter:1.0`