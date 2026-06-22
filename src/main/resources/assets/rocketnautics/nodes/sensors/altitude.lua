--@name     Altitude Sensor
--@category Sensors
--@output   m

-- Reads the current altitude (Y position) of the sputnik in world-space metres.
local alt = getAltitude()
output("m", alt)
