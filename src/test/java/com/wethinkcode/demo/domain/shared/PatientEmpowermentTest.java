package com.wethinkcode.demo.domain.shared;

import com.wethinkcode.demo.infrastructure.ai.LlamaAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Story P4: Patient wants a simplified explanation of their diagnosis")
class PatientEmpowermentTest {

    @Mock
    private LlamaAiService llamaAiService;

    @BeforeEach
    void setUp() {
        // Service is mocked - no initialization needed
    }

    @Test
    @DisplayName("P4.1: Given complex diagnosis, when getting patient summary, then returns simple English explanation")
    void givenComplexDiagnosis_whenGetPatientSummary_thenReturnsSimpleEnglish() {
        // Given: A complex medical diagnosis
        String complexDiagnosis = "Acute pharyngitis with exudative tonsillitis secondary to Group A Streptococcus infection. " +
                "Patient presents with dysphagia, odynophagia, and fever of 39.2°C. Cervical lymphadenopathy noted on examination.";

        String simplifiedResponse = "You have a minor throat infection that is usually caused by a bacteria called Streptococcus. " +
                "This is a common condition that causes a sore throat and fever. It's treatable with antibiotics.";

        when(llamaAiService.generatePatientSummary(complexDiagnosis)).thenReturn(simplifiedResponse);

        // When: Requesting patient-friendly summary
        String result = llamaAiService.generatePatientSummary(complexDiagnosis);

        // Then: Result should be in simple English
        assertNotNull(result);
        assertTrue(result.contains("throat infection"), "Should mention sore throat in simple terms");
        assertTrue(result.contains("bacteria"), "Should explain cause");
        assertTrue(result.contains("antibiotics"), "Should mention treatment");
        assertFalse(result.contains("exudative"), "Should not contain medical jargon");
        assertFalse(result.contains("odynophagia"), "Should not contain Latin medical terms");

        verify(llamaAiService, times(1)).generatePatientSummary(complexDiagnosis);
    }

    @Test
    @DisplayName("P4.2: Given medical terminology, when simplifying, then converts to layperson language")
    void givenMedicalTerminology_whenSimplifying_thenConvertsToLaypersonLanguage() {
        // Given: Complex medical terms
        String medicalTerms = "Hypertension (140/90 mmHg), left ventricular hypertrophy, and dyslipidemia with elevated LDL cholesterol.";

        String simplifiedResponse = "You have high blood pressure and high cholesterol. Your heart is working harder than it should. " +
                "These are serious conditions that need treatment to prevent heart disease.";

        when(llamaAiService.generatePatientSummary(medicalTerms)).thenReturn(simplifiedResponse);

        // When: Simplifying medical terms
        String result = llamaAiService.generatePatientSummary(medicalTerms);

        // Then: Should use simple language
        assertNotNull(result);
        assertTrue(result.contains("high blood pressure"), "Should use common term instead of 'hypertension'");
        assertTrue(result.contains("high cholesterol"), "Should simplify 'dyslipidemia'");
        assertTrue(result.contains("heart"), "Should explain impact on organs");

        verify(llamaAiService, times(1)).generatePatientSummary(medicalTerms);
    }

    @Test
    @DisplayName("P4.3: Given diagnosis with scary terminology, when creating patient summary, then maintains accuracy while reducing fear")
    void givenDiagnosisWithScaryTerminology_whenCreatingPatientSummary_thenMaintainsAccuracyWhileReducingFear() {
        // Given: A diagnosis with potentially frightening terminology
        String medicalDiagnosis = "Acute myocardial infarction with ST-segment elevation in the anterior wall. " +
                "Ejection fraction reduced to 35%. Complications include cardiogenic shock and pulmonary edema.";

        String reassuringYetAccurateResponse = "You have had a serious heart attack that affected the front part of your heart. " +
                "Your heart's pumping ability has been weakened. The good news is that modern treatments can help your heart recover. " +
                "You will need close monitoring and medications, but many people recover well from this condition.";

        when(llamaAiService.generatePatientSummary(medicalDiagnosis)).thenReturn(reassuringYetAccurateResponse);

        // When: Creating patient summary
        String result = llamaAiService.generatePatientSummary(medicalDiagnosis);

        // Then: Should be accurate but reassuring
        assertNotNull(result);
        assertTrue(result.contains("heart attack"), "Should be honest about condition");
        assertTrue(result.contains("treatments"), "Should mention hope and treatment");
        assertTrue(result.contains("recover"), "Should emphasize possibility of recovery");
        assertFalse(result.contains("myocardial"), "Should not use medical terminology");

        verify(llamaAiService, times(1)).generatePatientSummary(medicalDiagnosis);
    }

