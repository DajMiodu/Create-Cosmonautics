Celestial positions are the primary way that locations are defined in the universe. They are by default relative to the parent body of whatever object is using the celestial position.

### Fixed
A fixed position.

**`type`** (required): `fixed`

**`position`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) offset from the parent.

### Full Orbit
A fully configurable orbit that can be elliptical but tends to be inconvenient to work with.

**`type`** (required): `full_orbit`

**`coordinate_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial position.

**`position`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) position of the celestial position at `coordinate_date`.

**`velocity`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) velocity of the celestial position at `coordinate_date`.

### Circular Orbit -- Position
A circular orbit defined by a position.

**`type`** (required): `circular_orbit_position`

**`coordinate_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial position.

**`position`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) position of the celestial position at `coordinate_date`.

**`orbit_axis`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) axis the orbit rotates around. Negate this vector to negate the velocity.

### Circular Orbit -- Period
A circular orbit defined by a period.

**`type`** (required): `circular_orbit_period`

**`coordinate_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial position.

**`position_direction`** (optional, defaults to an arbitrary vector): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) direction that the celestial position should be in at `coordinate_date`. Magnitude is ignored.

**`orbit_axis`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) axis the orbit rotates around. Negate this vector to negate the velocity.

**`period_seconds`** (required): The number of seconds it takes for the celestial position to complete an orbit around the parent.