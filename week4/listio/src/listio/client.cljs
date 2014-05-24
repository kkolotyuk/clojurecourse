(ns my.namespace
  (:require [enfocus.core :as ef]
            [enfocus.events :as events]
            [ajax.core :refer [GET]]
            [jayq.core :refer [$]]
            [clojure.string :as str])
  (:require-macros [enfocus.macros :as em]))

(declare logout init-boxes)

(defn ^:export console-handler [data]
  (.log js/console (str data)))

(em/defsnippet header :compiled "public/prototype/login.html" ["#header"] [])
(em/defsnippet login-form :compiled "public/prototype/login.html" ["#login-form"] [])
(em/defsnippet menu :compiled "public/prototype/main.html" ["#menu"]
  [avatar-url github-url username]
  ".avatar" (ef/set-attr :src avatar-url)
  ".username" (ef/do-> (ef/set-attr :href github-url)
                       (ef/content username))
  ".logout" (events/listen :click logout)
  "#btn-switch-id" (events/listen :click #(init-boxes
                                           (ef/from "#repo"
                                                    (ef/get-prop :value)))))

(em/defsnippet hint :compiled "public/prototype/main.html" [".hint"] [])
(em/defsnippet warning :compiled "public/prototype/main.html" [".warning"] [text]
  ".warning" (ef/content text))
(em/defsnippet four-boxes :compiled "public/prototype/main.html" [".gogogo"] [])
(em/defsnippet one-issue :compiled "public/prototype/main.html" ["#issue-1"] [issue]
  "#issue-1" (ef/set-attr :id (str "issue-" (:number issue)))
  ".number" (ef/content (str "#" (:number issue)))
  ".title a" (ef/do-> (ef/set-attr :href (:html_url issue))
                      (ef/content (:title issue))))

(defn ^:export show-boxes [{:keys [success message issues]}]
  (ef/at
    ".hint" (ef/remove-node)
    ".warning" (ef/remove-node)
    ".gogogo" (ef/remove-node))
  (if success
    (do
      (ef/at "body" (ef/append (four-boxes)))
      (let [boxes ($ :.js-sortable)]
        (.disableSelection (.sortable
                            boxes
                            (js-obj "connectWith" ".js-sortable")))))
    (ef/at "body" (ef/append (warning message)))))


(defn ^:export init-boxes [fullrepo]
  (let [[username reponame] (map str/trim (str/split fullrepo #"/"))]
    (GET (str "/issues/" username "/" reponame)
         {:handler show-boxes})))

(defn ^:export login []
  (ef/at "body"
         (ef/do-> (ef/content (header))
                  (ef/append (login-form)))))

(defn ^:export logout []
  (GET "/logout"
       {:finally login}))

(defn ^:export main [avatar-url github-url username]
  (ef/at "body"
         (ef/do-> (ef/content (header))
                  (ef/append (menu avatar-url github-url username))
                  (ef/append (hint)))))

(defn ^:export show-start [{:keys [authenticated? avatar-url github-url username]}]
  (if authenticated?
    (main avatar-url github-url username)
    (login)))

(defn ^:export start []
  (GET "/is-authenticated"
       {:handler show-start}))

(set! (.-onload js/window) start)
