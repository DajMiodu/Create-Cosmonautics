--@name     Thruster Control
--@category Actuators
--@input    id
--@input    thrust

-- Control a liquid-fuel thruster.
-- id     = peripheral UUID string or list index (1-based)
-- thrust = thrust level (0.0 to 1.0)
local id_in = input("id")
local thrust = input("thrust")

local function get_id(val)
    if type(val) == "number" or (type(val) == "string" and tonumber(val)) then
        local idx = tonumber(val)
        local ids = getPeripheralIds()
        return ids[idx]
    end
    return val
end

local target_id = get_id(id_in)
if target_id and target_id ~= "" then
    writePeripheral(target_id, "thrust", thrust)
end
