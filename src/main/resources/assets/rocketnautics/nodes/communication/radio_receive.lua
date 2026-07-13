--@name     Radio Receive
--@category Communication
--@input    Channel
--@input    Code
--@output   Value

-- Retrieves the last value broadcast on the specified channel with the given code.
local channel = input("Channel")
local code = input("Code")

local val = receivePacket(channel, code)
output("Value", val)
