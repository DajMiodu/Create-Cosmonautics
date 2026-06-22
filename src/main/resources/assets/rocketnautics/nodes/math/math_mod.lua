--@name     Modulo
--@category Math
--@input    A
--@input    B
--@output   Out

-- Remainder of division of A by B.
local a = input("A")
local b = input("B")

output("Out", b ~= 0 and (a % b) or 0)
