--@name     Orbit Info
--@category Deep Space
--@output   sma
--@output   eccentricity
--@output   inclination
--@output   period
--@output   speed

-- Keplerian orbital elements of the current orbit.
-- sma         = semi-major axis in metres
-- eccentricity = 0.0 (circular) .. 1.0+ (hyperbolic escape)
-- inclination = orbital inclination in degrees
-- period      = orbital period in seconds (NaN if escape trajectory)
-- speed       = current orbital speed in m/s
local sma, ecc, inc, period, speed = getDeepSpaceOrbitalData()
output("sma",         sma)
output("eccentricity", ecc)
output("inclination", inc)
output("period",      period)
output("speed",       speed)