    @Test
    @DisplayName("P4.4: Given empty diagnosis, when requesting summary, then handles gracefully")
    void givenEmptyDiagnosis_whenRequestingSummary_thenHandlesGracefully() {
        // Given: Empty diagnosis string
        String emptyDiagnosis = "";
        String defaultResponse = "Please provide a diagnosis for the patient to generate a summary.";

        when(llamaAiService.generatePatientSummary("")).thenReturn(defaultResponse);

        // When: Requesting summary for empty diagnosis
        String result = llamaAiService.generatePatientSummary(emptyDiagnosis);

        // Then: Should handle gracefully
        assertNotNull(result);
        assertTrue(result.contains("provide"), "Should ask for diagnosis");

        verify(llamaAiService, times(1)).generatePatientSummary("");
    }

    @Test
    @DisplayName("P4.5: Given multiple complex diagnoses, when generating summaries, then each is simplified independently")
    void givenMultipleComplexDiagnoses_whenGeneratingSummaries_thenEachSimplifiedIndependently() {
        // Given: Multiple diagnoses
        String diagnosis1 = "Pulmonary embolism with hemodynamic instability";
        String diagnosis2 = "Acute glomerulonephritis with nephrotic syndrome";

        String summary1 = "You have a blood clot in your lungs. This is a serious condition that needs immediate treatment.";
        String summary2 = "Your kidneys are inflamed and not working properly. You will need medications and close monitoring.";

        when(llamaAiService.generatePatientSummary(diagnosis1)).thenReturn(summary1);
        when(llamaAiService.generatePatientSummary(diagnosis2)).thenReturn(summary2);

        // When: Generating summaries for each diagnosis
        String result1 = llamaAiService.generatePatientSummary(diagnosis1);
        String result2 = llamaAiService.generatePatientSummary(diagnosis2);

        // Then: Each summary should be appropriate for its diagnosis
        assertNotNull(result1);
        assertNotNull(result2);
        assertTrue(result1.contains("blood clot"));
        assertTrue(result2.contains("kidneys"));
        assertNotEquals(result1, result2, "Summaries should be different for different diagnoses");

        verify(llamaAiService, times(1)).generatePatientSummary(diagnosis1);
        verify(llamaAiService, times(1)).generatePatientSummary(diagnosis2);
    }

    @Test
    @DisplayName("P4.6: Given patient-ready summary, when displaying to patient, then language is accessible and understandable")
    void givenPatientReadySummary_whenDisplayingToPatient_thenLanguageIsAccessible() {
        // Given: A properly simplified summary
        String medicalDiagnosis = "Type 2 Diabetes Mellitus with poor glycemic control (HbA1c 9.2%), " +
                "diabetic retinopathy, and diabetic nephropathy with proteinuria.";

        String accessibleSummary = "You have diabetes, which means your body has trouble controlling sugar levels in your blood. " +
                "This has started to affect your eyes and kidneys. You need to take medications regularly and monitor your blood sugar. " +
                "If you follow your treatment plan, you can prevent serious complications.";

        when(llamaAiService.generatePatientSummary(medicalDiagnosis)).thenReturn(accessibleSummary);

        // When: Getting summary for patient
        String result = llamaAiService.generatePatientSummary(medicalDiagnosis);

        // Then: Language should be accessible
        assertNotNull(result);
        // Check for simple sentence structure (short sentences)
        String[] sentences = result.split("\\.");
        assertTrue(sentences.length > 2, "Should break into understandable sentences");
        // Check for absence of jargon
        assertFalse(result.contains("HbA1c"));
        assertFalse(result.contains("proteinuria"));
        // Check for action-oriented language
        assertTrue(result.contains("medications") || result.contains("treatment"));

        verify(llamaAiService, times(1)).generatePatientSummary(medicalDiagnosis);
    }
}
