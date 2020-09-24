/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.util.pref;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.MemberService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class PreferenceService implements Serializable {

    private static final long serialVersionUID = 1L;
    private final static String USER_ID = "USER_ID";
    private final static String KEY = "KEY";

    private final static String SQL_LOAD = "SELECT DISTINCT "
        + "p.id, p.user_id, p.key, p.value "
        + "FROM preferences AS p WHERE "
        + "     (p.user_id = :USER_ID OR :USER_ID = -1) "
        + " AND (p.key = :KEY OR :KEY = 'undefined') ";


    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private MemberService memberService;

    private Logger logger;

    public PreferenceService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Obtain the Preference for a given user and key
     *
     * @param user - the user
     * @param key - String key
     * @param dflt - the default value
     * @return Preference object
     */
    public Preference getPreference(User user, String key, String dflt) {
        List<Preference> result = load(user, key);
        if ((result == null) || (result.size() == 0)) {
            return new Preference(user, key, dflt);
        }
        return result.get(0);
    }

    /**
     * Obtain the preference value for a given user and key
     *
     * @param user - the user
     * @param key - String key
     * @param dflt - the default value
     * @return Preference object
     */
    public String getPreferenceValue(User user, String key, String dflt) {
        return getPreference(user, key, dflt).getValue();
    }

    /**
     * load Preferences
     */
    public List<Preference> load(User user, String key) {
        Query q = this.em.createNativeQuery(SQL_LOAD, PreferenceEntity.class);

        q.setParameter(USER_ID, (user != null) ? user.getId() : -1);
        q.setParameter(KEY, (key != null) ?  key : "undefined");
        // q.setFirstResult();
        // q.setMaxResults();

        List<Preference> result = new ArrayList<>();
        for (PreferenceEntity e :  (List<PreferenceEntity>) q.getResultList()) {
            result.add(new Preference(e, 
                this.memberService.loadUserById(e.getUserId())));
        }
        return result;
    }

    /**
     * save a preference
     * @param pref - the preference to save
     * @return a persisted preference
     */
    public Preference save(Preference pref) {
        return new Preference(this.em.merge(pref.createEntity()),
            pref.getUser());
    }

    /**
     * simple fire and forget set preference method
     * @param user the user for whom the preference is saved
     * @param key the key of the preference
     * @param value the preference value
     */
    public void setPreference(User user, String key, String value) {
        save(new Preference(user, key, value));
    }
}
