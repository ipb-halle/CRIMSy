package de.ipb_halle.lbac.admission.mock;

import de.ipb_halle.lbac.admission.UserPluginSettingsBean;
import java.util.Collections;
import java.util.List;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;

/**
 *
 * @author fmauz
 */
@SessionScoped
public class UserPluginSettingsBeanMock extends UserPluginSettingsBean {

    private static final long serialVersionUID = 1L;

    @PostConstruct
    @Override
    public void init() {

    }

}
