(ns listio.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [resource-response
                                        redirect]]
            [tentacles.core :refer [api-call]]))

(def client-id "61ae969d2a43e3038c0c")
(def client-secret "c05d6f0813ec1f6d3ec772a37f2798efb9b6a678")
(def state "hgF24h5kl")
(def scopes "public_repo")
(def auth-url (format "https://github.com/login/oauth/authorize?client_id=%s&state=%s&scope=%s"
                      client-id
                      state
                      scopes))

(defn authenticate [code]
  (api-call :post "https://github.com/login/oauth/access_token" nil {:client_id client-id :client_secret client-secret :code code}))

(defroutes handler
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/authorize" [] (redirect auth-url))
  (GET "/callback" [code] (str (authenticate code)))
  (route/resources "/"))

(def app
  (-> handler
      site))
