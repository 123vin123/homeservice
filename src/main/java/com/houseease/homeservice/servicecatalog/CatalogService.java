package com.houseease.homeservice.servicecatalog;

import com.houseease.homeservice.servicecatalog.dto.CategoryRequest;
import com.houseease.homeservice.servicecatalog.dto.CategoryResponse;
import com.houseease.homeservice.servicecatalog.dto.ServiceRequest;
import com.houseease.homeservice.servicecatalog.dto.ServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CatalogService {

        private final CategoryRepository categoryRepository;
        private final ServiceRepository serviceRepository;
        private final ServiceFactory serviceFactory;

        public List<CategoryResponse> getAllCategories() {
                return categoryRepository.findByIsActiveTrue().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public List<ServiceResponse> getAllServices() {
                return serviceRepository.findByIsActiveTrue().stream()
                                .map(this::mapToResponse)
                                .collect(Collectors.toList());
        }

        public ServiceResponse getServiceById(UUID id) {
                ServiceEntity svc = serviceRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Service not found"));
                return mapToResponse(svc);
        }

        public CategoryResponse createCategory(CategoryRequest request) {
                Category category = new Category();
                category.setName(request.getName());
                category.setDescription(request.getDescription());
                category = categoryRepository.save(category);
                return mapToResponse(category);
        }

        public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Category not found"));
                category.setName(request.getName());
                category.setDescription(request.getDescription());
                category = categoryRepository.save(category);
                return mapToResponse(category);
        }

        public void deleteCategory(UUID id) {
                Category category = categoryRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Category not found"));
                category.setIsActive(false);
                categoryRepository.save(category);
        }

        public ServiceResponse createService(ServiceRequest request) {
                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));

                ServiceEntity svc = serviceFactory.createServiceEntity(request, category);

                svc = serviceRepository.save(svc);
                return mapToResponse(svc);
        }

        public ServiceResponse updateService(UUID id, ServiceRequest request) {
                ServiceEntity svc = serviceRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Service not found"));

                Category category = categoryRepository.findById(request.getCategoryId())
                                .orElseThrow(() -> new RuntimeException("Category not found"));

                svc.setCategory(category);
                svc.setName(request.getName());
                svc.setDescription(request.getDescription());
                svc.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
                svc.setSupportsHomeService(request.getSupportsHomeService());

                svc = serviceRepository.save(svc);
                return mapToResponse(svc);
        }

        public void deleteService(UUID id) {
                ServiceEntity svc = serviceRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Service not found"));
                svc.setIsActive(false);
                serviceRepository.save(svc);
        }

        private ServiceResponse mapToResponse(ServiceEntity svc) {
                return ServiceResponse.builder()
                                .id(svc.getId())
                                .categoryId(svc.getCategory().getId())
                                .categoryName(svc.getCategory().getName())
                                .name(svc.getName())
                                .description(svc.getDescription())
                                .durationMinutes(svc.getDurationMinutes())
                                .supportsHomeService(svc.getSupportsHomeService())
                                .build();
        }

        private CategoryResponse mapToResponse(Category cat) {
                return CategoryResponse.builder()
                                .id(cat.getId())
                                .name(cat.getName())
                                .description(cat.getDescription())
                                .build();
        }
}
