
# SearchResult


## Properties

Name | Type
------------ | -------------
`domain` | [SearchType](SearchType.md)
`subtype` | [MaterialType](MaterialType.md)
`id` | string
`label` | string

## Example

```typescript
import type { SearchResult } from ''

// TODO: Update the object below with actual values
const example = {
  "domain": null,
  "subtype": null,
  "id": null,
  "label": null,
} satisfies SearchResult

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as SearchResult
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


