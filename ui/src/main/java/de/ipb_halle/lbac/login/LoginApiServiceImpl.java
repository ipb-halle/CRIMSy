/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.login;

//import de.ipb_halle.api.LoginTestApiService;
import de.ipb_halle.api.LoginApi;
import de.ipb_halle.api.LoginApiService;
//import de.ipb_halle.client_model.LoginTestRequest;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.model.LoginRequest;
import de.ipb_halle.model.LoginResponse;
import jakarta.enterprise.context.ApplicationScoped;


import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

/**
 *
 * @author halocal
 */

//public class LoginTestApiImpl {
public class LoginTestApiImpl implements LoginApiService {

    @Inject
    LogInProcess loginProcess;


   @Override
    public Response login(LoginRequest loginRequest, SecurityContext securityContext) {
       
        String login = loginRequest.getLogin();
        String password = loginRequest.getPassword();
        User user = loginProcess.tryLogIn(login, password);
        if (user != null) {
            return Response.status(Response.Status.OK)
                    .entity("{\"message\":\"Login succeeded\"}")
                    .build();
        } else {

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"message\":\"Login failed\"}")
                    .build();
        }
    }
   


}
