# Product Requirement Document: Favorite Stop Line Filtering

## Overview

Allow users to select specific lines for each favorite stop, filtering arrival times shown on the home screen. When opening the stop detail
screen, all lines continue to be displayed regardless of favorite line selections.

## Goals

- Enable granular control over which lines appear in favorite stop cards on the home screen
- Maintain backward compatibility with existing favorites (all lines selected by default)
- Persist line selections both locally and on the server
- Provide intuitive UI for selecting/deselecting lines in the Edit Favorites screen

## Implementation Plan

### Step 1: Data Model Foundation

Add support for line selections in the local database with proper defaults to ensure existing functionality remains unchanged.

**Files to modify:**

- `app/src/main/java/com/sloy/sevibus/data/database/Entities.kt`
- `app/src/main/java/com/sloy/sevibus/data/database/SevibusDao.kt`

#### Tasks

- [x] **Update FavoriteStopEntity**
    - Add `selectedLineIds: List<LineId>?` field (nullable for backward compatibility)
    - Default to `null` (all lines selected)

- [x] **Create database migration**
    - Add new column `selectedLineIds TEXT` to favorites table
    - Existing rows will have `NULL` value (representing all lines selected)
    - Test migration with existing data

- [x] **Update DAO**
    - Ensure all queries fetch the new field
    - No changes needed to method signatures (queries will automatically include new column)

#### Verification

- App compiles and runs
- Existing favorites continue to work normally
- Database inspector shows new column with NULL values for existing favorites

---

### Step 2: Domain Layer Updates

Update domain models and repositories to handle line selections while maintaining backward compatibility.

**Files to modify:**

- `app/src/main/java/com/sloy/sevibus/domain/model/FavoriteStop.kt`
- `app/src/main/java/com/sloy/sevibus/domain/repository/FavoriteRepository.kt`
- `app/src/main/java/com/sloy/sevibus/data/repository/LocalFavoriteRepository.kt`
- `app/src/main/java/com/sloy/sevibus/data/repository/RemoteAndLocalFavoriteRepository.kt`

#### Tasks

- [x] **Update FavoriteStop domain model**
    - Add `selectedLineIds: Set<LineId>?` field
    - Add helper method `fun isLineSelected(lineId: LineId): Boolean` that handles three cases:
        - `null` → returns `true` (all lines selected)
        - `isEmpty()` → returns `false` (no lines selected)
        - otherwise → returns `contains(lineId)` (specific lines selected)

- [x] **Update FavoriteRepository interface**
    - No method signature changes needed - existing methods accept `FavoriteStop`
    - `addFavorite(stop: FavoriteStop)` will handle the new field
    - `replaceFavorites(favorites: List<FavoriteStop>)` will handle the new field

- [x] **Update LocalFavoriteRepository**
    - Update entity ↔ domain model mapping to include `selectedLineIds`
    - Convert `List<LineId>?` (entity) ↔ `Set<LineId>?` (domain)
    - Handle three cases:
        - `null` → `null` (all lines selected)
        - Empty list → empty set (no lines selected)
        - Non-empty list → non-empty set (specific lines selected)

- [x] **Update RemoteAndLocalFavoriteRepository**
    - Update mapping to include `selectedLineIds`
    - Ensure sync logic handles the new field

#### Verification

- App compiles and runs
- Domain models correctly represent line selections
- Data flows through repository without breaking existing functionality

---

### Step 3: Edit Favorites UI - Line Selection

Implement the UI for selecting/deselecting lines in the Edit Favorites screen.

**Files to modify:**

- `app/src/main/java/com/sloy/sevibus/feature/foryou/favorites/edit/EditFavoritesScreen.kt`
- `app/src/main/java/com/sloy/sevibus/feature/foryou/favorites/edit/EditFavoritesViewModel.kt`

#### Tasks

- [x] **Update EditFavoritesScreen UI state**
    - The screen already maintains local state: `var favoritesLocalList by remember(state) { mutableStateOf(state.favorites) }`
    - Add `onLineSelectionChanged: (Set<LineId>) -> Unit` callback to `EditFavoriteListItem` (similar to existing `onNameChanged` and
      `onIconChanged`)
    - Implement the callback following the existing pattern:
      ```kotlin
      onLineSelectionChanged = { selectedLines ->
          favoritesLocalList = favoritesLocalList.toMutableList().apply {
              set(
                  indexOfFirst { it.stop.code == favorite.stop.code },
                  favorite.copy(selectedLineIds = selectedLines)
              )
          }
      }
      ```
    - Initialize line selections: When rendering `EditFavoriteListItem`, if `favorite.selectedLineIds == null`, treat all lines from
      `favorite.stop.lines` as selected
    - The updated favorites will be saved via existing `onSaveClick(favoritesLocalList)` when user taps "Guardar"

