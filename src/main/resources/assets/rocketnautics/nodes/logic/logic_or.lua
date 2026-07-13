--@name     OR
--@category Logic
--@input    A
--@input    B
--@output   Out

-- Outputs 1 if either A or B are non-zero.
local a = input("A")
local b = input("B")

output("Out", (a ~= 0 or b ~= 0) and 1 or 0)
