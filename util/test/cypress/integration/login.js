describe('CRIMSy login test', () => { 

    it('logs into CRIMSy and logs out again', () => {

        cy.visit('https://nwc04170.ipb-halle.de/ui/index.xhtml')

        // Log in
        cy.get('.tstLoginCmdLink').click()
        cy.get('.tstLoginLogin').type('admin')
        cy.get('.tstLoginPassword').type('admin')
        cy.get('.tstLoginCmdBtn').click()

        expect('.tstLogoutCmdLink').to.contain('Logout')

        // Log out
        cy.get('.tstLogoutCmdLink').click()
        expect('.tstLoginCmdLink').to.contain('Login')
        

    })
})

