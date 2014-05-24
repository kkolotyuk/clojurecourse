(ns listio.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [resource-response
                                        redirect
                                        response]]
            [tentacles.core :refer [api-call]]
            [tentacles.users :as users]
            [clj-oauth2.client :as oauth2]
            [ring.middleware.edn :refer [wrap-edn-params]]))

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

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
  (:access-token (oauth2/get-access-token github-oauth2 params auth-req)))

(defn github-user [access-token]
  (users/me {:oauth_token access-token}))

(defroutes handler
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (GET "/authorize" [] (redirect (:uri auth-req)))
  (GET "/callback" request
       (let [access-token (fetch-access-token (:params request))
             user (github-user access-token)
             user-info {:access-token access-token
                        :avatar-url (:avatar_url user)
                        :username (:login user)
                        :github-url (:html_url user)}
             response (redirect "/")]
         (update-in response [:session] merge user-info)))
  (GET "/is-authenticated" {{:keys [access-token avatar-url username github-url]} :session}
       (if (nil? access-token)
         (edn-response {:authenticated? false})
         (edn-response {:authenticated? true
                        :access-token access-token
                        :avatar-url avatar-url
                        :username username
                        :github-url github-url})))
  (GET "/foo" [] (str (github-user)))
  (route/resources "/"))

(def app
  (-> handler
      site
      wrap-edn-params))
