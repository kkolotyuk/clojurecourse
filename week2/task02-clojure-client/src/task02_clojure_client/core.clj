(ns task02-clojure-client.core
  (:gen-class)
  (:require [clojure.java.io :as io]
            [task02-clojure-client.helpers :refer :all])
  (:import [java.net Socket InetSocketAddress]))

(def queries
  (concat (take 100
                (cycle ["select student"
                        "select student where id = 10"
                        "select student where id = 10 limit 2"
                        "select student where id = 10 order by id limit 2"
                        "select student order by id limit 2 join subject on id = id"
                        "select student order by year"
                        "select student where id > 1"]))
          ["quit"]))

(defn send-queries []
  (let [sock-addr (InetSocketAddress. "localhost" 9997)
        sock (doto (Socket.)
                   (.connect sock-addr))]
    (with-open [sock sock
                rd (io/reader (.getInputStream sock))
                wr (io/writer (.getOutputStream sock))]
      (doseq [query queries]
        (.write wr query 0 (count query))
        (.newLine wr)))))

(defn -main [& args]
  (println "Starting client. Connecting to server on port " 9997)
  (dotimes [_ 10] (future (send-queries))))
;;   (doall (pcalls
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries
;;           send-queries)))
