--@name     Compare
--@category Logic
--@input    A
--@input    B
--@input    Mode (0: >, 1: <, 2: ==, 3: >=, 4: <=)
--@output   Out

-- Compares A and B based on Mode. Outputs 1 if true, 0 if false.
local a = input("A")
local b = input("B")
local mode = math.floor(input("Mode (0: >, 1: <, 2: ==, 3: >=, 4: <=)"))

local res = false
if mode == 0 then res = (a > b)
elseif mode == 1 then res = (a < b)
elseif mode == 2 then res = (a == b)
elseif mode == 3 then res = (a >= b)
elseif mode == 4 then res = (a <= b)
end

output("Out", res and 1 or 0)
