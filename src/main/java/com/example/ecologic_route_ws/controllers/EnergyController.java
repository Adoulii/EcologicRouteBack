package com.example.ecologic_route_ws.controllers;

import com.example.ecologic_route_ws.Models.Energy;
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
@RequestMapping(path = "/energy", produces = "application/json")
@CrossOrigin(origins = "http://localhost:3000/")
public class EnergyController {
    private final Model model;
    private final String NAMESPACE = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
    private final String RDF_FILE = "data/sementique_finale.rdf";

    public EnergyController() {
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
     * Get all energy records from the RDF model.
     * @return A ResponseEntity containing a JSON array of energy records.
     */
    @GetMapping
    public String getEnergies() {
        String queryString = "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                "SELECT ?energy ?consumptionRate ?type ?renewable " +
                "WHERE { " +
                "  ?energy rdf:type ont:Energy; " +
                "          ont:ConsumptionRate ?consumptionRate; " +
                "          ont:Type ?type; " +
                           "ont:Renewable ?renewable"      +       
                "}";
    
        QueryExecution qe = QueryExecutionFactory.create(queryString, model);
        ResultSet results = qe.execSelect();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ResultSetFormatter.outputAsJSON(outputStream, results);
        String json = new String(outputStream.toByteArray());
    
        JSONObject j = new JSONObject(json);
        return j.getJSONObject("results").getJSONArray("bindings").toString();
    }
    

   
    @PostMapping("/energy")
    public ResponseEntity<String> addEnergy(@RequestBody Energy energy) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
    
        // Use the provided ID for the energy resource
        String energyURI = NAMESPACE + "Energy_" + energy.getId();
        
        // Check if an energy resource with the same ID already exists
        Individual existingEnergy = ontModel.getIndividual(energyURI);
        if (existingEnergy != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Energy resource with this ID already exists.");
        }
    
        // Create a new energy individual
        Individual energyIndividual = ontModel.createIndividual(energyURI, ontModel.getOntClass(NAMESPACE + "Energy"));
    
        // Adding properties based on the energy type
        energyIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "ConsumptionRate"),
                String.valueOf(energy.getConsumptionRate()));
        energyIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "Renewable"),
                String.valueOf(energy.isRenewable()));
        energyIndividual.addProperty(ontModel.getDatatypeProperty(NAMESPACE + "Type"),
                energy.getType());
    
        try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
            ontModel.write(outputStream, "RDF/XML-ABBREV");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add the energy.");
        }
    
        return ResponseEntity.status(HttpStatus.CREATED).body("Energy added successfully.");
    }
    
    @DeleteMapping("/deleteById")
    public ResponseEntity<String> deleteEnergyById(@RequestParam("id") String energyId) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);
    
        // Construct the URI using the provided ID
        String energyURI = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#Energy" + energyId;
    
        // Check if the individual exists
        System.out.println("Checking for individual with ID: " + energyId);
        Individual energyIndividual = ontModel.getIndividual(energyURI);
    
        if (energyIndividual != null) {
            System.out.println("Found individual: " + energyIndividual.getURI());
            energyIndividual.remove();
    
            try (OutputStream outputStream = new FileOutputStream(RDF_FILE)) {
                ontModel.write(outputStream, "RDF/XML-ABBREV");
            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete the energy resource.");
            }
    
            return ResponseEntity.status(HttpStatus.OK).body("Energy resource deleted successfully.");
        } else {
            System.out.println("Energy resource not found for ID: " + energyId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Energy resource not found.");
        }
    }
    @GetMapping("/energy/{id}")
public ResponseEntity<Energy> getEnergyById(@PathVariable int id) {
    OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF, model);

    // Construct the URI based on the provided ID
    String energyURI = NAMESPACE + "Energy_" + id;
    
    // Retrieve the individual by URI
    Individual energyIndividual = ontModel.getIndividual(energyURI);

    if (energyIndividual != null) {
        // Create an Energy object to return
        Energy energy = new Energy(id, 
                energyIndividual.getPropertyValue(ontModel.getDatatypeProperty(NAMESPACE + "ConsumptionRate")).asLiteral().getFloat(),
                energyIndividual.getPropertyValue(ontModel.getDatatypeProperty(NAMESPACE + "Renewable")).asLiteral().getBoolean(),
                energyIndividual.getPropertyValue(ontModel.getDatatypeProperty(NAMESPACE + "Type")).asLiteral().getString());
        
        return ResponseEntity.ok(energy);
    } else {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); // Return 404 if not found
    }
}

    
    
}
