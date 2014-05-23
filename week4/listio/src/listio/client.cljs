(ns my.namespace
  (:require-macros [enfocus.macros :as em]
                   [shoreleave.remotes.macros :as macros])
  (:require [enfocus.core :as ef]
            [shoreleave.remotes.http-rpc :as rpc]))

(em/defsnippet header :compiled "public/prototype/login.html" ["#header"] [])
(em/defsnippet login-form :compiled "public/prototype/login.html" ["#login-form"] [])
(em/defsnippet menu :compiled "public/prototype/main.html" ["#menu"]
  [avatar-url github-url name]
  ".avatar" (ef/set-attr :src avatar-url)
  ".username" (ef/do-> (ef/set-attr :href github-url)
                       (ef/content name)))

(em/defaction login []
    ["body"] (ef/do-> (ef/content (header))
                      (ef/append (login-form))))

(em/defaction main []
    ["body"] (ef/do-> (ef/content (header))
                      (ef/append "MAIN PAGE")))

(defn ^:export console-handler [data]
  (.log js/console (str data)))

(defn ^:export show-start [data]
  (if (:authenticated? data)
    (main)
    (login)))

;; (defn ^:export start []
;;   (GET "/is-authenticated"
;;        {:handler show-start}))

(defn ^:export start[]
  (rpc/remote-callback :remote-fn ["parampampam"] #(js/alert %)))

;; (set! (.-onload js/window) start)
(set! (.-onload js/window) #(em/wait-for-load (start)))
