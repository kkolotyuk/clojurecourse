(ns task02.Database
  (:require [task02.db :refer :all]
            [task02.query :refer :all])

  ;; Объявить класс task02.Database с двумя статическими функциями доступными из Java:
  ;;  - void InitDatabase() - должна выполнять начальную загрузку данных используя функцию
  ;;       task02.db/load-initial-data
  ;;  - String Select(String query) - должна выполнять запрос к базе данных
  ;;       (task02.query/perform-query) и возвращать результат в виде строки в формате EDN.

  ;; Hint: load-initial-data, pr-str, perform-query
  (:gen-class
   :methods [[InitDatabase [] void]
             [Select [String] String]]))

(defn ^static -InitDatabase [this]
  (load-initial-data))

(defn ^static -Select [this ^String query]
  (pr-str (perform-query query)))
