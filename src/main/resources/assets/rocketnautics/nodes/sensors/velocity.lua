--@name     Velocity
--@category Sensors
--@output   vx
--@output   vy
--@output   vz
--@output   speed

-- Read 3D velocity vector and scalar speed (m/s).
local vx, vy, vz = getVelocity()
local speed = math.sqrt(vx*vx + vy*vy + vz*vz)
output("vx", vx)
output("vy", vy)
output("vz", vz)
output("speed", speed)
