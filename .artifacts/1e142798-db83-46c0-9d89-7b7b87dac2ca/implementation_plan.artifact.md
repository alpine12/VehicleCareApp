# Fix Type Inference and Mismatch Errors in HomeViewModel.kt

The user reported an error on line 50 of `HomeViewModel.kt`. Analysis shows multiple type-related errors in the `loadData` function, primarily caused by inconsistent numeric types (`Int` vs `Long`) which lead to inference failures in the `combine` flow.

## Analysis
- **Line 50 (`combine`)**: Inference fails because of errors inside the lambda.
- **Line 62 (`sumOf`)**: `it.cost ?: 0L` results in `Number` (mixing `Int?` and `Long`), but `sumOf` expects a specific primitive type (`Int`, `Long`, etc.).
- **Line 74 (`lastServiceKmMap`)**: Trying to put an `Int` (`kilometer`) into a `MutableMap<Long, Long>`.
- **Line 78 (`Triple`)**: Inference failure for the `Triple` components due to the issues above.
- **Line 91 (`collect`)**: Destructuring fails because the upstream type could not be inferred.

## Proposed Changes

### [Component: ViewModel]

#### [MODIFY] [HomeViewModel.kt](file:///E:/PROJECT ANDROID/VehicleCareApp/app/src/main/java/id/alpine12/vehiclecare/ui/viewmodel/HomeViewModel.kt)
Update the `loadData` function to explicitly convert `Int` values to `Long` where necessary to maintain type consistency.

```kotlin
// In HomeViewModel.kt, loadData() function

// 1. Fix cost sum
val costThisMonth = servicesThisMonth.sumOf { it.cost?.toLong() ?: 0L }

// 2. Fix kilometer map assignment
if (lastOliService != null) {
    lastServiceKmMap[vehicle.id] = lastOliService.kilometer.toLong()
}
```

## Verification Plan

### Manual Verification
- Run `analyze_file` on `HomeViewModel.kt` after the changes to ensure all errors are resolved.
- Verify that the `HomeUiState` is updated correctly with the long values.
