* Compress a template folder to a Pentaho report template:
`jar -cf testReport1.prpt -C testReport1 .`

## Test report templates ##
### testReport1.prpt ###
* uses the DataSource defined in arquillian.xml (apiDS) via its JNDI name
* parameters: `paramHello` (Integer), `paramWorld` (String)
* What is it doing? Writes the two parameters.