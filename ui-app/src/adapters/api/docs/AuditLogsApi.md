# AuditLogsApi

All URIs are relative to *https://compchem17.ipb-halle.de/ui/rest*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**auditLogsGet**](AuditLogsApi.md#auditlogsget) | **GET** /audit/logs | Retrieve audit logs |



## auditLogsGet

> Array&lt;AuditLog&gt; auditLogsGet()

Retrieve audit logs

Returns system activity logs for compliance and debugging. Logs include user actions and administrative events. 

### Example

```ts
import {
  Configuration,
  AuditLogsApi,
} from '';
import type { AuditLogsGetRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const config = new Configuration({ 
    // Configure HTTP bearer authorization: bearerAuth
    accessToken: "YOUR BEARER TOKEN",
  });
  const api = new AuditLogsApi(config);

  try {
    const data = await api.auditLogsGet();
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

[**Array&lt;AuditLog&gt;**](AuditLog.md)

### Authorization

[bearerAuth](../README.md#bearerAuth)

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Audit log list |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

