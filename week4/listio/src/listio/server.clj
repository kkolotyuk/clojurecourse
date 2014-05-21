(ns listio.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [resource-response
                                        redirect
                                        response]]
            [tentacles.core :refer [api-call]]
            [clj-oauth2.client :as oauth2]
            [ring.middleware.json :refer [wrap-json-response]]))

(def github-oauth2
  {:authorization-uri "https://github.com/login/oauth/authorize"
   :access-token-uri "https://github.com/login/oauth/access_token"
   :redirect-uri "http://127.0.0.1:4000/callback"
   :client-id "61ae969d2a43e3038c0c"
   :client-secret "c05d6f0813ec1f6d3ec772a37f2798efb9b6a678"
   :scope ["public_repo"]
   :access-query-param :access_token
   :grant-type "authorization_code"})

(def auth-req (oauth2/make-auth-request github-oauth2 "csrf-protection-jfhH4krP"))

(defn fetch-access-token [params]
  (oauth2/get-access-token github-oauth2 params auth-req))

(defroutes handler
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/authorize" [] (redirect (:uri auth-req)))
  (GET "/callback" request (let [access-token (fetch-access-token (:params request))
                                 response (redirect "/")]
                             (assoc-in response [:session :access-token] access-token)))
  (GET "/is-authenticated" {{access-token :access-token} :session} (response {:access-token (not (nil? access-token))}))
  (route/resources "/"))

(def app
  (-> handler
      site
      wrap-json-response))