- [x] **Implement line selection UI component**
    - Line indicators already exist at lines 334-352 in `EditFavoritesScreen.kt` using `FlowRow`
    - Currently ALL lines show checkmarks (non-interactive)
    - Make the existing line indicators interactive:
        - Track selected lines in local state within `EditFavoriteListItem` (initialize from `favorite.selectedLineIds` or all lines if
          null)
        - Wrap each `Box` with `Surface(onClick = { ... })` to make it clickable
        - On click, toggle the line in/out of selected set, then call `onLineSelectionChanged(updatedSet)`
        - Allow unchecking all lines (empty set is valid and means "no lines selected")
    - Update visual state based on selection:
        - Selected: Show `LineIndicator` with checkmark (existing code at lines 336-350)
        - Unselected: Show `LineIndicator` with `Modifier.alpha(0.4f)` and NO checkmark
    - Example toggle logic:
      ```kotlin
      Surface(onClick = {
          val isSelected = selectedLines.contains(line.id)
          if (isSelected) {
              onLineSelectionChanged(selectedLines - line.id)
          } else {
              onLineSelectionChanged(selectedLines + line.id)
          }
      }) {
          Box {
              LineIndicator(
                  line,
                  Modifier
                      .defaultMinSize(32.dp, 32.dp)
                      .alpha(if (selectedLines.contains(line.id)) 1f else 0.4f)
              )
              if (selectedLines.contains(line.id)) {
                  Icon(Icons.Rounded.Check, /* ... existing checkmark code ... */)
              }
          }
      }
      ```

- [x] **Add analytics tracking**
  - Add new event to `Clicks.kt`:
    ```kotlin
    data class EditFavoriteLineClicked(val isSelected: Boolean) : SevEvent(
        "Edit Favorite Line Clicked",
        "isSelected" to isSelected
    )
    ```
  - Track event when user toggles a line in the Edit Favorites screen
  - Call from the toggle handler before calling `onLineSelectionChanged`
  - Pass analytics through `EditFavoriteListItem` composable (add `onTrack: (SevEvent) -> Unit` parameter)
  - Wire analytics from `EditFavoritesViewModel` (inject `Analytics` dependency)

- [x] **Verify save logic**
    - No changes needed - existing flow already handles the new field
    - When user taps "Guardar", `onSaveClick(favoritesLocalList)` is called
    - This calls `viewModel.onFavoritesChanged(updatedFavorites)` which calls `favoriteRepository.replaceFavorites(favorites)`
    - The updated `selectedLineIds` in each favorite will be persisted automatically

#### Verification

- App compiles and runs
- Edit Favorites screen shows line selection UI
- Selected/unselected visual states work correctly
- Line selections persist when navigating away and returning
- Can deselect all lines (empty set is valid)
- Saved selections are persisted to database
- Analytics event `EditFavoriteLineClicked` is tracked when toggling lines

---

### Step 4: Home Screen Filtering

Apply line selections to filter arrival times displayed on the home screen favorite cards.

**Files to modify:**

- `app/src/main/java/com/sloy/sevibus/feature/foryou/favorites/FavoriteItemViewModel.kt`
- `app/src/main/java/com/sloy/sevibus/feature/foryou/favorites/FavoriteListItem.kt`

#### Tasks

- [x] **Update FavoriteItemViewModel filtering logic**
    - In the flow that fetches arrivals, handle three cases based on `favorite.selectedLineIds`:
        - If `selectedLineIds == null`: Fetch and show all arrivals (backward compatibility)
        - If `selectedLineIds.isEmpty()`: Skip arrivals API call entirely, emit empty state
        - If `selectedLineIds` is non-empty: Fetch arrivals and filter to only show arrivals where `arrival.line.id` is in the selected set
    - Apply filtering AFTER `.onePerLine()` or adjust `.onePerLine()` to accept selected lines
    - When skipping API call for empty selection, emit state without loading indicator (no shimmer effect)

- [x] **Update FavoriteListItem UI**
    - Display only arrivals for selected lines
    - Handle two types of empty states:
        - No lines selected (`selectedLineIds.isEmpty()`): Show favorite card without arrivals section
        - No arrivals available for selected lines: Show appropriate empty message
    - Show line indicators only for selected lines (or all lines if null, or no indicators if empty)

- [ ] **Verify stop detail screen unchanged**
    - Confirm StopDetailScreen shows ALL lines regardless of favorite selections
    - Ensure navigation from favorite card to stop detail works correctly

#### Verification

- App compiles and runs
- Home screen shows only arrivals for selected lines
- Favorites with all lines selected (null) continue to work as before
- Favorites with no lines selected (empty set) show card without arrivals and skip API call
- Stop detail screen shows all lines when opened
- Empty states handled gracefully:
    - No lines selected: Card visible, no arrivals section, no API call
    - No arrivals available for selected lines: Appropriate empty message

