(ns task02.query
  (:require [task02.helpers :refer :all]
            [task02.db :refer :all])
  (:require [clojure.core.match :refer [match]]
            [clojure.string :as str]))

;; Case-insensitive keywords checkers
(defn select? [s] (= (str/lower-case s) "select"))
(defn limit? [s] (= (str/lower-case s) "limit"))
(defn where? [s] (= (str/lower-case s) "where"))
(defn order? [s] (= (str/lower-case s) "order"))
(defn by? [s] (= (str/lower-case s) "by"))
(defn join? [s] (= (str/lower-case s) "join"))
(defn on? [s] (= (str/lower-case s) "on"))

(def str->op {"=" =
              "!=" not=
              "<" <
              ">" >
              "<=" <=
              ">=" >=})

(defn make-where-function [column-string op-string value-string]
  (let [column (keyword column-string)
        op (str->op op-string)
        tmp (seq (re-matches #"'(.*)'" value-string))
        value (cond
             tmp (second tmp)
             :else (parse-int value-string))]
    (fn [m]
      (when-let [v (column m)]
        (op v value)))))

;; Функция выполняющая парсинг запроса переданного пользователем
;;
;; Синтаксис запроса:
;; SELECT table_name [WHERE column comp-op value] [ORDER BY column] [LIMIT N] [JOIN other_table ON left_column = right_column]
;;
;; - Имена колонок указываются в запросе как обычные имена, а не в виде keywords. В
;;   результате необходимо преобразовать в keywords
;; - Поддерживаемые операторы WHERE: =, !=, <, >, <=, >=
;; - Имя таблицы в JOIN указывается в виде строки - оно будет передано функции get-table для получения объекта
;; - Значение value может быть либо числом, либо строкой в одинарных кавычках ('test').
;;   Необходимо вернуть данные соответствующего типа, удалив одинарные кавычки для строки.
;;
;; - Ключевые слова --> case-insensitive
;;
;; Функция должна вернуть последовательность со следующей структурой:
;;  - имя таблицы в виде строки
;;  - остальные параметры которые будут переданы select
;;
;; Если запрос нельзя распарсить, то вернуть nil

;; Примеры вызова:
;; > (parse-select "select student")
;; ("student")
;; > (parse-select "select student where id = 10")
;; ("student" :where #<function>)
;; > (parse-select "select student where id = 10 limit 2")
;; ("student" :where #<function> :limit 2)
;; > (parse-select "select student where id = 10 order by id limit 2")
;; ("student" :where #<function> :order-by :id :limit 2)
;; > (parse-select "select student where id = 10 order by id limit 2 join subject on id = sid")
;; ("student" :where #<function> :order-by :id :limit 2 :joins [[:id "subject" :sid]])
;; > (parse-select "werfwefw")
;; nil
(defn parse-select [^String sel-string]
  (loop [res [] splitted-s (str/split sel-string #" ")]
    (match splitted-s
      [] res

      [(_ :guard select?) table & rst]
      (recur (conj res table) rst)

      [(_ :guard limit?) n & rst]
      (recur (concat res [:limit (parse-int n)]) rst)

      [(_ :guard where?) op1 op op2 & rst]
      (recur (concat res [:where (make-where-function op1 op op2)]) rst)

      [(_ :guard order?) (_ :guard by?) column & rst]
      (recur (concat res [:order-by (keyword column)]) rst)

      [(_ :guard join?) other_table (_ :guard on?) left_column "=" right_column & rst]
      (recur (concat res
                     [:joins [[(keyword left_column) other_table (keyword right_column)]]])
             rst)

      :else nil)))

;; Выполняет запрос переданный в строке.  Бросает исключение если не удалось распарсить запрос

;; Примеры вызова:
;; > (perform-query "select student")
;; ({:id 1, :year 1998, :surname "Ivanov"} {:id 2, :year 1997, :surname "Petrov"} {:id 3, :year 1996, :surname "Sidorov"})
;; > (perform-query "select student order by year")
;; ({:id 3, :year 1996, :surname "Sidorov"} {:id 2, :year 1997, :surname "Petrov"} {:id 1, :year 1998, :surname "Ivanov"})
;; > (perform-query "select student where id > 1")
;; ({:id 2, :year 1997, :surname "Petrov"} {:id 3, :year 1996, :surname "Sidorov"})
;; > (perform-query "not valid")
;; exception...
(defn perform-query [^String sel-string]
  (if-let [query (parse-select sel-string)]
    (apply select (get-table (first query)) (rest query))
    (throw (IllegalArgumentException. (str "Can't parse query: " sel-string)))))
