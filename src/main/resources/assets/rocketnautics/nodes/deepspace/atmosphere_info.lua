--@name     Atmosphere Info
--@category Deep Space
--@output   in_atmosphere
--@output   distance

-- Detects whether the sputnik is inside a planet's atmosphere.
-- in_atmosphere = 1 if inside atmospheric transition height, 0 otherwise
-- distance      = metres above the planet surface
output("in_atmosphere", isInAtmosphere())
output("distance",      getDeepSpaceDistanceToPlanet())
