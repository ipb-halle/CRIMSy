## Dev cheat sheet
### Rules of thumb for element selectors of JSF components using `pt:data-test-id="testId"`
#### native JSF
* `<h:outputText>` ([rendering depends on other component attributes](https://docs.oracle.com/javaee/7/javaserver-faces-2-2/vdldocs-facelets/h/outputText.html)): `$(testId("testId"))`
  * rendered as `<span>`: attach `pt:data-test-id` to `<h:outputText>`
  * rendered as inline text: attach `pt:data-test-id` to parent component or embed it into a `<div data-test-id="testId">`

#### BootsFaces
* `<b:commandButton>`: `$(testId("testId"))`
* `<b:dataTable>`: use `DataTable.extract()` with `$(testId("testId"))`
* `<b:inputText>`: `$(testId("input", "testId"))`
* `<b:inputTextarea>`: `$(testId("textarea", "testId"))`
* `<b:message>`: `$(testId("testId"))`
* `<b:selectBooleanCheckbox>`: `$(testId("input", "testId"))`
* `<b:selectOneMenu>`: `$(testId("select", "testId"))`

#### PrimeFaces
* `<p:autoComplete>`: `$(testId("testId"))`; TODO: evaluate autocomplete suggestions
* `<p:commandButton>`: `$(testId("testId"))`
* `<p:inputText>`: `$(testId("testId"))`
* `<p:selectBooleanCheckbox>`: too complex, use class `PrimeFacesSelectBooleanCheckbox`
* `<p:selectManyCheckbox>`: too complex, use class `PrimeFacesSelectManyCheckbox`
* `<p:selectOneRadio>`: too complex, use class `PrimeFacesSelectOneRadio`
* `<p:tab>` inside `<p:tabView>`: `$(testId("testId"))`

#### MolecularFaces
* `<mol:molecule>`: use class `MolecularFacesMolecule`
* `<mol:openVectorEditor>`: use class `MolecularFacesOpenVectorEditor`