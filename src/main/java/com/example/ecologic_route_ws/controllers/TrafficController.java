package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.TrafficCondition;
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
@RequestMapping(path = "/trafficCondition", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class TrafficController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public TrafficController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    /**
     * Load the RDF model from the specified RDF file.
     */
    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    /**
     * Get all traffic condition records from the RDF model.
     * @return A ResponseEntity containing a JSON array of traffic condition records.
     */
    @GetMapping
    public String getTrafficConditions() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?trafficCondition ?trafficLevel ?averageDelay " +
                "WHERE { " +
                "  ?trafficCondition rdf:type ont:TrafficCondition; " +
                "                    ont:TrafficLevel ?trafficLevel; " +
                "                    ont:AverageDelay ?averageDelay " +
                "}";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @PostMapping
    public ResponseEntity<String> addTrafficCondition(@RequestBody TrafficCondition trafficCondition) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Use the provided ID for the traffic condition resource
        String trafficConditionURI = NAMESPACE + "TrafficCondition_" + trafficCondition.getId();

        // Check if a traffic condition resource with the same ID already exists
        Individual existingTrafficCondition = ontModel.getIndividual(trafficConditionURI);
        if (existingTrafficCondition != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Traffic condition resource with this ID already exists.");
        }

        // Create a new traffic condition individual
        Individual trafficConditionIndividual = ontModel.createIndividual(trafficConditionURI, ontModel.getOntClass(NAMESPACE + "TrafficCondition"));

        // Adding properties based on the traffic condition object
        trafficConditionIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "TrafficLevel"),
                trafficCondition.getTrafficLevel());
        trafficConditionIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "AverageDelay"),
                String.valueOf(trafficCondition.getAverageDelay()));

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the traffic condition.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Traffic condition added successfully.");
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateTrafficCondition(@PathVariable int id, @RequestBody TrafficCondition trafficCondition) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Use the provided ID for the traffic condition resource
        String trafficConditionURI = NAMESPACE + "TrafficCondition_" + id;

        // Check if a traffic condition resource with the given ID exists
        Individual existingTrafficCondition = ontModel.getIndividual(trafficConditionURI);
        if (existingTrafficCondition == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Traffic condition resource with this ID does not exist.");
        }

        // Update properties based on the traffic condition object
        existingTrafficCondition.removeProperties(); // Optional: remove existing properties
        existingTrafficCondition.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "TrafficLevel"),
                trafficCondition.getTrafficLevel().toString());
        existingTrafficCondition.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "AverageDelay"),
                String.valueOf(trafficCondition.getAverageDelay()));

        // Save changes to the RDF file
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the traffic condition.");
        }

        return ResponseEntity.ok("Traffic condition updated successfully.");
    }



    @GetMapping("/{id}")
    public ResponseEntity<TrafficCondition> getTrafficConditionById(@PathVariable int id) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Construct the URI based on the provided ID
        String trafficConditionURI = NAMESPACE + "TrafficCondition_" + id;

        // Retrieve the individual by URI
        Individual trafficConditionIndividual = ontModel.getIndividual(trafficConditionURI);

        if (trafficConditionIndividual != null) {
            // Create a TrafficCondition object to return
            TrafficCondition trafficCondition = new TrafficCondition(id,
                    trafficConditionIndividual.getPropertyValue(ontModel.getDatatypeProperty(NAMESPACE + "TrafficLevel")).asLiteral().getString(),
                    trafficConditionIndividual.getPropertyValue(ontModel.getDatatypeProperty(NAMESPACE + "AverageDelay")).asLiteral().getInt());

            return ResponseEntity.ok(trafficCondition);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if not found
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTrafficConditionById(@PathVariable("id") int trafficConditionId) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Construct the URI using the provided ID
        String trafficConditionURI = NAMESPACE + "TrafficCondition_" + trafficConditionId;

        // Check if the individual exists
        Individual trafficConditionIndividual = ontModel.getIndividual(trafficConditionURI);

        if (trafficConditionIndividual != null) {
            trafficConditionIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the traffic condition resource.");
            }

            return ResponseEntity.ok("Traffic condition resource deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Traffic condition resource not found.");
        }
    }


}