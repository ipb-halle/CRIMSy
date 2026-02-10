
# PagedUsers


## Properties

Name | Type
------------ | -------------
`totalUsers` | number
`totalPages` | number
`currentPage` | number
`users` | [Array&lt;User&gt;](User.md)

## Example

```typescript
import type { PagedUsers } from ''

// TODO: Update the object below with actual values
const example = {
  "totalUsers": null,
  "totalPages": null,
  "currentPage": null,
  "users": null,
} satisfies PagedUsers

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PagedUsers
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


