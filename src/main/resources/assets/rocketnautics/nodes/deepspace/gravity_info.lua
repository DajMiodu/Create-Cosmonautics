--@name     Gravity Info
--@category Deep Space
--@output   gravity
--@output   parent_radius
--@output   distance

-- Gravitational environment around the current parent body.
-- gravity      = gravitational acceleration at current position (m/s²)
-- parent_radius = equatorial radius of the orbited body (m)
-- distance     = distance from surface to sputnik (m)
output("gravity",       getDeepSpaceGravity())
output("parent_radius", getDeepSpaceParentRadius())
output("distance",      getDeepSpaceDistanceToPlanet())
