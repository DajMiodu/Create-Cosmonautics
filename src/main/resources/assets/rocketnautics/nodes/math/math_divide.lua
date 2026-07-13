--@name     Divide
--@category Math
--@input    A
--@input    B
--@output   Out

-- Divide A by B. Avoids division by zero.
local a = input("A")
local b = input("B")
output("Out", b ~= 0 and (a / b) or 0)
