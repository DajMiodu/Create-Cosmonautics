--@name     Lerp
--@category Math
--@input    A
--@input    B
--@input    T
--@output   Out

-- Linear interpolation between A and B based on T (0.0 to 1.0).
local a = input("A")
local b = input("B")
local t = input("T")

output("Out", a + (b - a) * t)
