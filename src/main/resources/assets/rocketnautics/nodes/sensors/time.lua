--@name     World Time
--@category Sensors
--@output   ticks
--@output   is_day

-- World time in ticks (0-24000 per day) and is_day flag (1=day, 0=night).
local t = getWorldTime()
local day_ticks = t % 24000
local is_day = (day_ticks >= 0 and day_ticks < 13000) and 1 or 0
output("ticks",  day_ticks)
output("is_day", is_day)
