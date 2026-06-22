--@name     Position
--@category Sensors
--@output   x
--@output   y
--@output   z

-- Global world-space position in metres.
local x, y, z = getPosition()
output("x", x)
output("y", y)
output("z", z)
