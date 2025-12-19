package de.ipb_halle.lbac.login;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LogInProcess;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

/**
 * REST endpoint for user login. This class assumes that the logic for login
 * authentication and handling is inside the UserBean.
 */
@Path("loginT")
@RequestScoped
public class LoginEndpoint {

    @Inject
    LogInProcess loginProcess;

  
    /**
     * This method handles the login process. It uses the UserBean's actionLogin
     * method to authenticate the user.
     *
     * @param login The login (username) of the user.
     * @param password The password of the user.
     * @return A Response indicating whether the login was successful or not.
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(
            @FormParam("login") String login,
            @FormParam("password") String password) {

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
