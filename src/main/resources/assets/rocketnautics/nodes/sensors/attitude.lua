--@name     Attitude
--@category Sensors
--@output   pitch
--@output   yaw
--@output   roll

-- Reads pitch, yaw, roll orientation angles in degrees.
local pitch, yaw, roll = getOrientation()
output("pitch", pitch)
output("yaw",   yaw)
output("roll",  roll)
