

package com.example.ecologic_route_ws.controllers;

        import com.example.ecologic_route_ws.Models.UsagePreference;
        import org.springframework.http.HttpStatus;
        import org.springframework.http.ResponseEntity;
        import org.springframework.web.bind.annotation.*;

        import java.util.ArrayList;
        import java.util.List;
        import java.util.Optional;

@RestController
@RequestMapping("/usagePreference")
public class UsagePreferenceController {
    private List<UsagePreference> usagePreferences = new ArrayList<>();

    // Create a new Usage Preference
    @PostMapping
    public ResponseEntity<UsagePreference> createUsagePreference(@RequestBody UsagePreference usagePreference) {
        usagePreferences.add(usagePreference);
        return ResponseEntity.status(HttpStatus.CREATED).body(usagePreference);
    }

    // Get all Usage Preferences
    @GetMapping
    public ResponseEntity<List<UsagePreference>> getAllUsagePreferences() {
        return ResponseEntity.ok(usagePreferences);
    }

    // Get a Usage Preference by ID
    @GetMapping("/{id}")
    public ResponseEntity<UsagePreference> getUsagePreferenceById(@PathVariable int id) {
        Optional<UsagePreference> usagePreference = usagePreferences.stream()
                .filter(up -> up.getId() == id)
                .findFirst();

        return usagePreference
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Update a Usage Preference by ID
    @PutMapping("/{id}")
    public ResponseEntity<UsagePreference> updateUsagePreference(@PathVariable int id, @RequestBody UsagePreference updatedUsagePreference) {
        for (int i = 0; i < usagePreferences.size(); i++) {
            UsagePreference usagePreference = usagePreferences.get(i);
            if (usagePreference.getId() == id) {
                usagePreference.setCostEffectivePreference(updatedUsagePreference.isCostEffectivePreference());
                usagePreference.setEcoFriendlyPreference(updatedUsagePreference.isEcoFriendlyPreference());
                usagePreference.setFastPreference(updatedUsagePreference.isFastPreference());
                return ResponseEntity.ok(usagePreference);
            }
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    // Delete a Usage Preference by ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsagePreference(@PathVariable int id) {
        boolean removed = usagePreferences.removeIf(up -> up.getId() == id);
        if (removed) {
            return ResponseEntity.ok("Usage preference deleted successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usage preference not found.");
        }
    }
}