---

### Step 5: API Integration

Extend the API to support storing and retrieving line selections, ensuring server-side persistence and sync.

**Files to modify:**

- `app/src/main/java/com/sloy/sevibus/data/api/model/ApiModels.kt`
- `app/src/main/java/com/sloy/sevibus/data/api/SevibusUserApi.kt`
- `app/src/main/java/com/sloy/sevibus/data/repository/RemoteAndLocalFavoriteRepository.kt`

#### Tasks

- [x] **Update FavoriteStopDto**
    - Add `selectedLineIds: List<LineId>?` field
    - Ensure backward compatibility (server should handle `null` as all lines selected)
    - LineId will serialize as String automatically

- [x] **Update API endpoints (if needed)**
    - Existing endpoints should automatically handle the new field
    - `POST /favorites` - accepts updated DTO
    - `PUT /favorites/{stop}` - accepts updated DTO
    - `GET /favorites` - returns updated DTO
    - Coordinate with backend team if manual API changes required

- [x] **Update RemoteAndLocalFavoriteRepository mapping**
    - Map `selectedLineIds` between domain model and API DTO
    - Convert `Set<LineId>?` (domain) ↔ `List<LineId>?` (DTO)
    - Handle server responses with/without line selections
    - Implement proper error handling

- [ ] **Test sync logic**
    - Ensure line selections sync on favorites upload
    - Handle conflict resolution (local vs server state)
    - Test with existing favorites (should upgrade gracefully)
    - Existing server-side favorites without field → default to all lines

#### Verification

- App compiles and runs
- Line selections persist across app restarts
- Line selections sync to server
- Changes on one device appear on other devices after sync
- Existing favorites migrate correctly

---

## Data Model Specifications

### Local Database (Room Entity)

**File:** `app/src/main/java/com/sloy/sevibus/data/database/Entities.kt`

```kotlin
@Entity(tableName = "favorites")
data class FavoriteStopEntity(
    @PrimaryKey val stopId: StopId,
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val order: Int = 0,
    val selectedLineIds: List<LineId>? = null
)
```

### Domain Model

**File:** `app/src/main/java/com/sloy/sevibus/domain/model/FavoriteStop.kt`

```kotlin
data class FavoriteStop(
    val stop: Stop,
    val customName: String?,
    val customIcon: CustomIcon?,
    val selectedLineIds: Set<LineId>? = null
) {
    fun isLineSelected(lineId: LineId): Boolean {
        return when {
            selectedLineIds == null -> true
            selectedLineIds.isEmpty() -> false
            else -> selectedLineIds.contains(lineId)
        }
    }
}
```

### API DTO

**File:** `app/src/main/java/com/sloy/sevibus/data/api/model/ApiModels.kt`

```kotlin
@Serializable
data class FavoriteStopDto(
    val stopId: StopId,
    val customName: String? = null,
    val customIcon: CustomIcon? = null,
    val order: Int = 0,
    val selectedLineIds: List<LineId>? = null
)
```

## Edge Cases & Considerations

### Backward Compatibility

- Existing favorites: `selectedLineIds = null` → all lines selected
- Server doesn't return field: treat as `null` → all lines selected
- Empty selection allowed: `selectedLineIds = emptySet()` → no lines selected, skip arrivals API call

### UI/UX

- Default state: All lines selected for new favorites
- Edit screen: Clear visual distinction between selected/unselected, allow unchecking all lines
- Home screen: Three states to handle:
    - All lines selected (null): Show all arrivals
    - Specific lines selected: Show filtered arrivals
    - No lines selected (empty): Show card without arrivals, skip API call
- Stop detail: Always shows all lines (unaffected by selections)

### Data Consistency

- Line IDs must match exactly between `stop.lines` and `selectedLineIds`
- Invalid line IDs in selections should be filtered out
- If all selected lines become invalid after filtering, treat as empty set (no arrivals)

### Performance

- Filter arrivals efficiently in ViewModel (avoid extra network calls)
- Skip API calls entirely for favorites with no lines selected (performance benefit)
- 20-second polling interval remains unchanged for favorites with selected lines
- Consider impact on home screen load time with many favorites

## Success Criteria

- [ ] Users can select/deselect lines in Edit Favorites screen
- [ ] Home screen shows only selected lines' arrival times
- [ ] Stop detail screen shows all lines regardless of selections
- [ ] Line selections persist locally and sync to server
- [ ] Existing favorites continue working (all lines selected by default)
- [ ] App remains stable and performant
- [ ] No regressions in existing favorite functionality

## Non-Goals

- Filtering in stop detail screen (out of scope)
- Line selection in other contexts (e.g., search results)
- Bulk selection/deselection UI (could be future enhancement)
- Analytics tracking for line selection patterns (could be added later)
