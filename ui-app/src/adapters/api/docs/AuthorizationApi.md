# AuthorizationApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**getRoleInfo**](AuthorizationApi.md#getroleinfo) | **GET** /me | Get role and group information of the current user |



## getRoleInfo

> RoleResponse getRoleInfo()

Get role and group information of the current user

Returns the authenticated user\&#39;s username, group memberships, and whether the user belongs to the Admin Group. 

### Example

```ts
import {
  Configuration,
  AuthorizationApi,
} from '';
import type { GetRoleInfoRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: SessionKeyAuthentication
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new AuthorizationApi(config);

  try {
    const data = await api.getRoleInfo();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**RoleResponse**](RoleResponse.md)

### Authorization

[SessionKeyAuthentication](../README.md#SessionKeyAuthentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Role and group information retrieved successfully |  -  |
| **401** | Unauthorized (missing or invalid token) |  -  |
| **404** | User not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

