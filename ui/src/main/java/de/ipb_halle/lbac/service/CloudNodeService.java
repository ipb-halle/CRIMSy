/*
 * Leibniz Bioactives Cloud
 * Copyright 2019 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.service;

/**
 * CloudNodeService provides service to load, save, update CloudNode entities.
 */
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.CloudNodeEntity;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.NodeEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

@Stateless
public class CloudNodeService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private CloudService cloudService;
    
    @Inject
    private NodeService nodeService;
    
    private Logger logger;

    public CloudNodeService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * load a single cloud node object
     * @param cloud the cloud
     * @param node the node
     * @return the matching CloudNode object or null
     */
    public CloudNode loadCloudNode(Cloud cloud, Node node) {
        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<CloudNodeEntity> criteriaQuery = builder.createQuery(CloudNodeEntity.class);
        Root<CloudNodeEntity> cloudNodeRoot = criteriaQuery.from(CloudNodeEntity.class);
        criteriaQuery.select(cloudNodeRoot);

        if ((cloud == null) || (node == null)) {
            return null;
        }

        criteriaQuery.where(builder.and(new Predicate[] {
            builder.equal(cloudNodeRoot.get("cloud"), cloud.getId()), 
            builder.equal(cloudNodeRoot.get("node"), node.getId()) }));

        try {
            CloudNodeEntity entity = this.em.createQuery(criteriaQuery).getSingleResult();
            return new CloudNode(entity, cloud, node);
        } catch(NoResultException e) {
            // ignore
        }
        return null;
    }

    /**     
     * load a single cloud node object
     * @param cloudName the name of the cloud
     * @param nodeId the UUID of the node
     * @return the matching CloudNode object or null
     */
    public CloudNode loadCloudNode(String cloudName, UUID nodeId) {

        if ((cloudName == null) || (nodeId == null)) {
            return null;
        }

        Cloud cloud = this.cloudService.loadByName(cloudName);
        Node node = this.nodeService.loadById(nodeId);
        if ((cloud == null) || (node == null)) {
            return null;
        }
        return loadCloudNode(cloud, node);
    }


    /**
     * Load a list of active (not failed) CloudNodes for a specific cloud
     * @param cloud the cloud for which the cloud nodes should be obtained
     * @param node the node for which to obtain the CloudNodes
     * @return a list of CloudNodes
     */
    public List<CloudNode> load(Cloud cloud, Node node) {
        return load(cloud, node, Boolean.TRUE);
    }

    /**
     * Load a list of CloudNodes for a specific cloud
     * @param cloud the cloud for which the cloud nodes should be obtained
     * @param node the node for which to obtain the CloudNodes
     * @param active ...
     * @return a list of CloudNodes
     */
    public List<CloudNode> load(Cloud cloud, Node node, Boolean active) {
        
        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<CloudNodeEntity> criteriaQuery = builder.createQuery(CloudNodeEntity.class);
        Root<CloudNodeEntity> cloudNodeRoot = criteriaQuery.from(CloudNodeEntity.class);
        criteriaQuery.select(cloudNodeRoot);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (cloud != null) {
            predicates.add(builder.equal(cloudNodeRoot.get("cloud"), cloud.getId())); 
        } 
        if (node != null) {
            predicates.add(builder.equal(cloudNodeRoot.get("node"), node.getId()));
        }
        if (active != null) {
            long time = new Date().getTime();
            if (active.booleanValue()) {
                // CloudNode is 'ok' (or in state 'retry')
                predicates.add(builder.lessThanOrEqualTo(cloudNodeRoot.get("retrytime"), time));
            } else {
                // CloudNode is in state 'failed'
                predicates.add(builder.greaterThan(cloudNodeRoot.get("retrytime"), time));
            }
        }

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));
        List<CloudNode> result = new ArrayList<CloudNode> ();
        for(CloudNodeEntity e:  this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new CloudNode(e,
              (cloud == null) ? this.cloudService.loadById(e.getCloud()) : cloud,
              (node == null) ? this.nodeService.loadById(e.getNode()) : node));
        }
        return result;
    }

    /**
     * Load the master node of the respective cloud. This method could 
     * either iterate over the set of (lazily loaded) nodes or perform 
     * a single database query.
     *
     * @param cloud the cloud object
     * @return the master node
     */
    public Node loadMasterNode(Cloud cloud) {

        String qry = "SELECT n.id AS id, n.baseurl AS baseurl, n.institution AS institution, "
          + "n.local AS local, n.publicnode AS publicnode, n.version AS version " 
          + "FROM nodes AS n JOIN (SELECT node_id FROM cloud_nodes AS cnn JOIN (SELECT cloud_id, max(rank) AS rank "
          + "FROM cloud_nodes WHERE cloud_id=? GROUP BY cloud_id) AS cnr ON cnn.rank=cnr.rank "
          + "AND cnn.cloud_id=cnr.cloud_id) AS cn ON n.id=cn.node_id";

        // this will throw NoResultException (among other exceptions) if no master node is present
        Node node = new Node((NodeEntity) this.em.createNativeQuery(qry, NodeEntity.class)
          .setParameter(1, cloud.getId())
          .setMaxResults(1)
          .getSingleResult());

        return node;
    }

    /**
     * load a CloudNode by id
     *
     * @param id id of the CloudNode object
     * @return the CloudNode object
     */
    public CloudNode loadById(Long id) {
        CloudNodeEntity entity = this.em.find(CloudNodeEntity.class, id);
        Cloud cloud = this.cloudService.loadById(entity.getCloud());
        Node node = this.nodeService.loadById(entity.getNode());
        return new CloudNode(entity, cloud, node);
    }

    /**
     * save a single CloudNode object
     * @param cn the CloudNode to save
     * @return the managed CloudNode object
     */
    public CloudNode save(CloudNode cn) {
        cn.setCloud(this.cloudService.save(cn.getCloud()));
        cn.setNode(this.nodeService.save(cn.getNode()));
        CloudNodeEntity e = cn.createEntity();
        e = this.em.merge(e);
        return new CloudNode(e, 
                cn.getCloud(), 
                cn.getNode());
    }
}
