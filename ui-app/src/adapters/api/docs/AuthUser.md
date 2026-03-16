
# AuthUser


## Properties

Name | Type
------------ | -------------
`id` | number
`username` | string
`name` | string
`email` | string
`groups` | Array&lt;string&gt;
`admin` | boolean

## Example

```typescript
import type { AuthUser } from ''

// TODO: Update the object below with actual values
const example = {
  "id": 42,
  "username": johndoe,
  "name": John Doe,
  "email": john.doe@example.com,
  "groups": ["Users","Admin Group"],
  "admin": true,
} satisfies AuthUser

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as AuthUser
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


