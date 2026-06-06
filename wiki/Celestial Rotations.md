Celestial rotations are the primary way that orientations are defined in the universe.

### Velocity
A rotation defined by an angular velocity.

**`type`** (required): `velocity`

**`starting_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial rotation.

**`starting_rotation`** (optional): The [Rotation](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Rotation) of the celestial rotation at `starting_date`.

**`velocity`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) angular velocity in rad/s of the celestial rotation.

### Period
A rotation defined by the period.

**`type`** (required): `period`

**`starting_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial rotation.

**`starting_rotation`** (optional): The [Rotation](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Rotation) of the celestial rotation at `starting_date`.

**`rotation_axis`** (required): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) axis of the celestial rotation.

**`period_seconds`** (required): The number of seconds it takes for the celestial rotation to complete a rotation.

### Spin-Orbit Resonance
A rotation defined by resonance with the orbit period.

**`type`** (required): `tidal_lock`

**`starting_date`** (optional): The [AbsoluteDate](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#AbsoluteDate) of the celestial rotation.

**`starting_rotation`** (optional): The [Rotation](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Rotation) of the celestial rotation at `starting_date`.

**`rotation_axis`** (optional, defaults to the orbit axis): The [Vector3D](https://github.com/CosmonauticsTeam/Create-Cosmonautics/wiki/Common-Types#Vector3D) axis of the celestial rotation.

**`rotations_per_orbit`** (required): The number of rotations completed every orbit. Can be less than one.
