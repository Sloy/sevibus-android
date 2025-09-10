### Migration Plan: PathDto to Polyline Format

#### Current State Analysis

- [X] `PathDto` currently has `points: List<PositionDto>`
- [X] `PathEntity` stores `points: List<PositionDto>`
- [X] Database version is currently 7
- [X] Domain model `Path` uses `points: List<Position>`

---

### Migration Tasks

#### 1. Add Polyline Type Definition

- [ ] Create `typealias Polyline = String` in the `domain.model` package.

#### 2. Create Custom Polyline Decoder

- [ ] Create a new file `PolylineUtils.kt` in the `domain/model` package.
- [ ] Implement the custom `decode(polyline: String): List<Position>` function.
- [ ] Add an extension function `Polyline.toPositions(): List<Position>` that calls `decode`.

#### 3. Update API Data Transfer Objects

- [ ] Modify `PathDto` in `ApiModels.kt`:
    - [ ] Replace `points: List<PositionDto>` with `polyline: Polyline`.

#### 4. Update Database Schema

- [ ] Modify `PathEntity` in `Entities.kt`:
    - [ ] Replace `points: List<PositionDto>` with `polyline: Polyline`.
- [ ] Increment the database version from 7 to 8 in `SevibusDatabase.kt`.
- [ ] Add a manual migration from 7 to 8 that executes: `DELETE FROM paths`.
- [ ] Remove `PositionListConverter` from `TypeConverters.kt` and the `@TypeConverters` annotation in `SevibusDatabase.kt`.

#### 5. Update Entity Mapping Functions

- [ ] Update `PathEntity.fromEntity()` to use `polyline.toPositions()` for the domain model.
- [ ] Update `PathDto.fromDto()` to use `polyline.toPositions()` for the domain model.
- [ ] Update `PathDto.toEntity()` to store the `polyline` string directly.

#### 6. Update Test Files

- [ ] Modify test data in `FakeSevibusApi.kt` to use polyline strings.
- [ ] Update `RemoteAndLocalPathRepositoryTest.kt` test expectations.
- [ ] Update `StubPathRepository.kt` if it creates `PathDto` objects.
