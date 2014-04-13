(ns dsl.core
  (:use clojure.walk))

(def cal (java.util.Calendar/getInstance))
(def today (java.util.Date.))
(def yesterday (do (.add cal java.util.Calendar/DATE -1) (.getTime cal)))
(def tomorrow (do (.add cal java.util.Calendar/DATE 2) (.getTime cal)))

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

(defn comp? [code]
  (and (list? code)
       (= (count code) 3)
       ('#{< > = <= >=} (first code))))

(defn date? [d]
  (instance? java.util.Date d))

(defn smrt-comparison [op x y]
  (if (and (date? x) (date? y))
    (op (.getTime x) (.getTime y))
    (op x y)))

(declare replace-comp replace-symbol-expression)

(defn replace-symbol-comp [code]
  (cond
    (not (seq? code)) code
    (comp? code) '(smrt-comparison (first code) (second code) (nnext code))
    :else (replace-comp code)))

(defn replace-comp [code]
  (if (seq code)
    (cons (replace-symbol-comp (first code))
          (replace-comp (rest code)))
    ()))

(replace-comp '((and (< 1 2))))

;; Режим Бога -- никаких подсказок.
;; Вы его сами выбрали ;-)
(defmacro with-datetime [& code]
  )



