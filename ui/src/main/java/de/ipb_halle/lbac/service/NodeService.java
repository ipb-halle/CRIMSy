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
package de.ipb_halle.lbac.service;

/**
 * NodeService provides service to load, save, update nodes.
 */
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.NodeEntity;
import de.ipb_halle.lbac.entity.NodeEntity_;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.log4j.Logger;

// import javax.persistence.Persistence;
@Stateless
public class NodeService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;
    private Node localNode;

    public NodeService() {
        this.localNode = null;
        this.logger = Logger.getLogger(this.getClass().getName());
    }

    public Node getLocalNode() {
        if (this.localNode == null) {
            this.localNode = this.load(null, Boolean.TRUE).get(0);
        }
        return this.localNode;
    }

    public UUID getLocalNodeId() {
        return getLocalNode().getId();
    }

    /**
     * check if node is remote
     *
     * @param n the node to test
     * @return true if the node successfully has been verified to be a remote
     * node. false is returned for local nodes or on error.
     */
    public boolean isRemoteNode(Node n) {
        return !this.getLocalNodeId().equals(n.getId());
    }

    /**
     * Load a list of nodes from the local database. The list of 
     * nodes will never contain the public node.
     *
     * @param id the node id or null if all nodes should be fetched
     * @param local true if the local node only should be fetched, false to
     * fetch only remote nodes and null if all nodes should be fetched
     * @return the list of nodes
     */
    @SuppressWarnings("unchecked")

    public List<Node> load(String id, Boolean local) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<NodeEntity> criteriaQuery = builder.createQuery(NodeEntity.class);
        Root<NodeEntity> nodeRoot = criteriaQuery.from(NodeEntity.class);
        criteriaQuery.select(nodeRoot);
        List<Predicate> predicates = new ArrayList<Predicate>();
        if (id != null) {
            predicates.add(builder.equal(nodeRoot.get("id"), id));
        }
        if (local != null) {
            predicates.add(builder.equal(nodeRoot.get("local"), local));
        }

        predicates.add(builder.not(nodeRoot.get("publicNode")));

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));

        List<Node> results = new ArrayList<> ();
        for(NodeEntity entity: this.em.createQuery(criteriaQuery).getResultList()) {
            results.add(new Node(entity));
        }
        return results;
    }

    /**
     * load a node by id
     *
     * @param id nodeId
     * @return the Node object
     */
    public Node loadById(UUID id) {
        NodeEntity entity = this.em.find(NodeEntity.class, id);
        if (entity != null) {
            return new Node(entity);
        }
        return null;
    }

    /**
     * save a single node
     *
     * @param n the node to save
     */
    public Node save(Node n) {
        this.em.merge(n.createEntity());
        return n; 
    }

}
