
# PaginatedUserResponse


## Properties

Name | Type
------------ | -------------
`items` | [Array&lt;UserSummary&gt;](UserSummary.md)
`totalItems` | number
`totalPages` | number
`currentPage` | number

## Example

```typescript
import type { PaginatedUserResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "items": null,
  "totalItems": 245,
  "totalPages": 13,
  "currentPage": 1,
} satisfies PaginatedUserResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PaginatedUserResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


