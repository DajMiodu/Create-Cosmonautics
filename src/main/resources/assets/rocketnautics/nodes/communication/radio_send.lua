--@name     Radio Send
--@category Communication
--@input    Channel
--@input    Code
--@input    Value

-- Broadcasts a value with a specific code over the specified radio channel.
local channel = input("Channel")
local code = input("Code")
local val = input("Value")

if channel ~= 0 then
    sendPacket(channel, code, val)
end
