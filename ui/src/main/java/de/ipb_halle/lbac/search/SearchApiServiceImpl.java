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
import de.ipb_halle.model.SearchResponse;
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

import java.util.ArrayList;
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
        SearchResult searchResult = new SearchResult();
        SearchResponse searchResponse = new SearchResponse();

        int count = 0;
        List<Object[]> items = new ArrayList<>();

        if (searchRequest.getSearchTypes().contains(SearchType.ITEMS)) {
            items = em.createQuery(
                    "SELECT it.id, it.label FROM ItemEntity it",
                    Object[].class)
                    .getResultList();

            for (Object[] row : items) {
                int id = (int) row[0];
                String label = (String) row[1];
                System.out.println("Item ID: " + id + " | Label: " + label);

                searchResult.setDomain(SearchType.ITEMS);
                searchResult.setSubtype(MaterialType.NONE);
                searchResult.setId(id);
                searchResult.setLabel(label);
                searchResponse.addResultsItem(searchResult);
                count++;
            }
        }
        if (searchRequest.getSearchTypes().contains(SearchType.EXPERIMENTS)) {

            System.out.println("EXPERIMENTS ID: " + 1 + " | Label: ex_label");

            searchResult.setDomain(SearchType.EXPERIMENTS);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId(1);
            searchResult.setLabel("ex_label");
            searchResponse.addResultsItem(searchResult);
            count++;
        }
        if (searchRequest.getSearchTypes().contains(SearchType.DOCUMENTS)) {

            System.out.println("DOCUMENTS ID: " + 1 + " | Label: doc_label");

            searchResult.setDomain(SearchType.DOCUMENTS);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId(1);
            searchResult.setLabel("doc_label");
            searchResponse.addResultsItem(searchResult);
            count++;
        }
        if (searchRequest.getSearchTypes().contains(SearchType.MATERIALS)) {

            System.out.println("MATERIALS ID: " + 1 + " | Label: mat_label");

            searchResult.setDomain(SearchType.MATERIALS);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId(1);
            searchResult.setLabel("mat_label");
            searchResponse.addResultsItem(searchResult);
            count++;
        } 
        else {
            searchResult.setDomain(SearchType.NONE);
            searchResult.setSubtype(MaterialType.NONE);
            searchResult.setId(0);
            searchResult.setLabel("empty result");
            searchResponse.addResultsItem(searchResult);
        }

        searchResponse.setTotal(count);
        return Response.ok(searchResponse).build();
    }

}
