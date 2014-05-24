(ns my.namespace
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET]])
  (:require-macros [enfocus.macros :as em]))

(em/defsnippet header :compiled "public/prototype/login.html" ["#header"] [])
(em/defsnippet login-form :compiled "public/prototype/login.html" ["#login-form"] [])
(em/defsnippet menu :compiled "public/prototype/main.html" ["#menu"]
  [avatar-url github-url username]
  ".avatar" (ef/set-attr :src avatar-url)
  ".username" (ef/do-> (ef/set-attr :href github-url)
                       (ef/content username)))

(defn ^:export login []
  (ef/at "body"
         (ef/do-> (ef/content (header))
                  (ef/append (login-form)))))

(defn ^:export main [avatar-url github-url username]
  (ef/at "body"
         (ef/do-> (ef/content (header))
                  (ef/append (menu avatar-url github-url username)))))

(defn ^:export console-handler [data]
  (.log js/console (str data)))

(defn ^:export show-start [{:keys [authenticated? avatar-url github-url username]}]
  (if authenticated?
    (main avatar-url github-url username)
    (login)))

(defn ^:export start []
  (GET "/is-authenticated"
       {:handler show-start}))

(set! (.-onload js/window) start)
