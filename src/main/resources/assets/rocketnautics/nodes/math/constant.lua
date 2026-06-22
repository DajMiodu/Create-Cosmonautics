--@name     Constant
--@category Math
--@output   Out

-- Инициализируем текстовое поле один раз и сохраняем в глобальное окружение ноды
if _G.tf == nil then
    _G.tf = ui.textfield(60)
    _G.tf.setValue("0")
end

-- Добавляем элемент на ноду (безопасно вызывать каждый тик)
addElement(_G.tf)

-- Читаем значение из поля и конвертируем в число
local val = tonumber(_G.tf.getValue()) or 0

-- Выводим
output("Out", val)
