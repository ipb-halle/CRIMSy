/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package de.ipb_halle.lbac.search;

import de.ipb_halle.api.SearchApiService;
import de.ipb_halle.lbac.admission.MemberEntity;
import de.ipb_halle.lbac.items.entity.ItemEntity;
import de.ipb_halle.lbac.security.service.TokenService;
import de.ipb_halle.model.ErrorResponse;
import de.ipb_halle.model.MaterialType;
import de.ipb_halle.model.RoleResponse;
import de.ipb_halle.model.SearchRequest;
import de.ipb_halle.model.SearchResult;
import de.ipb_halle.model.SearchType;

import jakarta.inject.Inject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;

import java.util.List;

/**
 *
 * @author halocal
 */
public class SearchApiServiceImpl implements SearchApiService {

    @Inject
    TokenService tokenService;

    @PersistenceContext
    private EntityManager em;

    @Context
    private HttpHeaders headers;

    @Override
    public Response search(SearchRequest searchRequest, SecurityContext securityContext) {
        System.err.println("\n\nI am in search method\n\n");

        SearchResult searchResult = new SearchResult();

        if (searchRequest.getSearchTypes().contains(SearchType.ITEMS)) {
            searchResult.setDomain(SearchType.ITEMS);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId("1");
            searchResult.setLabel("test connection");
        } else {
            searchResult.setDomain(SearchType.NONE);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId("0");
            searchResult.setLabel("empty result");
        }

        System.err.println("searchTypes: " + searchRequest.getSearchTypes());
        System.err.println("searchResult prepared: " + searchResult);

        return Response.ok(searchResult).build();
    }

}
