package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.DurationDTO;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.util.FileManager;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(path = "duration", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class DurationController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public DurationController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping
    public String getDurations() {
        String queryString = "PREFIX ont: <" + NAMESPACE + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?duration ?exactDuration ?longDuration ?mediumDuration ?shortDuration " +
                "WHERE { ?duration rdf:type ont:Duration; " +
                "                ont:ExactDuration ?exactDuration; " +   // Use capitalized property names
                "                ont:LongDuration ?longDuration; " +     // Update here
                "                ont:MediumDuration ?mediumDuration; " + // Update here
                "                ont:ShortDuration ?shortDuration. }";   // Update here

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }


    @PostMapping
    public ResponseEntity<String> addDuration(@RequestBody DurationDTO durationDTO) {
        // Log the received DTO to verify values
        System.out.println("Received DurationDTO: ");
        System.out.println("Exact Duration: " + durationDTO.getExactDuration());
        System.out.println("Long Duration: " + durationDTO.isLongDuration());
        System.out.println("Medium Duration: " + durationDTO.isMediumDuration());
        System.out.println("Short Duration: " + durationDTO.isShortDuration());

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual durationIndividual = ontModel.createIndividual(NAMESPACE + "Duration_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Duration"));

        try {
            // Add properties
            durationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ExactDuration"),
                    String.valueOf(durationDTO.getExactDuration()));
            durationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "LongDuration"),
                    String.valueOf(durationDTO.isLongDuration()));
            durationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "MediumDuration"),
                    String.valueOf(durationDTO.isMediumDuration()));
            durationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ShortDuration"),
                    String.valueOf(durationDTO.isShortDuration()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error setting properties: " + e.getMessage());
        }

        // Save the ontology model
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to save the model: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Duration added successfully.");
    }


    @GetMapping("/{id}")
    public ResponseEntity<String> getDurationById(@PathVariable("id") String durationId) {
        String durationURI = NAMESPACE + "Duration_" + durationId;
        String queryString = "PREFIX ont: <" + NAMESPACE + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?exactDuration ?longDuration ?mediumDuration ?shortDuration " +
                "WHERE { <" + durationURI + "> rdf:type ont:Duration; " +
                "                   ont:exactDuration ?exactDuration; " +
                "                   ont:longDuration ?longDuration; " +
                "                   ont:mediumDuration ?mediumDuration; " +
                "                   ont:shortDuration ?shortDuration. }";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();

        if (!results.hasNext()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Duration not found.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDuration(@PathVariable("id") String durationId) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual durationToDelete = ontModel.getIndividual(NAMESPACE + "Duration_" + durationId);

        if (durationToDelete != null) {
            durationToDelete.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the duration.");
            }

            return ResponseEntity.ok("Duration deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Duration not found.");
        }
    }
}
