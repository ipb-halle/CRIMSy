# SearchApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**search**](SearchApi.md#searchoperation) | **POST** /search | Unified search endpoint |



## search

> SearchResponse search(searchRequest)

Unified search endpoint

Performs search across Materials, Items, Experiments and Documents.   If \&quot;Materials\&quot; is included in searchTypes, materialTypes may be provided  (Structure, Sequence, Biomaterial, Composition).        All values are currently treated as string (for being simple as possible). 

### Example

```ts
import {
  Configuration,
  SearchApi,
} from '';
import type { SearchOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: SessionKeyAuthentication
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new SearchApi(config);

  const body = {
    // SearchRequest
    searchRequest: ...,
  } satisfies SearchOperationRequest;

  try {
    const data = await api.search(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **searchRequest** | [SearchRequest](SearchRequest.md) |  | |

### Return type

[**SearchResponse**](SearchResponse.md)

### Authorization

[SessionKeyAuthentication](../README.md#SessionKeyAuthentication)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Search results |  -  |
| **400** | Invalid search request |  -  |
| **401** | Unauthorized |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

