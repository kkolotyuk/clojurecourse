(ns task02.network
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [task02.helpers :refer :all]
            [task02.query :refer :all]
            [clojure.tools.logging :refer (info error)])
  (:import [java.net Socket ServerSocket InetAddress InetSocketAddress SocketTimeoutException]))

;; Объявить переменную для синхронизации между потоками. Воспользуйтесь promise
(def ^:private should-be-finished (promise))

;; Hint: *in*, *out*, io/writer, io/reader, socket.getOutputStream(), socket.getInputStream(), socket.close(), binding
;;       deliver, prn
(defn handle-request [^Socket sock]
  (with-open [sock sock
              rd (io/reader (.getInputStream sock))
              wr (io/writer (.getOutputStream sock))]
    (binding [*in* rd *out* wr] ;; переопределить *in* & *out* чтобы они указывали на входной и выходной потоки сокета
      (try
        (loop [s (read-line)] ;; считать данные из переопределенного *in*
          (info s)
          (case s
            "shutdown" (deliver should-be-finished true)
            "quit" 1
            nil 1
            (do
              (prn (perform-query s))
              (recur (read-line)))))
        (catch Throwable ex
          (println "Exception: " ex))))))


;; Hint: future, deliver
(defn- run-loop [server-sock]
  (try
    (let [^Socket sock (.accept server-sock)]
      ;; выполнить функцию handle-request в отдельном потоке
      (println "Socket connected")
      (future (handle-request sock)))
    (catch SocketTimeoutException ex)
    (catch Throwable ex
      (println "Got exception" ex)
      ;; сообщить основному потоку что мы завершаем выполнение
      ;; для этого необходимо установить переменную should-be-finished в true
      (deliver should-be-finished true))))

(defn run [port]
  (let [sock-addr (InetSocketAddress. nil port)
        server-socket (doto (ServerSocket.)
                        (.setReuseAddress true)
                        (.setSoTimeout 3000)
                        (.bind sock-addr))]
    (loop [_ (run-loop server-socket)]
      (when-not (realized? should-be-finished) ;; следующий запрос если работа не завершается...
        (recur (run-loop server-socket))))
    (.close server-socket)))
