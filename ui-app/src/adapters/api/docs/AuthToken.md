
# AuthToken


## Properties

Name | Type
------------ | -------------
`token` | string
`expiresInSeconds` | number

## Example

```typescript
import type { AuthToken } from ''

// TODO: Update the object below with actual values
const example = {
  "token": 550e8400-e29b-41d4-a716-446655440000,
  "expiresInSeconds": 3600,
} satisfies AuthToken

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as AuthToken
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


