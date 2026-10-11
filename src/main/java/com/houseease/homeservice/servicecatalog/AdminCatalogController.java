package com.houseease.homeservice.servicecatalog;

import com.houseease.homeservice.servicecatalog.dto.CategoryRequest;
import com.houseease.homeservice.servicecatalog.dto.CategoryResponse;
import com.houseease.homeservice.servicecatalog.dto.ServiceRequest;
import com.houseease.homeservice.servicecatalog.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminCatalogController {

    private final CatalogService catalogService;

    @PostMapping("/services")
    public ResponseEntity<ServiceResponse> createService(@RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogService.createService(request));
    }

    @PutMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> updateService(@PathVariable UUID id, @RequestBody ServiceRequest request) {
        return ResponseEntity.ok(catalogService.updateService(id, request));
    }

    @DeleteMapping("/services/{id}")
    public ResponseEntity<Void> deleteService(@PathVariable UUID id) {
        catalogService.deleteService(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        return ResponseEntity.ok(catalogService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable UUID id, @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(catalogService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
