
# SearchRequest


## Properties

Name | Type
------------ | -------------
`searchTypes` | [Array&lt;SearchType&gt;](SearchType.md)
`query` | string
`materialTypes` | [Array&lt;MaterialType&gt;](MaterialType.md)
`page` | number
`pageSize` | number

## Example

```typescript
import type { SearchRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "searchTypes": null,
  "query": benzin,
  "materialTypes": null,
  "page": null,
  "pageSize": null,
} satisfies SearchRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SearchRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


