--@name     Select
--@category Logic
--@input    Condition
--@input    True Val
--@input    False Val
--@output   Out

-- If Condition is non-zero, outputs True Val, otherwise False Val.
local cond = input("Condition")
local tv = input("True Val")
local fv = input("False Val")

output("Out", cond ~= 0 and tv or fv)
