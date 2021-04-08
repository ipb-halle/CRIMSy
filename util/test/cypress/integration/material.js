describe('CRIMSy material creation test', () => { 

    it('Test creation of new material (compound)', () => {

        cy.fixture('data.js').then((data) => {
            cy.visit(data.testURL);

            cy.viewport(1600, 900);

            // Log in
            cy.get('.tstLoginCmdLink').click();
            cy.get('.tstLoginLogin').type('admin');
            cy.get('.tstLoginPassword').type('admin');
            cy.get('.tstLoginCmdBtn').click();

            // enter materials form
            cy.get('.tstNavLIMS > .dropdown-menu').invoke('show');
            cy.get('.tstNavMaterials').click();

            // add materials
            for (const mat of data.compounds) {
                cy.get('.tstNewMaterial').click() ;
                cy.get('.tstProjectMenu').select("integration");
                cy.get('.tstMaterialTypeMenu').select('STRUCTURE');

                cy.get('a').contains('Structureinformation').should('not.be.disabled');
                cy.wait(400);

                cy.get('.tstMaterialNameInput').type(mat.name);

                cy.get('a').contains('Structureinformation').click();

                cy.wait(1000);
                cy.window().then((win) => {
                    win.structurePlugin.then(e=> e.setMol(mat.mol));
                });
                cy.wait(200);
                cy.get('.tstMaterialCreate').click();
            }

            cy.wait(200);
            cy.contains(data.compounds[0].name).should("be.visible");

        });

    });
});

