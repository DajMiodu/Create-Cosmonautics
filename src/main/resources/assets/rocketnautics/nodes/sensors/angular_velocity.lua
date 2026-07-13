--@name     Angular Velocity
--@category Sensors
--@output   wx
--@output   wy
--@output   wz

-- Angular velocity around each axis in degrees/second.
local wx, wy, wz = getAngularVelocity()
output("wx", wx)
output("wy", wy)
output("wz", wz)
