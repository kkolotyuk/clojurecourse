(ns listio.core
  (:require [compojure.core :refer :all]))

(defroutes app
  (GET "/" [] "<h1>Hello World</h1>"))

