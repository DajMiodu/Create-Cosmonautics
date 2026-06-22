--@name     Environment
--@category Sensors
--@output   light
--@output   temperature
--@output   biome

-- Ambient environment data at the sputnik's current location.
output("light",       getLightLevel())
output("temperature", getTemperature())
output("biome",       0) -- getBiome() returns string, use Lua Script node to branch on it
