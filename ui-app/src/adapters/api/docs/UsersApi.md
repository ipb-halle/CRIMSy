# UsersApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**usersGet**](UsersApi.md#usersget) | **GET** /users | Get list of users |



## usersGet

> PagedUsers usersGet(page, pageSize)

Get list of users

Returns all users for admin, or own info for non-admin users

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { UsersGetRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: SessionKeyAuthentication
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new UsersApi(config);

  const body = {
    // number | Page number (1-based) (optional)
    page: 56,
    // number | Number of users per page (optional)
    pageSize: 56,
  } satisfies UsersGetRequest;

  try {
    const data = await api.usersGet(body);
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
| **page** | `number` | Page number (1-based) | [Optional] [Defaults to `1`] |
| **pageSize** | `number` | Number of users per page | [Optional] [Defaults to `3`] |

### Return type

[**PagedUsers**](PagedUsers.md)

### Authorization

[SessionKeyAuthentication](../README.md#SessionKeyAuthentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successful response |  -  |
| **400** | Bad request |  -  |
| **401** | Unauthorized |  -  |
| **403** | Forbidden |  -  |
| **500** | Internal server error |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

