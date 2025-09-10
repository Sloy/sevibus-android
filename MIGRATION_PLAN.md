### Migration Plan: PathDto to Polyline Format

#### Current State Analysis

- [X] `PathDto` currently has `points: List<PositionDto>`
- [X] `PathEntity` stores `points: List<PositionDto>`
- [X] Database version is currently 7
- [X] Domain model `Path` uses `points: List<Position>`

---

### Migration Tasks

#### 1. Add Polyline Type Definition

- [x] Create `typealias Polyline = String` in the `domain.model` package.

#### 2. Create Custom Polyline Decoder

- [x] Create a new file `PolylineUtils.kt` in the `infrastructure` package.
- [x] Implement the custom `decode(polyline: String): List<Position>` function based on the Kotlin gist.
- [x] Add an extension function `Polyline.toPositions(): List<Position>` that calls `decode`.

#### 3. Update API Data Transfer Objects and Mapping Functions

- [x] Modify `PathDto` in `ApiModels.kt`:
  - [x] Replace `points: List<PositionDto>` with `polyline: Polyline`.
- [x] Update mapping functions in `Entities.kt` to convert between polyline and points:
  - [x] `PathDto.fromDto()`: Use `polyline.toPositions()` to convert polyline to Position list for domain model
  - [x] `PathDto.toEntity()`: Convert polyline to points list for database storage using
    `polyline.toPositions().map { PositionDto(it.latitude, it.longitude) }`
  - [x] Keep `PathEntity` unchanged (still stores `points: List<PositionDto>`)

#### 4. Update API Calls to Request Polyline Format

- [x] Update API implementation to always append `?format=polyline` query parameter
- [x] No changes needed to `SevibusApi` interface methods (keep existing signatures)
- [x] API will return `polyline` property when `format=polyline` is sent

#### 5. Update Test Files for Polyline Format

- [ ] Modify test data in `FakeSevibusApi.kt` to use polyline strings instead of points arrays
- [ ] Update `RemoteAndLocalPathRepositoryTest.kt` test expectations to use polyline format
- [ ] Update `StubPathRepository.kt` to use polyline format if it creates PathDto objects

#### 6. Database Schema Changes (Later Step)

**Note: This will be done in a separate step to maintain app usability during the transition**

- [ ] Modify `PathEntity` in `Entities.kt`:
    - [ ] Replace `points: List<PositionDto>` with `polyline: Polyline`.
- [ ] Increment the database version from 7 to 8 in `SevibusDatabase.kt`.
- [ ] Add a manual migration from 7 to 8 that executes: `DELETE FROM paths`.
- [ ] Remove `PositionListConverter` from `TypeConverters.kt` and the `@TypeConverters` annotation in `SevibusDatabase.kt`.
- [ ] Update entity mapping functions to use polyline storage directly.

---

## Benefits of This Approach

- **Clean API Migration**: API uses polyline format only, no dual format complexity
- **Database Compatibility**: Database continues using points format until step 6
- **Production Safe**: App remains functional throughout the migration process
- **Storage Efficiency**: Polyline is decoded once and stored as points (avoiding repeated decoding)
- **API Flexibility**: API can roll out polyline support gradually with feature flag via query parameter
- **Gradual Migration**: Database schema changes are separated into a later step

## Migration Flow

1. Deploy app with polyline API support and points database storage (steps 1-5)
2. API team enables polyline support with `?format=polyline` query parameter
3. App automatically starts using efficient polyline format from API
4. Later, in step 6, we can optimize database storage to use polyline strings directly
