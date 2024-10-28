package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.Route;
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
import java.util.UUID;

@RestController
@RequestMapping(path = "route", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class RouteController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public RouteController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }
    private void loadModel() {
        try {
            FileManager.get().readModel(model, RDF_FILE);
            System.out.println("Ontology model loaded successfully.");
        } catch (Exception e) {
            System.err.println("Failed to load ontology model from file: " + RDF_FILE);
            e.printStackTrace();
        }
    }
    // GET all Routes
    @GetMapping
    public ResponseEntity<String> getRoutes() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?route ?name ?co2EmissionValue ?distanceValue ?durationValue " +
                "WHERE { ?route rdf:type ont:Route; " +
                "              ont:CO2EmissionValue ?co2EmissionValue; " +
                "              ont:DistanceValue ?distanceValue; " +
                "              ont:DurationValue ?durationValue. }";


        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();
            if (!results.hasNext()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No routes found.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());
            return ResponseEntity.ok(new JSONObject(json).getJSONObject("results").getJSONArray("bindings").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching routes.");
        }
    }

    // GET Route by ID
    @GetMapping("/{id}")
    public ResponseEntity<String> getRouteById(@PathVariable("id") String routeId) {
        // Define the route URI based on the provided ID
        String routeURI = NAMESPACE + routeId;

        // Define the SPARQL query to retrieve specific route details by ID
        String queryString =
                "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                        "SELECT ?name ?co2EmissionValue ?distanceValue ?durationValue " +
                        "WHERE { <" + routeURI + "> rdf:type ont:Route; " +
                        "              ont:CO2EmissionValue ?co2EmissionValue; " +
                        "              ont:DistanceValue ?distanceValue; " +
                        "              ont:DurationValue ?durationValue. }";

        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();

            // Check if the result set is empty, meaning the route was not found
            if (!results.hasNext()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found.");
            }

            // Convert the ResultSet to JSON format
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());

            // Parse JSON to retrieve only the "bindings" array for a cleaner response
            JSONObject jsonResponse = new JSONObject(json);
            String bindings = jsonResponse.getJSONObject("results").getJSONArray("bindings").toString();

            // Return the response with status 200 OK
            return ResponseEntity.ok(bindings);
        } catch (Exception e) {
            // Log the exception and return an error response
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }
// POST to add a new Route
    @PostMapping
    public ResponseEntity<String> addRoute(@RequestBody Route route) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        String routeId = "Route_" + UUID.randomUUID();
        Individual routeIndividual = ontModel.createIndividual(NAMESPACE + routeId,
                ontModel.getOntClass(NAMESPACE + "Route"));

        // Retrieve and debug each property
        var co2EmissionProperty = ontModel.getDatatypeProperty(NAMESPACE + "CO2EmissionValue");
        var distanceProperty = ontModel.getDatatypeProperty(NAMESPACE + "DistanceValue");
        var durationProperty = ontModel.getDatatypeProperty(NAMESPACE + "DurationValue");
        var routeTypeProperty = ontModel.getDatatypeProperty(NAMESPACE + "RouteType"); // Add RouteType property

        if (co2EmissionProperty == null) {
            System.err.println("CO2EmissionValue property is missing in the ontology.");
        }
        if (distanceProperty == null) {
            System.err.println("DistanceValue property is missing in the ontology.");
        }
        if (durationProperty == null) {
            System.err.println("DurationValue property is missing in the ontology.");
        }

        // Only add non-null properties
        if (co2EmissionProperty != null) {
            routeIndividual.addProperty(co2EmissionProperty, String.valueOf(route.getCo2EmissionValue()));
        }
        if (distanceProperty != null) {
            routeIndividual.addProperty(distanceProperty, String.valueOf(route.getDistanceValue()));
        }
        if (durationProperty != null) {
            routeIndividual.addProperty(durationProperty, String.valueOf(route.getDurationValue()));
        }
        if (routeTypeProperty != null && route.getRouteType() != null) { // Ensure RouteType is provided
            routeIndividual.addProperty(routeTypeProperty, route.getRouteType().name());
        }
        // Save the model to the RDF file
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the route.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Route added successfully.");
    }
    // PUT to update an existing Route
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRoute(@PathVariable("id") String routeId, @RequestBody Route updatedRoute) {
        String routeURI = NAMESPACE + routeId;
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual routeIndividual = ontModel.getIndividual(routeURI);

        if (routeIndividual != null) {
            // Update properties

            routeIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "CO2EmissionValue"));
            routeIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "CO2EmissionValue"), String.valueOf(updatedRoute.getCo2EmissionValue()));

            routeIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "DistanceValue"));
            routeIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "DistanceValue"), String.valueOf(updatedRoute.getDistanceValue()));

            routeIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "DurationValue"));
            routeIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "DurationValue"), String.valueOf(updatedRoute.getDurationValue()));

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the route.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Route updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found.");
        }
    }

    // DELETE Route by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteRoute(@PathVariable("id") String routeId) {
        String routeURI = NAMESPACE + routeId;
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual routeIndividual = ontModel.getIndividual(routeURI);

        if (routeIndividual != null) {
            routeIndividual.remove();

            // Write the updated model back to the RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the route.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Route deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Route not found.");
        }
    }
}
