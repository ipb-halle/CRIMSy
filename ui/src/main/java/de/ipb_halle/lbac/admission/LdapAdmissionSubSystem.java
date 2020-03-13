/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Member;
import de.ipb_halle.lbac.entity.Membership;
import de.ipb_halle.lbac.entity.MemberType;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

public class LdapAdmissionSubSystem extends AbstractAdmissionSubSystem {

    private final AdmissionSubSystemType subSystemType; 
    private transient Logger logger;

    /**
     * default constructor
     */
    public LdapAdmissionSubSystem() {
        this.subSystemType = AdmissionSubSystemType.LDAP;
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * authenticate a user, perform a full lookup and update the 
     * local user database
     * @param u the user 
     * @param cred the credential string
     * @param bean the current sessions UserBean
     * @return true if the user could be authenticated,  false if not or LDAP is not configured 
     */
    public boolean authenticate(User u, String cred, UserBean bean) {
        LdapProperties prop = bean.getLdapProperties();
        if(! prop.getLdapEnabled()) {
            return false;
        }

        // authenticate
        LdapHelper helper = new LdapHelper(prop);
        try {
            if(! helper.authenticate(u.getLogin(), cred)) {
                // login failure
                this.logger.info("authenticate() failed to authenticate user: " + u.getLogin());
                return false;
            }
        } catch(Exception e) {
            // LDAP failure?
            this.logger.warn("authenticate() caught an exception: ", (Throwable) e);
            return false;
        }

        // do full lookup; map dn vs. LdapObject
        Map<String, LdapObject> ldapObjects = new HashMap<String, LdapObject> ();
        LdapObject ldapUser = helper.queryLdapUser(u.getLogin(), ldapObjects);
        if(ldapUser != null) {
            // update User (user object might be 'latent')
            User user = lookupLbacUser(ldapUser, bean);
            
            // to keep the user object which is used in the UserBean up to date,
            // its id is updated with the persistent one to enable user loading
            // in following steps
            u.setId(user.getId());
            // map the LdapObjects to Group, map might contain 'latent' objects
            Map<UUID, Member> ldapUuidMap = getLdapGroups(ldapObjects, bean);
            ldapUuidMap.put(user.getId(), user);

            // compute the LDAP Memberships
            // all the elements in this map are transient!
            Map<String, Membership> ldapMemberships = getLdapMemberships(ldapObjects, ldapUuidMap);

            // get the direct Memberships from LBAC
            Map<String, Membership> lbacMemberships = getLbacMemberships(ldapObjects, bean);

            // save the groups
            saveObjects(ldapUuidMap, bean);
            
            // remove all memberships from LBAC which are not present in 
            // the LDAP map ldapGroups; NOTE: lbacMemberships is modified
            removeExpiredMemberships(lbacMemberships, ldapMemberships, bean);

            // update / add memberships
            updateMemberships(lbacMemberships, ldapMemberships, bean);

            return true;
        }


        return false;
    }

    /**
     * get the Memberships from LBAC including all nested memberships
     * mapped by their groupId.
     * @param lo the map of LdapObjects by DN 
     * @param bean the current sessions UserBean
     * @return the map of memberships mapped by groupId
     */
    private Map<String, Membership> getLbacMemberships(Map<String, LdapObject> ldapObjects, UserBean bean) {
        Map<String, Membership> lbacMemberships = new HashMap<String, Membership> ();
        Map<String, Object> cmap = new HashMap<String, Object> ();
        cmap.put("group_node", bean.getNodeService().getLocalNode().getId());
        cmap.put("group_subSystemType", AdmissionSubSystemType.LDAP);
        cmap.put("nested", Boolean.FALSE);

        Iterator<LdapObject> iter = ldapObjects.values().iterator();
        while(iter.hasNext()) {
            LdapObject lo = iter.next();
            cmap.put("member_id", lo.getId());
            List<Membership> ms = bean.getMembershipService().load(cmap);

            lbacMemberships.putAll(ms.stream().collect(
              Collectors.toMap(x -> String.join("|", x.getGroupId().toString(), x.getMemberId().toString()), y -> y)));
        }

        return lbacMemberships; 
    }

    /**
     * build a map of Memberships from the ldapObjects map
     * @param ldapObjects a map of LDAP objects mapped by DN
     * @param ldapUuidMap a map of LBAC objects mapped by id (UUID)
     * @return a map of memberships mapped by groupId|memberId
     */
    private Map<String, Membership> getLdapMemberships(Map <String, LdapObject> ldapObjects, Map<UUID, Member> ldapUuidMap) {
        Map<String, Membership> ldapMemberships = new HashMap<String, Membership> ();
        Iterator<LdapObject> iter = ldapObjects.values().iterator();
        while(iter.hasNext()) {
            LdapObject lo = iter.next();

            // include the self-membership
            ldapMemberships.put(
             String.join("|", lo.getId().toString(), lo.getId().toString()),
              new Membership(ldapUuidMap.get(lo.getId()), ldapUuidMap.get(lo.getId()), false));

            ListIterator<String> listIter = lo.getMemberships().listIterator();
            while(listIter.hasNext()) {
                LdapObject go = ldapObjects.get(listIter.next());
                ldapMemberships.put(
                  String.join("|", go.getId().toString(), lo.getId().toString()),
                  new Membership(ldapUuidMap.get(go.getId()), ldapUuidMap.get(lo.getId()), false));
            }
        }
        return ldapMemberships;
    }

    /**
     * map the LdapObjects to Group objects. The result map 
     * might contain 'latent' objects which are not yet known 
     * to LBAC
     * @param lo the LdapObjects mapped by their DN
     * @param bean UserBean 
     * @return LBAC Group objects mapped by their UUID
     */
    private Map<UUID, Member> getLdapGroups(Map<String, LdapObject> lo, UserBean bean) {
        return lo.values().stream()
          .filter(lg -> lg.getType() == MemberType.GROUP)
          .map(lg -> lookupLbacGroup(lg, bean))
          .collect(Collectors.toMap(g -> g.getId(), h -> (Member) h));
    }

    /**
     * @return the AdmissionSubSystemType implemented by this class
     */
    public AdmissionSubSystemType getSubSystemType() { return this.subSystemType; }

    /**
     * lookup a user in the directory
     * @param login the login name (or email address)
     * @return the User object or null if user could not be found or LDAP is not configured
     */
    public User lookup(String login, UserBean bean) {
        LdapProperties prop = bean.getLdapProperties();
        if(! prop.getLdapEnabled()) {
            return null;
        }

        LdapHelper helper = new LdapHelper(prop); 
        LdapObject ldapUser = helper.queryLdapUser(login, null);
        if(ldapUser != null) {
            return lookupLbacUser(ldapUser, bean);
        }
        return null;
    }

    /**
     * lookup LBAC group by LDAP uniqueId
     * @param lo the LDAP group object. If lookup is successful, the id property
     * of this object will be modified to contain the UUID of the corresponding
     * LBAC object
     * @param bean the current sessions UserBean 
     * @return the Group object from the database or a 'latent' Group object which will
     * be persisted upon successful authentication. 
     */
    private Group lookupLbacGroup(LdapObject lo, UserBean bean) {
        Node node = bean.getNodeService().getLocalNode();
        Map<String, Object> cmap = new HashMap<String, Object> ();
        cmap.put("subSystemType", AdmissionSubSystemType.LDAP);
        cmap.put("subSystemData", lo.getUniqueId());
        cmap.put("node_id", node.getId());
        List<Group> groups = bean.getMemberService().loadGroups(cmap);
        Group g;

        if((groups != null) && (groups.size() == 1)) {
            g = groups.get(0);
            lo.setId(g.getId());
        } else {
            g = lo.createGroup();
            g.setNode(node);
        }
        return g;
    }

    /**
     * lookup LBAC user by LDAP uniqueId
     * @param bean UserBean 
     * @param lo the LDAP user object. If lookup is successful, the id property
     * of this object will be modified to contain the UUID of the corresponding
     * LBAC object
     * @return the User object from the database or a 'latent' User object which will 
     * be persisted upon successful authentication
     */
    private User lookupLbacUser(LdapObject lo, UserBean bean) {
        Node node = bean.getNodeService().getLocalNode();
        Map<String, Object> cmap = new HashMap<String, Object> ();
        cmap.put("subSystemType", AdmissionSubSystemType.LDAP); 
        cmap.put("subSystemData", lo.getUniqueId());
        cmap.put("node", node);

        List<User> users = bean.getMemberService().loadUsers(cmap);
        if((users != null) && (users.size() == 1)) {
            User u = users.get(0);
            lo.setId(u.getId());
            u.setEmail(lo.getEmail());
            u.setPhone(lo.getPhone());
            u.setName(lo.getName());
            return u;
        }
        User u =lo.createUser();
        u.setNode(node);
        return u;
    }

    /**
     * remove memberships from the LBAC database, which are not present 
     * in LDAP. This method will modify the database and the lbacMS list.
     * @param lbacMS the list of memberships from the LBAC database. Elements might
     * be removed from this list by this method.
     * @param ldapG the map of LDAP groups mapped by their id. This map may contain 
     * latent objects
     * @param bean the current sessions UserBean 
     */
    private void removeExpiredMemberships(Map<String, Membership> lbacMS, Map<String, Membership> ldapMS, UserBean bean) {
        Iterator<Map.Entry<String, Membership>> iter = lbacMS.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String, Membership> entry = iter.next();

            // remove LBAC memberships not present in LDAP
            if(ldapMS.get(entry.getKey()) == null) {
                Membership ms = entry.getValue();
                this.logger.info("removeExpiredMemberships() Group -> " 
                  + ms.getGroup().getName()
                  + "   Member -> " 
                  + ms.getMember().getName());
                bean.getMembershipService().removeMembership(ms); 
                iter.remove();
            }
        }
    }

