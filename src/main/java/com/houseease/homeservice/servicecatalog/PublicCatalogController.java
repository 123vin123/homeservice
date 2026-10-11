package com.houseease.homeservice.servicecatalog;

import com.houseease.homeservice.servicecatalog.dto.CategoryResponse;
import com.houseease.homeservice.servicecatalog.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PublicCatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> getCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getServices() {
        return ResponseEntity.ok(catalogService.getAllServices());
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceResponse> getService(@PathVariable UUID id) {
        return ResponseEntity.ok(catalogService.getServiceById(id));
    }
}
