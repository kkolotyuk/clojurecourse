(ns dsl.core
  (:use clojure.walk)
  (:import [java.util Calendar]))

(def cal (Calendar/getInstance))
(def today (java.util.Date.))
(def yesterday (do (.add cal Calendar/DATE -1) (.getTime cal)))
(def tomorrow (do (.add cal Calendar/DATE 2) (.getTime cal)))

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
  (if (= op '+) n (- n)))

(defn smrt-op [date op n metric]
  (.getTime (doto (Calendar/getInstance)
                  (.setTime date)
                  (.add (metric-map metric) (op->int op n)))))

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
  (when (seq code)
    (let [res (cons (replace-expr (first code))
                    (replace-expr (rest code)))]
      (if (vector? code) ;; handle let case and other vectors in code
        (vec res)
        res))))

(defmethod replace-expr :comp [code]
  `(smrt-comparison ~(first code) ~(second code) ~(last code)))

(defmethod replace-expr :operation [code]
  `(smrt-op ~(first code) '~(second code) ~(nth code 2) '~(last code)))

(defmethod replace-expr :scalar [code] code)

;; Режим Бога -- никаких подсказок.
;; Вы его сами выбрали ;-)
(defmacro with-datetime [& code]
  (let [replaced-code (map replace-expr code)]
    `(do ~@replaced-code)))
