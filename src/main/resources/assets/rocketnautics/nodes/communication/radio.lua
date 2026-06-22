--@name     Radio
--@category Communication
--@input    Freq
--@output   Channel

-- Registers this node on a frequency to interact with the network.
local freq = input("Freq")
output("Channel", freq)

-- The underlying system automatically handles packets, but you must register the frequency.
registerFrequency(freq)

-- In custom logic, you can check packets:
-- local val = receivePacket(freq, 100)
