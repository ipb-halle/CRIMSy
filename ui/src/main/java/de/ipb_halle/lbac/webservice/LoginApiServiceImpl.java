package de.ipb_halle.lbac.webservice;


import de.ipb_halle.api.LoginApiService;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.model.LoginRequest;
import de.ipb_halle.model.LoginResponse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
public class LoginApiServiceImpl implements LoginApiService {

    @Inject
    LogInProcess loginProcess;

    @Override
    public Response login(LoginRequest loginRequest, jakarta.ws.rs.core.SecurityContext securityContext) {
        if (loginRequest == null
                || loginRequest.getUsername() == null
                || loginRequest.getPassword() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("username/password missing")
                    .build();
        }

        User user = loginProcess.tryLogIn(loginRequest.getUsername(), loginRequest.getPassword());
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Login failed")
                    .build();
        }

        LoginResponse resp = new LoginResponse();
        resp.setMessage("Login succeeded");
        resp.setUsername(user.getLogin());

        return Response.ok(resp).build();
    }
}
