--@name     Constant
--@category Math
--@output   Out

-- Initialize the text field once and store it in the node's global environment
if _G.tf == nil then
    _G.tf = ui.textfield(60)
    _G.tf.setValue("0")
end

-- Add the element to the node (safe to call every tick)
addElement(_G.tf)

-- Read the value from the field and convert it to a number
local val = tonumber(_G.tf.getValue()) or 0

-- Output the value
output("Out", val)
