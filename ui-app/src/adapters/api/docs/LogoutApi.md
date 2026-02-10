# LogoutApi

All URIs are relative to *http://localhost*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**logout**](LogoutApi.md#logout) | **POST** /auth/logout | Logout the current user |



## logout

> LogoutResponse logout()

Logout the current user

Invalidates the user\&#39;s session and deletes the token from database.

### Example

```ts
import {
  Configuration,
  LogoutApi,
} from '';
import type { LogoutRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: SessionKeyAuthentication
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new LogoutApi(config);

  try {
    const data = await api.logout();
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

[**LogoutResponse**](LogoutResponse.md)

### Authorization

[SessionKeyAuthentication](../README.md#SessionKeyAuthentication)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successfuly loged out |  -  |
| **401** | Unauthorized request (invalid or missing token) |  -  |
| **400** | Invalid request |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

