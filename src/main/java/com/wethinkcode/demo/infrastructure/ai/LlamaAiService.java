package com.wethinkcode.demo.infrastructure.ai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlamaAiService {
    
    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Generates a structured SOAP note from rough consultation notes
     */
    public String generateSoapNote(String roughNotes) {
        String prompt = """
            You are a medical documentation expert. Convert the following rough clinical notes into a structured SOAP note format.
            Return the response as valid JSON with the following structure:
            {
                "subjective": "Patient's chief complaint and history",
                "objective": "Vital signs and examination findings",
                "assessment": "Clinical diagnosis and assessment",
                "plan": "Treatment plan and recommendations"
            }
            
            Rough notes:
            %s
            
            Return ONLY valid JSON, no additional text.
            """.formatted(roughNotes);
        
        return callOllamaApi(prompt);
    }
    
    /**
     * Generates a simplified patient-friendly summary from a SOAP note
     */
    public String generatePatientSummary(String soapNote) {
        String prompt = """
            You are a medical translator. Convert the following medical SOAP note into a simple, patient-friendly summary.
            Use simple 5th-grade level English that a patient can understand.
            Avoid medical jargon. Explain in simple terms what the doctor found and what the patient should do next.
            Keep it to 2-3 paragraphs maximum.
            
            SOAP Note:
            %s
            
            Patient Summary:
            """.formatted(soapNote);
        
        return callOllamaApi(prompt);
    }
    
    /**
     * Extracts prescription details from a SOAP note
     */
    public String extractPrescription(String soapNote) {
        String prompt = """
            Extract any prescription medications from the following medical note.
            Format as a simple list with medication name, dosage, and frequency.
            If no medications are mentioned, return "No prescriptions recommended".
            
            Medical Note:
            %s
            
            Prescription List:
            """.formatted(soapNote);
        
        return callOllamaApi(prompt);
    }
    
    /**
     * Call Ollama API with the given prompt
     */
    private String callOllamaApi(String prompt) {
        try {
            String url = ollamaBaseUrl + "/api/generate";
            
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", "llama2");
            requestBody.put("prompt", prompt);
            requestBody.put("stream", false);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            String response = restTemplate.postForObject(url, request, String.class);
            
            // Parse the JSON response to extract the text
            if (response != null) {
                JsonNode jsonNode = objectMapper.readTree(response);
                if (jsonNode.has("response")) {
                    return jsonNode.get("response").asText();
                }
            }
            
            return response != null ? response : "Unable to generate response";
            
        } catch (Exception e) {
            log.error("Error calling Ollama API", e);
            return "Error: " + e.getMessage();
        }
    }
}

