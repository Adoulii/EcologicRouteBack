package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.ChargingStation;
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
@RequestMapping(path = "charging",produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class ChargingStationController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public ChargingStationController() {
        this.model = ModelFactory.createDefaultModel();
        loadModel();
    }

    private void loadModel() {
        FileManager.get().readModel(model, RDF_FILE);
    }

    public Model getModel() {
        return model;
    }

    @GetMapping
    public String getChargingStations() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?station ?chargingSpeed ?fastCharging ?stationType " +
                "WHERE { ?station rdf:type ont:ChargingStation; " +
                "              ont:ChargingSpeed ?chargingSpeed; " +
                "              ont:FastCharging ?fastCharging; " +
                "              ont:StationType ?stationType. }";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }

    @PostMapping
    public ResponseEntity<String> addChargingStation(@RequestBody ChargingStation chargingStation) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual stationIndividual = ontModel.createIndividual(NAMESPACE + "ChargingStation_" + System.currentTimeMillis(),
                ontModel.getOntClass(NAMESPACE + "ChargingStation"));

        stationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ChargingSpeed"),
                String.valueOf(chargingStation.getChargingSpeed()));
        stationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "FastCharging"),
                String.valueOf(chargingStation.isFastCharging()));
        stationIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "StationType"),
                chargingStation.getStationType());

        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the charging station.");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body("Charging station added successfully.");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteChargingStation(@RequestParam("URI") String stationURI) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

        Individual stationIndividual = ontModel.getIndividual(stationURI);

        if (stationIndividual != null) {
            stationIndividual.remove();

            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the charging station.");
            }

            return ResponseEntity.status(HttpStatus.OK).body("Charging station deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Charging station not found.");
        }
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getChargingStationById(@PathVariable("id") String stationId) {
        // Define the query with the specific station URI
        String stationURI = NAMESPACE + stationId;
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?chargingSpeed ?fastCharging ?stationType " +
                "WHERE { <" + stationURI + "> rdf:type ont:ChargingStation; " +
                "              ont:ChargingSpeed ?chargingSpeed; " +
                "              ont:FastCharging ?fastCharging; " +
                "              ont:StationType ?stationType. }";

        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();

        if (!results.hasNext()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Charging station not found.");
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());

        JSONObject j = new JSONObject(json);
        return ResponseEntity.ok(j.getJSONObject("results").getJSONArray("bindings").toString());
    }
}
