(ns task02.db
  "Namespace for database-related data & functions..."
  (:require [clojure.data.csv :as csv]
            [clojure.string :as str]
            [task02.helpers :refer :all]))

;; helper functions

(defn table-keys [tbl]
  (mapv keyword (first tbl)))

(defn key-value-pairs [tbl-keys tbl-record]
  (interleave tbl-keys tbl-record))

(def data-record zipmap)

(defn data-table [tbl]
  (map (partial data-record (table-keys tbl)) (next tbl)))

(defn str-field-to-int [field rec]
  (update-in rec [field] parse-int))

;; Заменяет строковые значения полей на целочисленные, если это возможно
(defn str-fields-to-int [rec]
  (into rec
        (for [[k v] rec
              :when (seq (re-matches #"\d+" v))]
          [k (parse-int v)])))

;; Место для хранения данных - используйте atom/ref/agent/...
(def tables (ref {}))

;; возвращает содержимое таблицы по строковому имени
(defn get-table [^String tb-name]
  (let [tb-key (keyword (str/lower-case tb-name))]
    (tb-key @tables)))

(defn create-table [name data]
  (dosync
    (alter tables
           assoc
           (keyword name)
           (->> (data-table data)
                (map #(str-fields-to-int %))))))

(defn create-table-from-csv [name]
  (if-let [data (csv/read-csv (slurp (str name ".csv")))]
    (create-table name data)))

(defn drop-table [table]
  (dosync
    (alter tables dissoc (keyword table))))

;;; Данная функция загружает начальные данные из файлов .csv
;;; и сохраняет их в изменяемой переменной tables
(defn load-initial-data []
  (dosync
    (ref-set tables {})
    (create-table-from-csv "student")
    (create-table-from-csv "subject")
    (create-table-from-csv "student_subject")))

;; select-related functions...
(defn where* [data condition-func]
  (if condition-func
    (filter condition-func data)
    data))

(defn limit* [data lim]
  (if lim
    (take lim data)
    data))

(defn order-by* [data column]
  (if column
    (sort-by column data)
    data))

(defn join* [data1 column1 data2 column2]
  (for [left data1
        right data2
        :when (= (column1 left) (column2 right))]
    (merge right left)))

;; Внимание! в отличии от task01, в этом задании 2-я таблица передается в виде строки с именем таблицы!
(defn perform-joins [data joins]
  (if (empty? joins)
    data
    (let [[col1 data2 col2] (first joins)]
      (recur (join* data col1 (get-table data2) col2)
             (next joins)))))

;; Данная функция аналогична функции в первом задании за исключением параметра :joins как описано выше.
;; примеры использования с измененым :joins:
;;   (select student-subject :joins [[:student_id "student" :id] [:subject_id "subject" :id]])
;;   (select student-subject :limit 2 :joins [[:student_id "student" :id] [:subject_id "subject" :id]])
(defn select [data & {:keys [where limit order-by joins]}]
  (-> data
      (perform-joins joins)
      (where* where)
      (order-by* order-by)
      (limit* limit)))

;; insert/update/delete...

;; Данная функция должна удалить все записи соответствующие указанному условию
;; :where. Если :where не указан, то удаляются все данные.
;;
;; Аргумент data передается как мутабельный объект (использование зависит от выбора ref/atom/agent/...)
;;
;; Примеры использования
;;   (delete student) -> []
;;   (delete student :where #(= (:id %) 1)) -> все кроме первой записи
(defn delete [table & {:keys [where]}]
  (let [k-table (keyword table)]
    (dosync
      (if-let [_ (contains? @tables k-table)]
        (if where
          (alter tables
                 update-in
                 [k-table]
                 (partial remove where))
          (alter tables
                 update-in
                 [k-table]
                 (identity [])))
        (throw (IllegalArgumentException. str("Cannot delete unknown table " table)))))))

;; Данная функция должна обновить данные в строках соответствующих указанному предикату
;; (или во всей таблице).
;;
;; - Аргумент data передается как мутабельный объект (использование зависит от выбора ref/atom/agent/...)
;; - Новые данные передаются в виде map который будет объединен с данными в таблице.
;;
;; Примеры использования:
;;   (update student {:id 5})
;;   (update student {:id 6} :where #(= (:year %) 1996))
(defn update [table upd-map & {:keys [where]}]
  (let [k-table (keyword table)
        where (if where
                where
                (identity true))
        mmerge #(if (where %)
                  (merge % upd-map)
                  %)]
    (dosync
      (if (contains? @tables k-table)
        (alter tables
               update-in
               [k-table]
               #(map mmerge %))
        (throw (IllegalArgumentException. str("Cannot update in unknown table " table)))))))

;; Вставляет новую строку в указанную таблицу
;;
;; - Аргумент data передается как мутабельный объект (использование зависит от выбора ref/atom/agent/...)
;; - Новые данные передаются в виде map
;;
;; Примеры использования:
;;   (insert student {:id 10 :year 2000 :surname "test"})
(defn insert [table new-entry]
  (let [k-table (keyword table)]
    (dosync
      (if (contains? @tables k-table)
        (alter tables
               update-in
               [k-table]
               conj
               new-entry)
        (throw (IllegalArgumentException. str("Cannot insert into unknown table " table)))))))
