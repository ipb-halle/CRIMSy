# UsersApi

All URIs are relative to *https://compchem17.ipb-halle.de/ui/rest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**createUser**](UsersApi.md#createuser) | **POST** /users | Create a new user |
| [**deleteUser**](UsersApi.md#deleteuser) | **DELETE** /users/{id} | Delete a user |
| [**getUsersList**](UsersApi.md#getuserslist) | **GET** /users | Retrieve list of all users |
| [**updateUser**](UsersApi.md#updateuser) | **PUT** /users/{id} | Update an existing user |



## createUser

> UserSummary createUser(userSummary)

Create a new user

Allows admin to add a new user

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { CreateUserRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new UsersApi(config);

  const body = {
    // UserSummary | User information to create
    userSummary: ...,
  } satisfies CreateUserRequest;

  try {
    const data = await api.createUser(body);
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
| **userSummary** | [UserSummary](UserSummary.md) | User information to create | |

### Return type

[**UserSummary**](UserSummary.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User created successfully |  -  |
| **400** | Invalid request |  -  |
| **401** | Unauthorized |  -  |
| **409** | User already exists |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## deleteUser

> DeleteUser200Response deleteUser(id)

Delete a user

Allows admin to remove a user

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { DeleteUserRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new UsersApi(config);

  const body = {
    // number
    id: 56,
  } satisfies DeleteUserRequest;

  try {
    const data = await api.deleteUser(body);
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
| **id** | `number` |  | [Defaults to `undefined`] |

### Return type

[**DeleteUser200Response**](DeleteUser200Response.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User deleted successfully |  -  |
| **401** | Unauthorized |  -  |
| **404** | User not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


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


## updateUser

> UserSummary updateUser(id, userSummary)

Update an existing user

Allows admin to edit user information

### Example

```ts
import {
  Configuration,
  UsersApi,
} from '';
import type { UpdateUserRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new UsersApi(config);

  const body = {
    // number
    id: 56,
    // UserSummary | Update user info
    userSummary: ...,
  } satisfies UpdateUserRequest;

  try {
    const data = await api.updateUser(body);
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
| **id** | `number` |  | [Defaults to `undefined`] |
| **userSummary** | [UserSummary](UserSummary.md) | Update user info | |

### Return type

[**UserSummary**](UserSummary.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | User updated successfully |  -  |
| **400** | Invalid request |  -  |
| **401** | Unauthorized |  -  |
| **404** | User not found |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

