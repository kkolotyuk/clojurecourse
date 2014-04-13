(ns dsl.core
  (:use clojure.walk)
  (:import [java.util Calendar]))

(def cal (Calendar/getInstance))
(def today (java.util.Date.))
(def yesterday (do (.add cal Calendar/DATE -1) (.getTime cal)))
(def tomorrow (do (.add cal Calendar/DATE 2) (.getTime cal)))

(comment
  (defn one [] 1)

  ;; Примеры вызова
  (with-datetime
    (if (> today tomorrow) (println "Time goes wrong"))
    (if (<= yesterday today) (println "Correct"))
    (let [six (+ 1 2 3)
          d1 (today - 2 days)
          d2 (today + 1 week)
          d3 (today + six months)
          d4 (today + (one) year)]
      (if (and (< d1 d2)
               (< d2 d3)
               (< d3 d4))
        (println "DSL works correctly")))))

(defn date? [d]
  (instance? java.util.Date d))

(def op-sym '#{+ -})

(def metric-sym '#{day days week weeks month months year years
                   hour hours minute minutes second seconds})

(def metric-map {'day Calendar/DATE
                 'days Calendar/DATE
                 'week Calendar/WEEK_OF_YEAR
                 'weeks Calendar/WEEK_OF_YEAR
                 'month Calendar/MONTH
                 'months Calendar/MONTH
                 'year Calendar/YEAR
                 'years Calendar/YEAR
                 'hour Calendar/HOUR_OF_DAY
                 'hours Calendar/HOUR_OF_DAY
                 'minute Calendar/MINUTE
                 'minutes Calendar/MINUTE
                 'second Calendar/SECOND
                 'seconds Calendar/SECOND})

(defn op? [code]
  (and (list? code)
       (= (count code) 4)
       (op-sym (second code))
       (metric-sym (last code))))

(defn op->int [op n]
  (if (= op '+)
    n
    (- n)))

(defn smrt-op [date op n metric]
  (doto (Calendar/getInstance)
        (.setTime date)
        (.add (metric-map metric) (op->int op n))))

(def comp-sym '#{< > = <= >=})

(defn comp? [code]
  (and (list? code)
       (= (count code) 3)
       (comp-sym (first code))))

(defn smrt-comparison [op x y]
  (if (and (date? x) (date? y))
    (op (.getTime x) (.getTime y))
    (op x y)))

(defn- disp [x]
  (cond
    (comp? x) :comp
    (op? x) :operation
    (coll? x) :collection
    :else :scalar))

(defmulti replace-expr disp)

(defmethod replace-expr :collection [code]
  (lazy-seq
    (when (seq code)
      (cons (replace-expr (first code))
            (replace-expr (rest code))))))

(defmethod replace-expr :comp [code]
  (println :comp)
  `(smrt-comparison ~(first code) ~(second code) ~(last code)))

(defmethod replace-expr :operation [code]
  (println :operation)
  `(smrt-op ~(first code) ~(second code) ~(nth code 2) ~(last code)))

(defmethod replace-expr :scalar [code] code)

;; Режим Бога -- никаких подсказок.
;; Вы его сами выбрали ;-)
(defmacro with-datetime [& code]
  (let [replaced-code (map replace-expr code)]
    `(do ~@replaced-code)))

;;(replace-expr '(> today yesterday))
;;(disp '(today + 1 day))
;;(replace-expr '(today + 1 day))

(defn replace-expr-1 [code]
  `(smrt-op ~(first code) ~(second code) ~(nth code 2) ~(last code)))
(replace-expr-1 '(today + 1 day))

;;(macroexpand-1 '(with-datetime (today - 2 days)))
;;(macroexpand-1 '(with-datetime (> today yesterday)))
