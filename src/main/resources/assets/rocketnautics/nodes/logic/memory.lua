--@name     Memory
--@category Logic
--@input    In
--@input    Save
--@output   Out

-- Stores the 'In' value when 'Save' is non-zero and outputs it constantly.
if _G.mem_val == nil then _G.mem_val = 0 end

local v = input("In")
local save = input("Save")

if save ~= 0 then
    _G.mem_val = v
end

output("Out", _G.mem_val)
