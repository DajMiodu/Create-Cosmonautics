--@name     Timer
--@category Logic
--@input    Run
--@input    Reset
--@output   Seconds

-- Counts up in seconds while Run is non-zero. Resets if Reset is non-zero.
-- To persist state between ticks, we store it in the global environment (unique to this node).
if _G.timer_val == nil then _G.timer_val = 0 end

local run = input("Run")
local rst = input("Reset")

if rst ~= 0 then
    _G.timer_val = 0
elseif run ~= 0 then
    _G.timer_val = _G.timer_val + 0.05 -- 1 tick = 0.05 seconds
end

output("Seconds", _G.timer_val)
