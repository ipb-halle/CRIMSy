package de.ipb_halle.lbac.authentication.service;

import de.ipb_halle.api.AuditApiService;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

public class AuditApiServiceImpl implements AuditApiService {

    @Override
    public Response auditLogsGet(SecurityContext securityContext) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'auditLogsGet'");
    }
    
}
