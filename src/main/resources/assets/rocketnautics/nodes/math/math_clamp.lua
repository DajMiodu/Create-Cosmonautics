--@name     Clamp
--@category Math
--@input    In
--@input    Min
--@input    Max
--@output   Out

-- Constrains a value between Min and Max.
local v = input("In")
local mn = input("Min")
local mx = input("Max")

local out = v
if out < mn then out = mn end
if out > mx then out = mx end

output("Out", out)
