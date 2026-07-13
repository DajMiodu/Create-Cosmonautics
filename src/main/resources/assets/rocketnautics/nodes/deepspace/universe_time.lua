--@name     Universe Time
--@category Deep Space
--@output   ticks

-- Total elapsed ticks in the Deep Space universe simulation.
-- Monotonically increasing — useful for PID integrals or timers.
output("ticks", getUniverseTime())
