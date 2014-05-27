(ns listio.server
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [compojure.handler :refer [site]]
            [ring.util.response :refer [resource-response
                                        redirect
                                        content-type]]
            [clj-oauth2.client :as oauth2]
            [ring.middleware.edn :refer [wrap-edn-params]]
            [listio.github :as github]
            [carica.core :refer [config]]))

(defn edn-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(def github-oauth2
  {:authorization-uri "https://github.com/login/oauth/authorize"
   :access-token-uri "https://github.com/login/oauth/access_token"
   :client-id (config :client-id)
   :client-secret (config :client-secret)
   :scope ["public_repo"]
   :access-query-param :access_token
   :grant-type "authorization_code"})

(def auth-req (oauth2/make-auth-request github-oauth2 "csrf-protection-jfhH4krP"))

(defn fetch-access-token [params]
  (:access-token (oauth2/get-access-token github-oauth2 params auth-req)))

(defroutes handler
  (GET "/" [] (content-type (resource-response "index.html" {:root "public"}) "text/html"))
  (GET "/authorize" [] (redirect (:uri auth-req)))
  (GET "/callback" request
       (let [access-token (fetch-access-token (:params request))
             user (github/fetch-user access-token)
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
                        :avatar-url avatar-url
                        :username username
                        :github-url github-url})))
  (GET "/logout" request
       (let [resp (edn-response {:logout true})]
         (update-in resp [:session] dissoc :access-token :avatar-url :username :github-url)))
  (GET "/issues/:username/:reponame" [username reponame]
       (if-let [issues (github/fetch-open-issues username reponame)]
         (if (empty? issues)
           (edn-response {:success false
                          :message "There are no open issues in this repository"})
           (edn-response {:success true
                          :issues issues}))
         (edn-response {:success false
                        :message "Repository not found"})))
  (POST "/issues/replace" {:keys [params session] :as request}
        (let [access-token (:access-token session)
              username (:username params)
              repo (:repo params)
              number (:number params)
              box (:box params)]
          (edn-response (github/to-the-box access-token username repo number box))))
  (route/resources "/"))

(def app
  (-> handler
      site
      wrap-edn-params))
