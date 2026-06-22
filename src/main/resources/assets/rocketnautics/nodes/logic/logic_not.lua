--@name     NOT
--@category Logic
--@input    In
--@output   Out

-- Outputs 1 if In is 0, otherwise outputs 0.
local v = input("In")

output("Out", (v == 0) and 1 or 0)
