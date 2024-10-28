package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.Distance;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import org.apache.jena.query.*;
import org.apache.jena.util.FileManager;
import org.apache.jena.ontology.*;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

@RestController
@RequestMapping(path = "/distance", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class DistanceController {
    private static final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";
    private final Model model;

    public DistanceController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    // Load RDF model from file
    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    // Get all Distances
    @GetMapping
    public String getDistances() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?distance ?exactDistance ?longDistance " +
                "WHERE { " +
                "  ?distance rdf:type ont:Distance; " +
                "            ont:ExactDistance ?exactDistance; " +
                "            ont:LongDistance ?longDistance " +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    // Get Distance by ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getDistanceById(@PathVariable String id) {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?exactDistance ?longDistance " +
                "WHERE { " +
                "  <" + NAMESPACE + "Distance" + id + "> rdf:type ont:Distance; " +
                "                           ont:ExactDistance ?exactDistance; " +
                "                           ont:LongDistance ?longDistance " +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        
        if (results.hasNext()) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());
            JSONObject j = new JSONObject(json);
            return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Distance not found");
        }
    }

    // Post new Distance
    @PostMapping()
    public ResponseEntity<String> addDistance(@RequestBody Distance distance) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        
        // Use the provided ID for the distance resource
        String distanceURI = NAMESPACE + "Distance_" + distance.getId();
        
        // Check if a distance resource with the same ID already exists
        Individual existingDistance = ontModel.getIndividual(distanceURI);
        if (existingDistance != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Distance resource with this ID already exists.");
        }
    
        // Create a new distance individual
        Individual distanceIndividual = ontModel.createIndividual(distanceURI, ontModel.getOntClass(NAMESPACE + "Distance"));
    
        // Adding properties based on the distance object
        distanceIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ExactDistance"),
                String.valueOf(distance.getExactDistance()));
        distanceIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "LongDistance"),
                String.valueOf(distance.isLongDistance()));
    
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the distance.");
        }
    
        return ResponseEntity.status(HttpStatus.CREATED).body("Distance added successfully.");
    }
    @PutMapping("/{id}")
    public ResponseEntity<String> updateDistance(@PathVariable int id, @RequestBody Distance distance) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
    
        // Use the provided ID for the distance resource
        String distanceURI = NAMESPACE + "Distance_" + id;
    
        // Check if a distance resource with the given ID exists
        Individual existingDistance = ontModel.getIndividual(distanceURI);
        if (existingDistance == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Distance resource with this ID does not exist.");
        }
    
        // Update properties based on the distance object
        // Optionally clear existing properties if needed
        existingDistance.removeProperties(); // Optional: remove existing properties
        existingDistance.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ExactDistance"),
                String.valueOf(distance.getExactDistance()));
        existingDistance.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "LongDistance"),
                String.valueOf(distance.isLongDistance()));
    
        // Save changes to the RDF file
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the distance.");
        }
    
        return ResponseEntity.ok("Distance updated successfully.");
    }
    
    

    // Delete Distance by ID
    @DeleteMapping("/deleteById")
    public ResponseEntity<String> deleteDistanceById(@RequestParam("id") String distanceId) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        
        // Construct the URI directly using the provided ID
        String distanceURI = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#Distance_" + distanceId;
        
        // Check if the individual exists
        System.out.println("Checking for individual with ID: " + distanceId);
        Individual distanceIndividual = ontModel.getIndividual(distanceURI);
        
        if (distanceIndividual != null) {
            System.out.println("Found individual: " + distanceIndividual.getURI());
            distanceIndividual.remove();
        
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the distance resource.");
            }
        
            return ResponseEntity.status(HttpStatus.OK).body("Distance resource deleted successfully.");
        } else {
            System.out.println("Distance resource not found for ID: " + distanceId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Distance resource not found.");
        }
    }
    
}
