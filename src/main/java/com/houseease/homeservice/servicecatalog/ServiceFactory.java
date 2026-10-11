package com.houseease.homeservice.servicecatalog;

import com.houseease.homeservice.servicecatalog.dto.ServiceRequest;
import org.springframework.stereotype.Component;

@Component
public class ServiceFactory {

    public ServiceEntity createServiceEntity(ServiceRequest request, Category category) {
        ServiceEntity svc = new ServiceEntity();
        svc.setCategory(category);
        svc.setName(request.getName());
        svc.setDescription(request.getDescription());
        // Default to 60 minutes if not provided
        svc.setDurationMinutes(request.getDurationMinutes() != null ? request.getDurationMinutes() : 60);
        svc.setSupportsHomeService(request.getSupportsHomeService());
        return svc;
    }
}
