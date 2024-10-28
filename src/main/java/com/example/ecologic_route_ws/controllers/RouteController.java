package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.ChargingStation;
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

    @GetMapping("/with-relationships")
    public ResponseEntity<String> getRoutesWithRelationships() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?route ?co2EmissionValue ?distanceValue ?durationValue ?routeType " +
                "?speedValue ?fastSpeed ?mediumSpeed ?slowSpeed ?chargingSpeed ?fastCharging ?stationType " +
                "WHERE { " +
                "  ?route rdf:type ont:Route; " +
                "         ont:CO2EmissionValue ?co2EmissionValue; " +
                "         ont:DistanceValue ?distanceValue; " +
                "         ont:DurationValue ?durationValue; " +
                "         ont:RouteType ?routeType. " +

                // Speed relationship
                "  OPTIONAL { ?route ont:hasSpeed ?speed. " +
                "             ?speed ont:speedValue ?speedValue; " +
                "                    ont:fastSpeed ?fastSpeed; " +
                "                    ont:mediumSpeed ?mediumSpeed; " +
                "                    ont:slowSpeed ?slowSpeed. } " +

                // ChargingStation relationship
                "  OPTIONAL { ?route ont:hasChargingStation ?chargingStation. " +
                "             ?chargingStation ont:chargingSpeed ?chargingSpeed; " +
                "                             ont:fastCharging ?fastCharging; " +
                "                             ont:stationType ?stationType. } " +
                "} ORDER BY DESC(?route) LIMIT 10";

        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();

            if (!results.hasNext()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No routes with relationships found.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());

            return ResponseEntity.ok(new JSONObject(json).getJSONObject("results").getJSONArray("bindings").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while fetching routes with relationships.");
        }
    }

    @PostMapping("/with-relationships")
    public ResponseEntity<String> createRouteWithRelationships(@RequestBody Route route) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        // Generate a unique ID for the route
        String routeId = "Route_" + UUID.randomUUID();
        Individual routeIndividual = ontModel.createIndividual(NAMESPACE + routeId, ontModel.getOntClass(NAMESPACE + "Route"));

        // Set the basic properties of the route
        var co2EmissionProperty = ontModel.getDatatypeProperty(NAMESPACE + "CO2EmissionValue");
        var distanceProperty = ontModel.getDatatypeProperty(NAMESPACE + "DistanceValue");
        var durationProperty = ontModel.getDatatypeProperty(NAMESPACE + "DurationValue");

        if (co2EmissionProperty != null) {
            routeIndividual.addProperty(co2EmissionProperty, String.valueOf(route.getCo2EmissionValue()));
        } else {
            System.err.println("Property CO2EmissionValue is missing in the ontology.");
        }

        if (distanceProperty != null) {
            routeIndividual.addProperty(distanceProperty, String.valueOf(route.getDistanceValue()));
        } else {
            System.err.println("Property DistanceValue is missing in the ontology.");
        }

        if (durationProperty != null) {
            routeIndividual.addProperty(durationProperty, String.valueOf(route.getDurationValue()));
        } else {
            System.err.println("Property DurationValue is missing in the ontology.");
        }

        // Handle RouteType if present
        if (route.getRouteType() != null) {
            var routeTypeProperty = ontModel.getDatatypeProperty(NAMESPACE + "RouteType");
            if (routeTypeProperty != null) {
                routeIndividual.addProperty(routeTypeProperty, route.getRouteType().name());
            } else {
                System.err.println("Property RouteType is missing in the ontology.");
            }
        }

        // Handle Speed relationship if present
        if (route.getSpeed() != null) {
            var speedClass = ontModel.getOntClass(NAMESPACE + "Speed");
            var speedProperty = ontModel.getObjectProperty(NAMESPACE + "hasSpeed");
            if (speedClass != null && speedProperty != null) {
                Individual speedIndividual = ontModel.createIndividual(NAMESPACE + "Speed_" + UUID.randomUUID(), speedClass);

                // Set properties for Speed
                var speedValueProperty = ontModel.getDatatypeProperty(NAMESPACE + "speedValue");
                var fastSpeedProperty = ontModel.getDatatypeProperty(NAMESPACE + "fastSpeed");
                var mediumSpeedProperty = ontModel.getDatatypeProperty(NAMESPACE + "mediumSpeed");
                var slowSpeedProperty = ontModel.getDatatypeProperty(NAMESPACE + "slowSpeed");

                if (speedValueProperty != null) {
                    speedIndividual.addProperty(speedValueProperty, String.valueOf(route.getSpeed().getSpeedValue()));
                } else {
                    System.err.println("Property speedValue is missing in the ontology.");
                }

                if (fastSpeedProperty != null) {
                    speedIndividual.addProperty(fastSpeedProperty, String.valueOf(route.getSpeed().isFastSpeed()));
                } else {
                    System.err.println("Property fastSpeed is missing in the ontology.");
                }

                if (mediumSpeedProperty != null) {
                    speedIndividual.addProperty(mediumSpeedProperty, String.valueOf(route.getSpeed().isMediumSpeed()));
                } else {
                    System.err.println("Property mediumSpeed is missing in the ontology.");
                }

                if (slowSpeedProperty != null) {
                    speedIndividual.addProperty(slowSpeedProperty, String.valueOf(route.getSpeed().isSlowSpeed()));
                } else {
                    System.err.println("Property slowSpeed is missing in the ontology.");
                }

                // Link the speed individual to the route
                routeIndividual.addProperty(speedProperty, speedIndividual);
            } else {
                System.err.println("Class Speed or property hasSpeed is missing in the ontology.");
            }
        }

        // Handle ChargingStation relationships
        if (route.getChargingStations() != null) {
            var chargingStationClass = ontModel.getOntClass(NAMESPACE + "ChargingStation");
            var chargingStationProperty = ontModel.getObjectProperty(NAMESPACE + "hasChargingStation");
            if (chargingStationClass != null && chargingStationProperty != null) {
                for (ChargingStation chargingStation : route.getChargingStations()) {
                    Individual stationIndividual = ontModel.createIndividual(NAMESPACE + "ChargingStation_" + UUID.randomUUID(), chargingStationClass);

                    // Set properties for ChargingStation
                    var chargingSpeedProperty = ontModel.getDatatypeProperty(NAMESPACE + "chargingSpeed");
                    var fastChargingProperty = ontModel.getDatatypeProperty(NAMESPACE + "fastCharging");
                    var stationTypeProperty = ontModel.getDatatypeProperty(NAMESPACE + "stationType");

                    if (chargingSpeedProperty != null) {
                        stationIndividual.addProperty(chargingSpeedProperty, String.valueOf(chargingStation.getChargingSpeed()));
                    } else {
                        System.err.println("Property chargingSpeed is missing in the ontology.");
                    }

                    if (fastChargingProperty != null) {
                        stationIndividual.addProperty(fastChargingProperty, String.valueOf(chargingStation.isFastCharging()));
                    } else {
                        System.err.println("Property fastCharging is missing in the ontology.");
                    }

                    if (stationTypeProperty != null) {
                        stationIndividual.addProperty(stationTypeProperty, chargingStation.getStationType());
                    } else {
                        System.err.println("Property stationType is missing in the ontology.");
                    }

                    // Link the charging station to the route
                    routeIndividual.addProperty(chargingStationProperty, stationIndividual);
                }
            } else {
                System.err.println("Class ChargingStation or property hasChargingStation is missing in the ontology.");
            }
        }

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the route with relationships.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Route with relationships added successfully.");
    }
}
