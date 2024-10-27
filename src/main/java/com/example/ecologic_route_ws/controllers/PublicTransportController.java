package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.PublicTransportDTO;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.util.FileManager;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping(path = "public-transport", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class PublicTransportController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public PublicTransportController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping
    public ResponseEntity<String> getPublicTransport() {
        String queryString = "PREFIX ont: <" + NAMESPACE + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?transport ?lineNumber ?shortDistance ?operatesOnWeekend ?arrivalTime ?departureTime ?transportType " +
                "WHERE { ?transport rdf:type ont:PublicTransport; " +
                "                   ont:lineNumber ?lineNumber; " +
                "                   ont:shortDistance ?shortDistance; " +
                "                   ont:operatesOnWeekend ?operatesOnWeekend; " +
                "                   ont:arrivalTime ?arrivalTime; " +
                "                   ont:departureTime ?departureTime; " +
                "                   ont:transportType ?transportType. }";

        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());
            JSONObject j = new JSONObject(json);
            return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
        }
    }

    @PostMapping("/add")
    public ResponseEntity<String> addPublicTransport(@RequestBody PublicTransportDTO transportDTO) {
        try {
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
            OntClass transportClass = ontModel.getOntClass(NAMESPACE + "PublicTransport");

            if (transportClass == null) {
                return ResponseEntity.badRequest().body("{\"error\": \"PublicTransport class not found in ontology.\"}");
            }

            // Create a unique identifier for the new transport instance
            String transportTypeURI = "PublicTransport_" + System.currentTimeMillis();
            Individual newTransport = transportClass.createIndividual(NAMESPACE + transportTypeURI);

            // Add properties from DTO to the new individual
            newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "lineNumber"), transportDTO.getLineNumber());
            newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "shortDistance"), transportDTO.isShortDistance());
            newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "operatesOnWeekend"), transportDTO.isOperatesOnWeekend());

            // Adding transport type as a literal
            if (transportDTO.getTransportType() != null) {
                newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "transportType"), transportDTO.getTransportType().name());
            }

            if (transportDTO.getArrivalTime() != null) {
                newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "arrivalTime"), transportDTO.getArrivalTime().format(DATE_TIME_FORMATTER));
            }
            if (transportDTO.getDepartureTime() != null) {
                newTransport.addLiteral(ResourceFactory.createProperty(NAMESPACE + "departureTime"), transportDTO.getDepartureTime().format(DATE_TIME_FORMATTER));
            }

            // Save the updated ontology model
            return saveModel(ontModel, "Failed to save the updated public transport model.");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the public transport: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getPublicTransportById(@PathVariable("id") String transportId) {
        String transportURI = NAMESPACE + "PublicTransport_" + transportId;
        String queryString = "PREFIX ont: <" + NAMESPACE + "> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?lineNumber ?shortDistance ?operatesOnWeekend ?arrivalTime ?departureTime ?transportType " +
                "WHERE { <" + transportURI + "> rdf:type ont:PublicTransport; " +
                "                     ont:lineNumber ?lineNumber; " +
                "                     ont:shortDistance ?shortDistance; " +
                "                     ont:operatesOnWeekend ?operatesOnWeekend; " +
                "                     ont:arrivalTime ?arrivalTime; " +
                "                     ont:departureTime ?departureTime; " +
                "                     ont:transportType ?transportType. }";

        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();
            if (!results.hasNext()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Public transport not found.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());
            JSONObject j = new JSONObject(json);
            return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePublicTransport(@PathVariable("id") String transportId) {
        try {
            OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
            Individual transportToDelete = ontModel.getIndividual(NAMESPACE + "PublicTransport_" + transportId);

            if (transportToDelete != null) {
                transportToDelete.remove();
                // Save the model
                return saveModel(ontModel, "Failed to delete the public transport.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Public transport not found.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the public transport: " + e.getMessage());
        }
    }

    private ResponseEntity<String> saveModel(OntModel ontModel, String errorMessage) {
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
            return ResponseEntity.ok("Operation successful.");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }
}
