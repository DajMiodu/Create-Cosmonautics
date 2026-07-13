--@name     AND
--@category Logic
--@input    A
--@input    B
--@output   Out

-- Outputs 1 if both A and B are non-zero.
local a = input("A")
local b = input("B")

output("Out", (a ~= 0 and b ~= 0) and 1 or 0)
