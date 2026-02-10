
# RoleResponse


## Properties

Name | Type
------------ | -------------
`username` | string
`groups` | Array&lt;string&gt;
`admin` | boolean

## Example

```typescript
import type { RoleResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "username": johndoe,
  "groups": ["Users","Admin Group"],
  "admin": true,
} satisfies RoleResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as RoleResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


