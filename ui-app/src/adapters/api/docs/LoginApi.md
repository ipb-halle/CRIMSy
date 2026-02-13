# LoginApi

All URIs are relative to *https://compchem17.ipb-halle.de/ui/rest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**login**](LoginApi.md#loginoperation) | **POST** /auth/login | Log in a user |



## login

> AuthResponse login(loginRequest)

Log in a user

Authenticates a user with the provided login credentials.

### Example

```ts
import {
  Configuration,
  LoginApi,
} from '';
import type { LoginOperationRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new LoginApi();

  const body = {
    // LoginRequest | User login credentials
    loginRequest: ...,
  } satisfies LoginOperationRequest;

  try {
    const data = await api.login(body);
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
| **loginRequest** | [LoginRequest](LoginRequest.md) | User login credentials | |

### Return type

[**AuthResponse**](AuthResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Successful login |  -  |
| **401** | Unauthorized login attempt |  -  |
| **400** | Invalid input or missing parameters |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

