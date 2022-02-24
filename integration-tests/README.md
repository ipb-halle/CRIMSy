## Dev cheat sheet
### Rules of thumb for element selectors of JSF components using `pt:data-test-id="testId"`
#### native JSF
* `<h:inputText>`: `$(testId("testId"))`
* `<h:outputText>` ([rendering depends on other component attributes](https://docs.oracle.com/javaee/7/javaserver-faces-2-2/vdldocs-facelets/h/outputText.html)): `$(testId("testId"))`
  * rendered as `<span>`: attach `pt:data-test-id` to `<h:outputText>`
  * rendered as inline text: attach `pt:data-test-id` to parent component or embed it into a `<div data-test-id="testId">`

#### BootsFaces
* `<b:commandButton>`: `$(testId("testId"))`
* `<b:dataTable>`: use class `DataTable`
* `<b:inputText>`: `$(testId("input", "testId"))`
* `<b:inputTextarea>`: `$(testId("textarea", "testId"))`
* `<b:message>`: `$(testId("testId"))`
* `<b:navLink>`: `$(testId("a", "testId"))`
* `<b:selectBooleanCheckbox>`: `$(testId("input", "testId"))`
* `<b:selectOneMenu>`: `$(testId("select", "testId"))`

#### PrimeFaces
* `<p:autoComplete>`: `$(testId("testId"))`; TODO: evaluate autocomplete suggestions
* `<p:commandButton>`: `$(testId("testId"))`
* `<p:dataTable>`: use class `PrimeFacesDataTable`
* `<p:dialog>`: use class `PrimeFacesDialog`
* `<p:inputText>`: `$(testId("testId"))`
* `<p:selectBooleanCheckbox>`: use class `PrimeFacesSelectBooleanCheckbox`
* `<p:selectManyCheckbox>`: use class `PrimeFacesSelectManyCheckbox`
* `<p:selectOneRadio>`: use class `PrimeFacesSelectOneRadio`
* `<p:tab>` inside `<p:tabView>`: `$(testId("testId"))`
* `<p:tooltip>`: use class `PrimeFacesTooltip`

#### MolecularFaces
* `<mol:molecule>`: use class `MolecularFacesMolecule`
* `<mol:openVectorEditor>`: use class `MolecularFacesOpenVectorEditor`