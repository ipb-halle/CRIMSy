describe('CRIMSy user and group management test', () => { 

    it('Tests user and groups management', () => {

        cy.fixture('data.js').then((data) => {
            cy.visit(data.testURL) 

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

            // manage group membership (add group)
            cy.get('#frmUserList\\:userList').find('.tstUserMgrMembershipDlg').should('have.length.greaterThan', 1)
            cy.get('#frmUserList\\:userList_filter input[type="search"]').type(data.userLogin)
            cy.get('#frmUserList\\:userList').find('.tstUserMgrMembershipDlg').should('have.length', 1)
            cy.get('.tstUserMgrMembershipDlg').click()

            cy.get('.modal-content').should('be.visible')
            cy.get('#frmModalGroupDialog\\:groupList_filter input[type="search"]').type(data.groupName)
            cy.get('.tstUserMgrMembershipAdd').click()
            cy.get('.tstUserMgrMembershipClose').click()

            // final check in group dialog (test group has 1 member)
            cy.get('.tstNavSettings').click()
            cy.get('.tstNavGroupManager').click()
            cy.get('#frmGroupList\\:groupList_filter input[type="search"]').type(data.groupName)
            cy.get('#frmGroupList\\:groupList').find('.tstGrpMgrMembershipDlg').should('have.length', 1)
            cy.get('.tstGrpMgrMembershipDlg').click()
            
            cy.get('.modal-content').should('be.visible')
            cy.get('#frmModalMembershipDialog\\:membershipList').find('.tstGrpMgrRemoveMember').should('have.length', 1)
            cy.get('.tstGrpMgrMembershipClose').click()

        })

    })
})

