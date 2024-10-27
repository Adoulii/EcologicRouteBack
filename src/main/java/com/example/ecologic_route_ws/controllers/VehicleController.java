    package com.example.ecologic_route_ws.controllers;

    import com.example.ecologic_route_ws.Models.Vehicle;
    import com.example.ecologic_route_ws.OntologyService;
    import org.apache.jena.ontology.Individual;
    import org.apache.jena.ontology.OntClass;
    import org.apache.jena.ontology.OntModel;
    import org.apache.jena.query.QueryExecution;
    import org.apache.jena.query.QueryExecutionFactory;
    import org.apache.jena.query.ResultSet;
    import org.apache.jena.query.ResultSetFormatter;
    import org.apache.jena.rdf.model.Property;
    import org.json.JSONObject;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.*;

    import java.io.ByteArrayOutputStream;

    @RestController
    @RequestMapping("/ontology/vehicle")
    public class VehicleController {

        @Autowired
        private OntologyService ontologyService;

        @PostMapping("/creates")
        public String createVehicles(@RequestBody Vehicle vehicle) {
            OntModel model = ontologyService.getModel();
            String namespace = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";

            // Retrieve the Vehicle subclass from the ontology (e.g., Scooter, ElectricVehicle)
            OntClass vehicleClass = model.getOntClass(namespace + vehicle.getSubClass());

            if (vehicleClass == null) {
                return "{\"error\": \"Vehicle subclass not found in ontology.\"}";
            }

            // Create a new vehicle individual of the correct subclass
            Individual newVehicle = vehicleClass.createIndividual(namespace + vehicle.getVehicleType());

            // Add data properties
            Property vehicleTypeProp = model.getProperty(namespace + "Type");
            Property electricProp = model.getProperty(namespace + "Electric");
            Property maxSpeedProp = model.getProperty(namespace + "MaxSpeed");
            Property energyConsumptionProp = model.getProperty(namespace + "EnergyConsumption");
            Property co2EmissionRateProp = model.getProperty(namespace + "CO2EmissionRate");
            Property publicTransportProp = model.getProperty(namespace + "PublicTransport");

            if (vehicleTypeProp != null) newVehicle.addProperty(vehicleTypeProp, vehicle.getVehicleType());
            if (electricProp != null) newVehicle.addLiteral(electricProp, vehicle.isElectric());
            if (maxSpeedProp != null) newVehicle.addLiteral(maxSpeedProp, vehicle.getMaxSpeed());
            if (energyConsumptionProp != null) newVehicle.addLiteral(energyConsumptionProp, vehicle.getEnergyConsumption());
            if (co2EmissionRateProp != null) newVehicle.addLiteral(co2EmissionRateProp, vehicle.getCo2EmissionRate());
            if (publicTransportProp != null) newVehicle.addLiteral(publicTransportProp, vehicle.isPublicTransport());

            // Save the updated ontology model
            ontologyService.saveModel();

            return "{\"message\": \"Vehicle created successfully.\"}";
        }


        // Create a new vehicle
        @PostMapping("/create")
        public String createVehicle(@RequestBody Vehicle vehicle) {
            OntModel model = ontologyService.getModel();
            String namespace = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";

            // Retrieve the Vehicle class from the ontology
            OntClass vehicleClass = model.getOntClass(namespace + "Vehicle");

            if (vehicleClass == null) {
                return "{\"error\": \"Vehicle class not found in ontology.\"}";
            }

            // Create a new vehicle individual
            Individual newVehicle = vehicleClass.createIndividual(namespace + vehicle.getVehicleType());

            // Add data properties
            Property vehicleTypeProp = model.getProperty(namespace + "Type");
            Property electricProp = model.getProperty(namespace + "Electric");
            Property maxSpeedProp = model.getProperty(namespace + "MaxSpeed");
            Property energyConsumptionProp = model.getProperty(namespace + "EnergyConsumption");
            Property co2EmissionRateProp = model.getProperty(namespace + "CO2EmissionRate");
            Property publicTransportProp = model.getProperty(namespace + "PublicTransport");

            if (vehicleTypeProp != null) newVehicle.addProperty(vehicleTypeProp, vehicle.getVehicleType());
            if (electricProp != null) newVehicle.addLiteral(electricProp, vehicle.isElectric());
            if (maxSpeedProp != null) newVehicle.addLiteral(maxSpeedProp, vehicle.getMaxSpeed());
            if (energyConsumptionProp != null) newVehicle.addLiteral(energyConsumptionProp, vehicle.getEnergyConsumption());
            if (co2EmissionRateProp != null) newVehicle.addLiteral(co2EmissionRateProp, vehicle.getCo2EmissionRate());
            if (publicTransportProp != null) newVehicle.addLiteral(publicTransportProp, vehicle.isPublicTransport());

            // Save the updated ontology model
            ontologyService.saveModel();

            return "{\"message\": \"Vehicle created successfully.\"}";
        }

        // Get all vehicles
        @GetMapping("/all")
        public String getAllVehicles() {
            OntModel model = ontologyService.getModel();

            String queryString = """
            PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
            SELECT ?vehicle ?vehicleType ?isElectric ?maxSpeed ?energyConsumption ?co2EmissionRate ?publicTransport
            WHERE { 
                ?vehicle rdf:type/rdfs:subClassOf* ont:Vehicle; 
                         ont:Type ?vehicleType; 
                         ont:Electric ?isElectric;
                         ont:MaxSpeed ?maxSpeed;
                         ont:EnergyConsumption ?energyConsumption;
                         ont:CO2EmissionRate ?co2EmissionRate;
                         ont:PublicTransport ?publicTransport.
            }
        """;

            QueryExecution qe = QueryExecutionFactory.create(queryString, model);
            ResultSet results = qe.execSelect();

            // Convert to JSON
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            return new String(outputStream.toByteArray());
        }

        /*@GetMapping("/all")
        public String getAllVehicles() {
            OntModel model = ontologyService.getModel();

            String queryString = """
            PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#>
            PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
            SELECT ?vehicle ?vehicleType ?isElectric ?maxSpeed ?energyConsumption ?co2EmissionRate ?publicTransport
            WHERE {
              ?vehicle rdf:type ont:Vehicle;
                       ont:Type ?vehicleType;
                       ont:Electric ?isElectric;
                       ont:MaxSpeed ?maxSpeed;
                       ont:EnergyConsumption ?energyConsumption;
                       ont:CO2EmissionRate ?co2EmissionRate;
                       ont:PublicTransport ?publicTransport.
            }
        """;

            QueryExecution qe = QueryExecutionFactory.create(queryString, model);
            ResultSet results = qe.execSelect();

            // Convert to JSON
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            return new String(outputStream.toByteArray());
        }*/
        @DeleteMapping("/delete/{id}")
        public ResponseEntity<String> deleteVehicle(@PathVariable("id") String vehicleId) {
            OntModel model = ontologyService.getModel();
            String namespace = "http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#";
            String vehicleURI = namespace + vehicleId;

            // Debugging: print the vehicle URI to check if it is correct
            System.out.println("Deleting Vehicle with URI: " + vehicleURI);

            Individual vehicleIndividual = model.getIndividual(vehicleURI);

            if (vehicleIndividual != null) {
                vehicleIndividual.remove();
                ontologyService.saveModel();
                return ResponseEntity.ok("Vehicle deleted successfully.");
            } else {
                System.out.println("Vehicle not found with URI: " + vehicleURI); // Debugging: Vehicle not found
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Vehicle not found.");
            }
        }
        

        @GetMapping("/search")
        public String searchVehicles(
                @RequestParam(required = false) String vehicleType,
                @RequestParam(required = false) String minSpeed,
                @RequestParam(required = false) String maxSpeed,
                @RequestParam(required = false) Boolean isElectric) {

            // Retrieve the ontology model from the service
            OntModel model = ontologyService.getModel();

            StringBuilder queryString = new StringBuilder(
                    "PREFIX ont: <http://www.semanticweb.org/imenfrigui/ontologies/2024/8/PlanificateurTrajetsEcologiques#> " +
                            "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
                            "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
                            "SELECT ?vehicle ?vehicleType ?isElectric ?maxSpeed ?energyConsumption ?co2EmissionRate " +
                            "WHERE { ?vehicle rdf:type ont:Vehicle; " +
                            "              ont:Type ?vehicleType; " +
                            "              ont:Electric ?isElectric; " +
                            "              ont:MaxSpeed ?maxSpeed. "
            );

            // Apply filters based on parameters
            if (vehicleType != null && !vehicleType.isEmpty()) {
                queryString.append("FILTER(?vehicleType = \"").append(vehicleType).append("\") ");
            }

            if (minSpeed != null) {
                queryString.append("FILTER(xsd:double(?maxSpeed) >= ").append(minSpeed).append(") ");
            }

            if (maxSpeed != null) {
                queryString.append("FILTER(xsd:double(?maxSpeed) <= ").append(maxSpeed).append(") ");
            }

            if (isElectric != null) {
                queryString.append("FILTER(?isElectric = \"").append(isElectric).append("\") ");
            }

            queryString.append("}");

            // Execute the query
            QueryExecution qe = QueryExecutionFactory.create(queryString.toString(), model);
            ResultSet results = qe.execSelect();

            // Convert the results to JSON
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ResultSetFormatter.outputAsJSON(outputStream, results);
            String json = new String(outputStream.toByteArray());

            // Parse and return results
            JSONObject j = new JSONObject(json);
            return j.getJSONObject("results").getJSONArray("bindings").toString();
        }









    }
