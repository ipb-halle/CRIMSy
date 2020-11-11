describe('CRIMSy user and group management test', () => { 

    it('Tests user and groups management', () => {

        cy.fixture('data.js').then((data) => {
            cy.visit('https://nwc04170.ipb-halle.de/ui/index.xhtml')

            // Log in
            cy.get('.tstLoginCmdLink').click()
            cy.get('.tstLoginLogin').type('admin')
            cy.get('.tstLoginPassword').type('admin')
            cy.get('.tstLoginCmdBtn').click()


            // add group 
            cy.get('.tstNavSettings').click()
            cy.get('.tstNavGroupManager').click()
            cy.get('.tstGrpMgrNewGroup').click()

            cy.get('.modal-content').should('be.visible')

            cy.get('.tstGrpMgrNewName').type(data.groupName)
            cy.get('.tstGrpMgrCreate').click()

            // add user
            cy.get('.tstNavSettings').click()
            cy.get('.tstNavUserManager').click()
            cy.get('.tstUserMgrCreateDlg').click()

            cy.get('.modal-content').should('be.visible')

            cy.get('.tstUserMgrName').type(data.userName)
            cy.get('.tstUserMgrLogin').type(data.userLogin)
            cy.get('.tstUserMgrEmail').type(data.userLogin + '@somewhere.invalid')
            cy.get('.tstUserMgrPassword').type('test1234')
            cy.get('.tstUserMgrPasswordRepeat').type('test1234')
            cy.get('.tstUserMgrCreate').click()

            // manage group membership
            cy.get('#frmUserList:userList_wrapper input[type="search"]').type(data.userLogin)

        })

    })
})

