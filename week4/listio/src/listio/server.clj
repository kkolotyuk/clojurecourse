(ns listio.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer (resource-response)]))

(defroutes app
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/"))

