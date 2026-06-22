--@name     Vector Control
--@category Actuators
--@input    id
--@input    thrust
--@input    pitch
--@input    yaw

-- Control a vector thruster's gimbal and thrust level.
local id_in = input("id")
local thrust = input("thrust")
local pitch  = input("pitch")
local yaw    = input("yaw")

local function get_id(val)
    if type(val) == "number" or (type(val) == "string" and tonumber(val)) then
        return getPeripheralIds()[tonumber(val)]
    end
    return val
end

local target_id = get_id(id_in)
if target_id and target_id ~= "" then
    writePeripheralValues(target_id, "gimbal", pitch, yaw)
    writePeripheral(target_id, "thrust", thrust)
end