    /**
     * save all the groups from the ldapGroups map to the database. The 
     * ldapGroups variable contains the user object. The user object is 
     * also added to the publicGroup.
     * 
     * @param ldapUuidMap a map containing all member objects discovered by LDAP
     */
    private void saveObjects(Map<UUID, Member> ldapUuidMap, UserBean bean) {
        Node node = bean.getNodeService().getLocalNode();
        Iterator<Member> iter = ldapUuidMap.values().iterator();
        while(iter.hasNext()) {
            Member m = iter.next();
            m.setNode(node);
            bean.getMemberService().save(m);
            bean.getMembershipService().addMembership(m,m);
            if (m.isUser()) {
                bean.getMembershipService().addMembership(
                  bean.getPublicGroup(), m);
            }
        }
    }

    /**
     * add memberships to the LBAC database, which were discovered from LDAP.
     * @param lbacMS the map of LBAC memberships
     * @param ldapMS the map of LDAP memberships. May contain latent objects
     * @param bean the current sessions UserBean
     */
    private void updateMemberships(Map<String, Membership> lbacMS, Map<String, Membership> ldapMS, UserBean bean) {
        Iterator<Map.Entry<String, Membership>> iter = ldapMS.entrySet().iterator(); 
        while(iter.hasNext()) {
            Map.Entry<String, Membership> entry = iter.next();
            Membership ms = lbacMS.get(entry.getKey());

            // Membership not yet known
            if(ms == null) {
                ms = entry.getValue();
                // this.logger.info(String.format("updateMemberships()\n        Group:  %s\n        Member: %s", ms.getGroup().toString(), ms.getMember().toString()));
                bean.getMembershipService().addMembership(ms.getGroup(), ms.getMember());
            }
        }
    }

}

