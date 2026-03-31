# UsersApi

All URIs are relative to *https://compchem17.ipb-halle.de/ui/rest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getUsersList**](UsersApi.md#getuserslist) | **GET** /users | Retrieve list of all users |



## getUsersList

> PaginatedUserResponse getUsersList(page, pageSize)

Retrieve list of all users

Returns all users from the system with basic details. Useful for populating user lists in the frontend. 

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { GetUsersListRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new UsersApi(config);

  const body = {
    // number | Page number (starts from 1) (optional)
    page: 56,
    // number | Number of users per page (optional)
    pageSize: 56,
  } satisfies GetUsersListRequest;

  try {
    const data = await api.getUsersList(body);
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
| **page** | `number` | Page number (starts from 1) | [Optional] [Defaults to `1`] |
| **pageSize** | `number` | Number of users per page | [Optional] [Defaults to `10`] |

### Return type

[**PaginatedUserResponse**](PaginatedUserResponse.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Array of users |  -  |
| **401** | Unauthorized (missing or invalid token) |  -  |
| **500** | Internal server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

