package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.Speed;
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
@RequestMapping(path = "speed", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class SpeedController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public SpeedController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    @GetMapping
    public String getSpeeds() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?speed ?speedValue ?fastSpeed ?mediumSpeed ?slowSpeed " +
                "WHERE { ?speed rdf:type ont:Speed; " +
                "              ont:SpeedValue ?speedValue; " +
                "              ont:FastSpeed ?fastSpeed; " +
                "              ont:MediumSpeed ?mediumSpeed; " +
                "              ont:SlowSpeed ?slowSpeed. }";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @PostMapping
    public ResponseEntity<String> addSpeed(@RequestBody Speed speed) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual speedIndividual = ontModel.createIndividual(NAMESPACE + "Speed_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "Speed"));

        speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "SpeedValue"),
                String.valueOf(speed.getSpeedValue()));
        speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "FastSpeed"),
                String.valueOf(speed.isFastSpeed()));
        speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "MediumSpeed"),
                String.valueOf(speed.isMediumSpeed()));
        speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "SlowSpeed"),
                String.valueOf(speed.isSlowSpeed()));

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the speed.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Speed added successfully.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSpeed(@PathVariable("id") String speedId) {
        // Construct the full URI using the namespace and the provided ID
        String speedURI = NAMESPACE + speedId;

        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual speedIndividual = ontModel.getIndividual(speedURI);

        if (speedIndividual != null) {
            speedIndividual.remove();

            // Write the updated model back to the RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the speed.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Speed deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Speed not found.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getSpeedById(@PathVariable("id") String speedId) {
        String speedURI = NAMESPACE + speedId;
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?speedValue ?fastSpeed ?mediumSpeed ?slowSpeed " +
                "WHERE { <" + speedURI + "> rdf:type ont:Speed; " +
                "              ont:SpeedValue ?speedValue; " +
                "              ont:FastSpeed ?fastSpeed; " +
                "              ont:MediumSpeed ?mediumSpeed; " +
                "              ont:SlowSpeed ?slowSpeed. }";

        try (QueryExecution qe = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qe.execSelect();

            if (!results.hasNext()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Speed not found.");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());

            JSONObject j = new JSONObject(json);
            return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing the request.");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateSpeed(@PathVariable("id") String speedId, @RequestBody Speed updatedSpeed) {
        String speedURI = NAMESPACE + speedId;
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
        Individual speedIndividual = ontModel.getIndividual(speedURI);

        if (speedIndividual != null) {
            // Update properties
            speedIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "SpeedValue"));
            speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "SpeedValue"), String.valueOf(updatedSpeed.getSpeedValue()));

            speedIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "FastSpeed"));
            speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "FastSpeed"), String.valueOf(updatedSpeed.isFastSpeed()));

            speedIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "MediumSpeed"));
            speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "MediumSpeed"), String.valueOf(updatedSpeed.isMediumSpeed()));

            speedIndividual.removeAll(ontModel.getDatatypeProperty(NAMESPACE + "SlowSpeed"));
            speedIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "SlowSpeed"), String.valueOf(updatedSpeed.isSlowSpeed()));

            // Save changes to RDF file
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update the speed.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Speed updated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Speed not found.");
        }
    }
}
