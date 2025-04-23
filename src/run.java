import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;

public class run {

    // Вспомогательный метод проверки возможности размещения гостей
    public static boolean checkCapacity(int maxCapacity, List<Map<String, String>> guests) {

        List<Event> events = new ArrayList<>();
        int currentGuests = 0;

        // Заполняем список событий
        for (Map<String, String> guest : guests) {
            String checkIn = guest.get("check-in");
            String checkOut = guest.get("check-out");

            // Проверяем на корректность ввода даты
            if (!isWrongDate(checkIn, checkOut)) {
                events.add(new Event(checkIn, Event.Type.CHECK_IN));
                events.add(new Event(checkOut, Event.Type.CHECK_OUT));
            }
        }

        // Сортировка
        Collections.sort(events, comparator());

        // Обрабатываем события
        for (Event event : events) {
            currentGuests = changeCurrentGuests(event.getType().toString(), currentGuests);

            if (currentGuests > maxCapacity) {
                return false;
            }
        }

        return true;
    }

    // Вспомогательный метод для проверки корректности ввода даты
    private static boolean isWrongDate(String checkIn, String checkOut) {
        String regex = ".*[^0-9-].*";
        if (checkIn.isBlank() || checkIn.matches(regex) || checkOut.matches(regex)) {
            System.out.println("Введена неверная дата. Введите дату по формату \"YYYY-MM-DD\". Гостей с неверно указанными датами заезда/выезда не заселяем!");
            return true;
        } else if (Integer.parseInt(checkIn.replaceAll("-", "")) > Integer.parseInt(checkOut.replaceAll("-", ""))) {
            System.out.println("Дата выезда не может быть раньше даты заезда. Гостей с неверно указанными датами заезда/выезда не заселяем!");
            return true;
        }
        return false;
    }

    // Вспомогательный метод сортировки
    private static Comparator<Event> comparator() {
        // Сортируем события по дате, если даты равны, выезд должен быть раньше заезда
        return (o1, o2) -> {
            if (o1.getDate().equals(o2.getDate())) {
                return -o1.getType().toString().compareTo(o2.getType().toString());
            }
            return o1.getDate().compareTo(o2.getDate());
        };
    }

    // Вспомогательный метод для изменения текущего количества гостей
    private static int changeCurrentGuests(String event, int currentGuests) {
        switch (event) {
            case "CHECK_IN":
                currentGuests++;
                break;
            case "CHECK_OUT":
                currentGuests--;
                break;
        }

        return currentGuests;
    }

    // Классический POJO-класс
    private static class Event {
        // Вспомогательный вложенный класс для представления типа события
        private enum Type {
            CHECK_IN,
            CHECK_OUT
        }

        private final Type type;
        private final String date;

        public Event(String date, Type type) {
            this.date = date;
            this.type = type;
        }

        public String getDate() {
            return date;
        }

        public Type getType() {
            return type;
        }
    }

    // Вспомогательный метод для парсинга JSON строки в Map
    private static Map<String, String> parseJsonToMap(String json) {
        Map<String, String> map = new HashMap<>();
        // Удаляем фигурные скобки
        json = json.substring(1, json.length() - 1);

        // Разбиваем на пары ключ-значение
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim().replace("\"", "");
            String value = keyValue[1].trim().replace("\"", "");
            map.put(key, value);
        }

        return map;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Первая строка - вместимость гостиницы
        int maxCapacity = Integer.parseInt(scanner.nextLine());

        // Вторая строка - количество записей о гостях
        int n = Integer.parseInt(scanner.nextLine());

        List<Map<String, String>> guests = new ArrayList<>();

        // Читаем n строк, json-данные о посещении
        for (int i = 0; i < n; i++) {
            String jsonGuest = scanner.nextLine();
            // Простой парсер JSON строки в Map
            Map<String, String> guest = parseJsonToMap(jsonGuest);
            guests.add(guest);
        }

        // Вызов функции
        boolean result = checkCapacity(maxCapacity, guests);

        // Вывод результата
        System.out.println(result ? "True" : "False");

        scanner.close();
    }
}